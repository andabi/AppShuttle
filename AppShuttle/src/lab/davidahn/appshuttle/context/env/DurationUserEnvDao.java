package lab.davidahn.appshuttle.context.env;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DurationUserEnvDao {
	private static DurationUserEnvDao durationUserEnvDao;
	private SQLiteDatabase db;

	private DurationUserEnvDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public static DurationUserEnvDao getInstance(Context cxt) {
		if (durationUserEnvDao == null)
			durationUserEnvDao = new DurationUserEnvDao(cxt);
		return durationUserEnvDao;
	}

	public void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		ContentValues row = new ContentValues();
		row.put("time", durationUserEnv.getTime().getTime());
		row.put("duration", durationUserEnv.getDuration());
		row.put("end_time", durationUserEnv.getEndTime().getTime());
		row.put("timezone", durationUserEnv.getTimeZone().getID());
		row.put("env_type", durationUserEnv.getEnvType().toString());
		row.put("user_env", gson.toJson(durationUserEnv.getUserEnv()));
		db.insert("duration_user_env", null, row);
		Log.i("stored duration_user_env", durationUserEnv.toString());
	}
	
	public DurationUserEnv retrieveDurationUserEnvContains(Date time, EnvType envType) {
	Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
	Cursor cur = db.rawQuery("SELECT * FROM duration_user_env WHERE time <= "
			+ time.getTime() + " AND end_time >= " + time.getTime() +" AND env_type = '" + envType.toString() + "';", null);
	DurationUserEnv res = null;
	while (cur.moveToNext()) {
		Date startTime = new Date(cur.getLong(0));
		Date endTime = new Date(cur.getLong(2));
		TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
		UserEnv userEnv = gson.fromJson(cur.getString(5), UserEnv.class);

		DurationUserEnv durationUserEnv = new DurationUserEnv.Builder()
		.setTime(startTime)
		.setEndTime(endTime)
		.setTimeZone(timezone)
		.setUserEnv(userEnv)
		.build();
		Log.i("retrieved duration_user_env", durationUserEnv.toString());
		res = durationUserEnv;
	}
	cur.close();
	return res;
	}
	
	public List<DurationUserEnv> retrieveDurationUserEnvIncluded(Date fromTime, Date toTime, EnvType envType) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		Cursor cur = db.rawQuery("SELECT * FROM duration_user_env WHERE time >= "
				+ fromTime.getTime() + " AND end_time <= " + toTime.getTime() +" AND env_type = '" + envType.toString() + "';", null);
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			Date endTime = new Date(cur.getLong(2));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
			UserEnv userEnv = gson.fromJson(cur.getString(5), UserEnv.class);

			DurationUserEnv durationUserEnv = new DurationUserEnv.Builder()
			.setTime(time)
			.setEndTime(endTime)
			.setTimeZone(timezone)
			.setUserEnv(userEnv)
			.build();
			Log.i("retrieved duration_user_env", durationUserEnv.toString());
			res.add(durationUserEnv);
		}
		cur.close();
		return res;
	}
	
	
	public List<DurationUserEnv> retrieveDurationUserEnv(Date fromTime, Date toTime, EnvType envType){
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
		DurationUserEnv headPiece = retrieveDurationUserEnvContains(fromTime, envType);
		if(headPiece != null) {
			res.add(headPiece);
			List<DurationUserEnv> middlePieces = retrieveDurationUserEnvIncluded(fromTime, toTime, envType);
			res.addAll(middlePieces);
			DurationUserEnv tailPiece = retrieveDurationUserEnvContains(toTime, envType);
			if(!headPiece.equals(tailPiece))
				res.add(tailPiece);
			res.get(0).setTime(fromTime);
			res.get(res.size()-1).setEndTime(toTime);
		}
		return res;
	}
	
	public void deleteDurationUserEnv(long time){
		db.execSQL("DELETE FROM duration_user_env WHERE time < " + time +";");
	}
}
