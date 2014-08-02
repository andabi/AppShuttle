package lab.davidahn.appshuttle.collect.env;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

public class DurationUserEnvDao {
	private static final String tableName = "history_user_env";
	private static final String columnTime = "time";
	private static final String columnDuration = "duration";
	private static final String columnEndTime = "end_time";
	private static final String columnTimeZone = "timezone";
	private static final String columnEnvType = "env_type";
	private static final String columnEnv = "user_env";
	private static final String index1Name = "idx1_history_user_env";
	private static final String index2Name = "idx2_history_user_env";
	
	private static DurationUserEnvDao durationUserEnvDao = new DurationUserEnvDao();
	private SQLiteDatabase db;
	private Gson gson;
	
	private DurationUserEnvDao() {
		db = AppShuttleDBHelper.getDatabase();
		gson = new Gson();
	}

	public static DurationUserEnvDao getInstance() {
		return durationUserEnvDao;
	}

	public void store(DurationUserEnv durationUserEnv) {
		ContentValues row = new ContentValues();
		row.put(columnTime, durationUserEnv.getTime());
		row.put(columnDuration, durationUserEnv.getDuration());
		row.put(columnEndTime, durationUserEnv.getEndTime());
		row.put(columnTimeZone, durationUserEnv.getTimeZone().getID());
		row.put(columnEnvType, durationUserEnv.getUserEnv().getEnvType()
				.toString());
		row.put(columnEnv, gson.toJson(durationUserEnv.getUserEnv()));
		db.insert(tableName, null, row);
		// Log.i("stored history_user_env", durationUserEnv.toString());
	}

	public DurationUserEnv retrieveContains(long time, EnvType envType) {
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM " + tableName +
				" WHERE " + columnTime + " <= " + time +
					" AND " + columnEndTime + " > " + time + 
					" AND " + columnEnvType + " = '" + envType.toString() + "';"
			, null);
		DurationUserEnv res = null;
		while (cur.moveToNext()) {
			DurationUserEnv durationUserEnv = new DurationUserEnv.Builder()
					.setTime(cur.getLong(0))
					.setEndTime(cur.getLong(2))
					.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
					.setUserEnv(
							gson.fromJson(cur.getString(5), envType.getClazz()))
					.build();
			// Log.i("retrieved history_user_env", durationUserEnv.toString());
			res = durationUserEnv;
		}
		cur.close();
		return res;
	}

	public DurationUserEnv retrieveContains(long fromTime, long toTime,
			EnvType envType) {
		Cursor cur = db.rawQuery(
				"SELECT *" + 
				" FROM " + tableName + 
				" WHERE " + columnTime + " <= " + fromTime + 
					" AND " + columnEndTime + " > " + toTime + 
					" AND " + columnEnvType + " = '" + envType.toString() + "';"
			, null);
		DurationUserEnv res = null;
		while (cur.moveToNext()) {
			DurationUserEnv durationUserEnv = new DurationUserEnv.Builder()
					.setTime(cur.getLong(0))
					.setEndTime(cur.getLong(2))
					.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
					.setUserEnv(
							gson.fromJson(cur.getString(5), envType.getClazz()))
					.build();
			// Log.i("retrieved history_user_env", durationUserEnv.toString());
			res = durationUserEnv;
		}
		cur.close();
		return res;
	}

	public List<DurationUserEnv> retrieveBetween(long fromTime, long toTime,
			EnvType envType) {
		Cursor cur = db.rawQuery(
				"SELECT *" + 
				" FROM " + tableName + 
				" WHERE " + columnTime + " >= " + fromTime + 
					" AND " + columnEndTime + " < " + toTime +
					" AND " + columnEnvType + " = '" + envType.toString() + "';"
			, null);
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
		while (cur.moveToNext()) {
			DurationUserEnv durationUserEnv = new DurationUserEnv.Builder()
					.setTime(cur.getLong(0))
					.setEndTime(cur.getLong(2))
					.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
					.setUserEnv(
							gson.fromJson(cur.getString(5), envType.getClazz()))
					.build();
			// Log.i("retrieved history_user_env", durationUserEnv.toString());
			res.add(durationUserEnv);
		}
		cur.close();
		return res;
	}

	public void deleteBefore(long time) {
		db.execSQL(
				"DELETE FROM " + tableName + " WHERE " + columnTime + " < "	+ time + ";");
	}

	public void deleteBetween(long beginTime, long endTime) {
		db.execSQL("DELETE FROM " + tableName + " WHERE " + columnTime + " >= " + beginTime +
				" AND " + columnTime + " < " + endTime + "';");
	}

	public static class DDL {
		public static void createTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ columnTime + " INTEGER, "
				+ columnDuration + " INTEGER, "
				+ columnEndTime + " INTEGER, "
				+ columnTimeZone + " TEXT, "
				+ columnEnvType + " TEXT, "
				+ columnEnv + " TEXT, "
				+ "PRIMARY KEY (" + columnTime + ", " + columnTimeZone + ", " + columnEnvType + ") "
				+ ");");
			db.execSQL("CREATE INDEX " + index1Name + " on " + tableName + " (" 
				+ columnTime + ")");
			db.execSQL("CREATE INDEX " + index2Name + " on " + tableName + " (" 
				+ columnTime + ", " + columnEndTime + ", " + columnEnvType +")");
		}
	}
}
