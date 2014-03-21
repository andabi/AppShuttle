package lab.davidahn.appshuttle.collect.bhv;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DurationUserBhvDao {
	private static final String tableName = "history_user_bhv";
	private static final String columnTime = "time";
	private static final String columnDuration = "duration";
	private static final String columnEndTime = "end_time";
	private static final String columnTimeZone = "timezone";
	private static final String columnBhvType = "bhv_type";
	private static final String columnBhvName = "bhv_name";
	private static final String index1Name = "idx1_history_user_bhv";
	private static final String index2Name = "idx2_history_user_bhv";
	
	private static DurationUserBhvDao durationUserBhvDao = new DurationUserBhvDao();
	private SQLiteDatabase db;

	private DurationUserBhvDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
	}

	public static DurationUserBhvDao getInstance() {
		return durationUserBhvDao;
	}

	public void createTable() {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ columnTime + " INTEGER, "
				+ columnDuration + " INTEGER, "
				+ columnEndTime + " INTEGER, "
				+ columnTimeZone + " TEXT, "
				+ columnBhvType + " TEXT, "
				+ columnBhvName + " TEXT, "
				+ "PRIMARY KEY (" + columnTime + ", " + columnTimeZone + ", "
					+ columnBhvType + ", " + columnBhvName + ") "
				+ ");");
		db.execSQL("CREATE INDEX " + index1Name + " on " + tableName + " (" 
				+ columnTime + ")");
		db.execSQL("CREATE INDEX " + index2Name + " on " + tableName + " (" 
				+ columnTime + ", " + columnBhvType + ", " + columnBhvName +")");
	}
	
	public void store(DurationUserBhv durationUserBhv) {
		ContentValues row = new ContentValues();
		row.put(columnTime, durationUserBhv.getTime());
		row.put(columnDuration, durationUserBhv.getDuration());
		row.put(columnEndTime, durationUserBhv.getEndTime());
		row.put(columnTimeZone, durationUserBhv.getTimeZone().getID());
		row.put(columnBhvType, durationUserBhv.getUserBhv().getBhvType().toString());
		row.put(columnBhvName, durationUserBhv.getUserBhv().getBhvName());
//		db.insertWithOnConflict("history_user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
		db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.i("stored duration bhv", durationUserBhv.toString());
	}

	public List<DurationUserBhv> retrieve(long beginTime, long endTime) {
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM " + tableName +
				" WHERE " + columnTime + " >= " + beginTime +
					" AND " + columnTime + " < " + endTime + ";"
			, null);
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		while (cur.moveToNext()) {
//			Type listType = new TypeToken<HashMap<EnvType, UserEnv>>(){}.getType();
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(4));
			String bhvName= cur.getString(5);
			BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			DurationUserBhv durationUserBhv = new DurationUserBhv.Builder()
				.setTime(cur.getLong(0))
				.setDuration(cur.getLong(1))
				.setEndTime(cur.getLong(2))
				.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
				.setBhv(uBhv)
				.build();
			res.add(durationUserBhv);
		}
		cur.close();
		return res;
	}
	
	public List<DurationUserBhv> retrieveByBhv(long beginTime, long endTime, UserBhv uBhv) {
			Cursor cur = db.rawQuery(
					"SELECT *" +
					" FROM " + tableName +
					" WHERE " + columnTime + " >= " + beginTime +
						" AND " + columnTime + " < " + endTime +
						" AND " + columnBhvType + " = '" + uBhv.getBhvType() + "' " +
						" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';"
				, null);
			
			List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
			while (cur.moveToNext()) {
	//			Type listType = new TypeToken<HashMap<EnvType, UserEnv>>(){}.getType();
				DurationUserBhv durationUserBhv = new DurationUserBhv.Builder()
					.setTime(cur.getLong(0))
					.setDuration(cur.getLong(1))
					.setEndTime(cur.getLong(2))
					.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
					.setBhv(uBhv)
					.build();
				res.add(durationUserBhv);
			}
			cur.close();
			return res;
		}

	public List<DurationUserBhv> retrieveOnEndTimeByBhv(long beginTime, long endTime, UserBhv uBhv) {
			Cursor cur = db.rawQuery(
					"SELECT *" +
					" FROM " + tableName +
					" WHERE " + columnEndTime + " >= " + beginTime +
						" AND " + columnEndTime + " < " + endTime +
						" AND " + columnBhvType + " = '" + uBhv.getBhvType() + "' " +
						" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';"
				, null);
			List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
			while (cur.moveToNext()) {
	//			Type listType = new TypeToken<HashMap<EnvType, UserEnv>>(){}.getType();
				DurationUserBhv durationUserBhv = new DurationUserBhv.Builder()
					.setTime(cur.getLong(0))
					.setDuration(cur.getLong(1))
					.setEndTime(cur.getLong(2))
					.setTimeZone(TimeZone.getTimeZone(cur.getString(3)))
					.setBhv(uBhv)
					.build();
				res.add(durationUserBhv);
			}
			cur.close();
			return res;
		}

	public void delete(long beginTime, long endTime){
		db.execSQL(
				"DELETE FROM " + tableName +
				" WHERE " + columnTime + " >= " + beginTime +
					" AND " + columnTime + " < " + endTime + ";");
	}
	
	public void deleteBefore(long time){
		db.execSQL(
				"DELETE FROM " + tableName +
				" WHERE " + columnTime + " < " + time + ";");
	}

