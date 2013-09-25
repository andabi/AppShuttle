package lab.davidahn.appshuttle.context.bhv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.context.env.UserLoc;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class DurationUserBhvDao {
	private static DurationUserBhvDao rfdUserCxtDao;
	private SQLiteDatabase db;

	private DurationUserBhvDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public synchronized static DurationUserBhvDao getInstance(Context cxt) {
		if (rfdUserCxtDao == null)
			rfdUserCxtDao = new DurationUserBhvDao(cxt);
		return rfdUserCxtDao;
	}

	public void storeDurationBhv(DurationUserBhv rfdUCxt) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", rfdUCxt.getTimeDate().getTime());
		row.put("duration", rfdUCxt.getDuration());
		row.put("end_time", rfdUCxt.getEndTime().getTime());
		row.put("timezone", rfdUCxt.getTimeZone().getID());
		row.put("bhv_type", rfdUCxt.getBhv().getBhvType().toString());
		row.put("bhv_name", rfdUCxt.getBhv().getBhvName());
		db.insertWithOnConflict("history_user_bhv", null, row, SQLiteDatabase.CONFLICT_REPLACE);
		Log.i("stored duration bhv", rfdUCxt.toString());
	}

	public List<DurationUserBhv> retrieveDurationBhv(long fromTime, long toTime) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		Cursor cur = db.rawQuery("SELECT * FROM history_user_bhv WHERE time >= "
				+ fromTime + " AND end_time <= " + toTime+";", null);
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			long duration = cur.getLong(1);
			Date endTime = new Date(cur.getLong(2));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
//			Type listType = new TypeToken<HashMap<EnvType, UserEnv>>(){}.getType();
			BhvType bhvType= BhvType.valueOf(cur.getString(4));
			String bhvName= cur.getString(5);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);
			DurationUserBhv rfdUCxt = new DurationUserBhv.Builder()
				.setTime(time)
				.setDuration(duration)
				.setEndTime(endTime)
				.setTimeZone(timezone)
//				.setInitialUserEnvs(initialUserEnvs)
				.setBhv(uBhv)
//				.setLocs(locs)
//				.setPlaces(places)
				.build();
			res.add(rfdUCxt);
		}
		cur.close();
		return res;
	}
	
	public void deleteDurationBhv(Date fromTime, Date toTime){
		db.execSQL("DELETE FROM history_user_bhv WHERE time >= "
				+ fromTime.getTime() + " AND end_time <= " + toTime.getTime() +";");
	}
	
	public void deleteDurationBhvBefore(Date timeDate){
		db.execSQL("DELETE FROM history_user_bhv WHERE time < " + timeDate.getTime() +";");
	}
	
	public List<DurationUserBhv> retrieveDurationBhvByBhv(Date fromTime, Date toTime, UserBhv uBhv) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		
		Cursor cur = db.rawQuery("SELECT * FROM history_user_bhv WHERE time >= "
				+ fromTime.getTime() + " AND end_time <= " + toTime.getTime() 
				+ " AND bhv_type = '"+uBhv.getBhvType()+"' AND bhv_name = '"+uBhv.getBhvName()+"';", null);
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			long duration = cur.getLong(1);
			Date endTime = new Date(cur.getLong(2));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
//			Type listType = new TypeToken<HashMap<EnvType, UserEnv>>(){}.getType();
//			HashMap<EnvType, UserEnv> initialUserEnvs = gson.fromJson(cur.getString(4), listType);
//			Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
//			Map<Date, UserLoc>  locs = gson.fromJson(cur.getString(4), listType);
//			Map<Date, UserLoc>  places = gson.fromJson(cur.getString(5), listType);
			DurationUserBhv rfdUCxt = new DurationUserBhv.Builder()
				.setTime(time)
				.setDuration(duration)
				.setEndTime(endTime)
				.setTimeZone(timezone)
//				.setInitialUserEnvs(initialUserEnvs)
				.setBhv(uBhv)
//				.setLocs(locs)
//				.setPlaces(places)
				.build();
			res.add(rfdUCxt);
		}
		cur.close();
		return res;
	}

	//TODO fix
	public File loadRfdCxtAsCsvFile(Context cxt, String fileName, Date sTime, Date eTime) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
				+ sTime.getTime() + " AND e_time <= " + eTime.getTime()+";", null);
		try {
			FileOutputStream fos = cxt.openFileOutput(fileName + ".csv", Context.MODE_PRIVATE);
			String columnnames = "";
			for(String columnname : cur.getColumnNames()) columnnames+=columnname+",";
			columnnames = columnnames.substring(0, columnnames.length()-1);
			columnnames+="\n";
			fos.write(columnnames.getBytes());
			while (cur.moveToNext()) {
				int contextId = cur.getInt(0);
				String startTime = new Date(cur.getLong(1)).toString();
				String endTime = new Date(cur.getLong(2)).toString();
//				String timezone = gson.fromJson(cur.getString(3), TimeZone.class).getID();
				String timezone = cur.getString(3);
				Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
				String locFreqList = gson.fromJson(cur.getString(4), listType).toString();
				String placeFreqList = gson.fromJson(cur.getString(5), listType).toString();
				String bhvType= cur.getString(6);
				String bhvName= cur.getString(7);
				String row = contextId+","+bhvType+","+bhvName+","+startTime+","+endTime+","+timezone+",\""+locFreqList+"\",\"" + placeFreqList + "\"\n";
				fos.write(row.getBytes());
			}
			cur.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(cxt.getFilesDir() + "/" + fileName
				+ ".csv");
	}
	
//	public String storeRfdCxtByBhv(RfdUserCxt rfdUCxt) {
//		Gson gson = new Gson();
//		ContentValues row = new ContentValues();
//		String bhvName = rfdUCxt.getBhv().getBhvName();
//		String tableName = "user_behavior_"+bhvName.replace('.', '_');
//		row.put("s_time", rfdUCxt.getStartTime().getTime());
//		row.put("e_time", rfdUCxt.getEndTime().getTime());
////		row.put("timezone", gson.toJson(rfdUCxt.getTimeZone()));
//		row.put("timezone", rfdUCxt.getTimeZone().getID());
//		row.put("location_list", gson.toJson(rfdUCxt.getLocFreqList()));
//		db.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+" (context_id INTEGER PRIMARY KEY AUTOINCREMENT, s_time INTEGER, e_time INTEGER, timezone TEXT, location_list TEXT);");
//		db.insert(tableName, null, row);
//		Log.i("stored classified refined cxt", row.toString());
//		return tableName;
//	}
}
