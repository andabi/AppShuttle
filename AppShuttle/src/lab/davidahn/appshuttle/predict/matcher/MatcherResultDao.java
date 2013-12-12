/*package lab.davidahn.appshuttle.predict.matcher;

import java.util.Date;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class MatcherResultDao {
	private static MatcherResultDao matcherResultDao = new MatcherResultDao();
	private SQLiteDatabase db;

	private MatcherResultDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
	}

	public static MatcherResultDao getInstance() {
		return matcherResultDao;
	}

	public void storeMatcherResult(MatcherResult mCxt) {
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

//		Log.i("stored matched result", mCxt.toString());
	}
	
	public void deleteMatcherResult(Date timeDate){
		db.execSQL("DELETE FROM matched_result WHERE time < " + timeDate.getTime() +";");
	}
}
*/