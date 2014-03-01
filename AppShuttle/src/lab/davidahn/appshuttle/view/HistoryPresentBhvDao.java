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
	private SQLiteDatabase db;
	
	private static HistoryPresentBhvDao presentBhvDao = new HistoryPresentBhvDao();
	private HistoryPresentBhvDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
	}
	public static HistoryPresentBhvDao getInstance() {
		return presentBhvDao;
	}

	public void store(HistoryPresentBhv bhv) {
		ContentValues row = new ContentValues();
		row.put("bhv_type", bhv.getBhvType().toString());
		row.put("bhv_name", bhv.getBhvName());
		row.put("recent_pred_time", bhv.getRecentPredictionTime());
		row.put("recent_pred_score", bhv.getRecentPredictionScore());
		db.insertWithOnConflict("history_present_bhv", null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.d("PresentBhvDao", "store: " + bhv.toString());
	}
	
	public List<HistoryPresentBhv> retrieveRecent() {
		Cursor cur = db.rawQuery(
				"SELECT * " +
				"FROM history_present_bhv " +
				"ORDER BY recent_pred_time DESC, recent_pred_score DESC "
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

	public void delete(HistoryPresentBhv bhv) {
		
	}
}