//	//TODO fix
//	public File loadRfdCxtAsCsvFile(Context cxt, String fileName, Date sTime, Date eTime) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
//
//		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
//				+ sTime.getTime() + " AND e_time <= " + eTime.getTime()+";", null);
//		try {
//			FileOutputStream fos = cxt.openFileOutput(fileName + ".csv", Context.MODE_PRIVATE);
//			String columnnames = "";
//			for(String columnname : cur.getColumnNames()) columnnames+=columnname+",";
//			columnnames = columnnames.substring(0, columnnames.length()-1);
//			columnnames+="\n";
//			fos.write(columnnames.getBytes());
//			while (cur.moveToNext()) {
//				int contextId = cur.getInt(0);
//				String startTime = new Date(cur.getLong(1)).toString();
//				String endTime = new Date(cur.getLong(2)).toString();
////				String timezone = gson.fromJson(cur.getString(3), TimeZone.class).getID();
//				String timezone = cur.getString(3);
//				Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
//				String locFreqList = gson.fromJson(cur.getString(4), listType).toString();
//				String placeFreqList = gson.fromJson(cur.getString(5), listType).toString();
//				String bhvType= cur.getString(6);
//				String bhvName= cur.getString(7);
//				String row = contextId+","+bhvType+","+bhvName+","+startTime+","+endTime+","+timezone+",\""+locFreqList+"\",\"" + placeFreqList + "\"\n";
//				fos.write(row.getBytes());
//			}
//			cur.close();
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return new File(cxt.getFilesDir() + "/" + fileName
//				+ ".csv");
//	}
	
//	public String storeRfdCxtByBhv(RfdUserCxt durationUserBhv) {
//		Gson gson = new Gson();
//		ContentValues row = new ContentValues();
//		String bhvName = durationUserBhv.getBhv().getBhvName();
//		String tableName = "user_behavior_"+bhvName.replace('.', '_');
//		row.put("s_time", durationUserBhv.getStartTime().getTime());
//		row.put("e_time", durationUserBhv.getEndTime());
////		row.put("timezone", gson.toJson(durationUserBhv.getTimeZone()));
//		row.put("timezone", durationUserBhv.getTimeZone().getID());
//		row.put("location_list", gson.toJson(durationUserBhv.getLocFreqList()));
//		db.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+" (context_id INTEGER PRIMARY KEY AUTOINCREMENT, s_time INTEGER, e_time INTEGER, timezone TEXT, location_list TEXT);");
//		db.insert(tableName, null, row);
//		Log.i("stored classified refined cxt", row.toString());
//		return tableName;
//	}
}
