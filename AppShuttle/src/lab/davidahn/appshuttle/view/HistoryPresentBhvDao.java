package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HistoryPresentBhvDao {
	private static final String tableName = "history_present_bhv";
	private static final String columnBhvType = "bhv_type";
	private static final String columnBhvName = "bhv_name";
	private static final String columnRecentPredTime = "recent_pred_time";
	private static final String columnRecentPredScore = "recent_pred_score";
	
	private SQLiteDatabase db;
	
	private static HistoryPresentBhvDao presentBhvDao = new HistoryPresentBhvDao();
	private HistoryPresentBhvDao() {
		db = AppShuttleDBHelper.getDatabase();
	}
	public static HistoryPresentBhvDao getInstance() {
		return presentBhvDao;
	}
	
	public void store(HistoryPresentBhv bhv) {
		ContentValues row = new ContentValues();
		row.put(columnBhvType, bhv.getBhvType().toString());
		row.put(columnBhvName, bhv.getBhvName());
		row.put(columnRecentPredTime, bhv.getRecentPredictionTime());
		row.put(columnRecentPredScore, bhv.getRecentPredictionScore());
		db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.d("PresentBhvDao", "store: " + bhv.toString());
	}
	
	public List<HistoryPresentBhv> retrieveRecent() {
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM " + tableName +
				" ORDER BY " + columnRecentPredTime + " DESC, " + columnRecentPredScore + " DESC "
//				"LIMIT " + topN
				, null);
		List<HistoryPresentBhv> res = new ArrayList<HistoryPresentBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			long recentPredictionTime = cur.getLong(2);
			long recentPredictionScore = cur.getLong(3);
			UserBhv ubhv = UserBhvManager.getInstance().getRegisteredUserBhv(bhvType, bhvName);
			if(ubhv == null)
				continue;
			HistoryPresentBhv hisPresentBhv = new HistoryPresentBhv(ubhv);
			hisPresentBhv.setRecentPredictionTime(recentPredictionTime);
			hisPresentBhv.setRecentPredictionScore(recentPredictionScore);
			res.add(hisPresentBhv);
//			Log.d("PresentBhvDao", "retrieve: " + bhv.toString());
		}
		cur.close();
		return res;
	}

	public void delete(HistoryPresentBhv bhv) {}
	

	public static class DDL {
		public static void createTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
					+ columnBhvType + " TEXT, "
					+ columnBhvName + " TEXT, "
					+ columnRecentPredTime + " INTEGER DEFAULT 0, "
					+ columnRecentPredScore + " INTEGER DEFAULT 0, "
					+ "PRIMARY KEY (" + columnBhvType + ", " + columnBhvName + ") "
					+ ");");
		}
	}
}