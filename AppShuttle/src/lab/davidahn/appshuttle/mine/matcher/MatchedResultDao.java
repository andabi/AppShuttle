package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MatchedResultDao {
	private static MatchedResultDao matchedResultDao;
	private SQLiteDatabase db;

	private MatchedResultDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public synchronized static MatchedResultDao getInstance(Context cxt) {
		if (matchedResultDao == null)
			matchedResultDao = new MatchedResultDao(cxt);
		return matchedResultDao;
	}

	public void storeMatchedResult(MatchedResult mCxt) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", mCxt.getTime().getTime());
		row.put("timezone", mCxt.getTimeZone().getID());
		row.put("bhv_type", mCxt.getUserBhvs().getBhvType().toString());
		row.put("bhv_name", mCxt.getUserBhvs().getBhvName());
		row.put("matcher_type", mCxt.getMatcherType().toString());
		row.put("likelihood", mCxt.getLikelihood());
		row.put("inverse_entropy", mCxt.getInverseEntropy());
		db.insert("matched_result", null, row);

		Log.i("stored matched result", mCxt.toString());
	}
	
	public void deleteMatchedResult(Date timeDate){
		db.execSQL("DELETE FROM matched_result WHERE time < " + timeDate.getTime() +";");
	}
}
