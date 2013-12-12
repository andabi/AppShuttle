/*package lab.davidahn.appshuttle.predict;

import java.util.Date;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultDao;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PredictionInfoDao {
	private SQLiteDatabase db;
	private MatcherResultDao matcherResultDao;

	private static PredictionInfoDao predictionInfoDao = new PredictionInfoDao();
	private PredictionInfoDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		matcherResultDao = MatcherResultDao.getInstance();
	}
	public static PredictionInfoDao getInstance() {
		return predictionInfoDao;
	}

	public void storePredictionInfo(PredictionInfo predictionInfo) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", predictionInfo.getTimeDate().getTime());
		row.put("timezone", predictionInfo.getTimeZone().getID());
		row.put("user_envs", gson.toJson(predictionInfo.getUserEnvMap()));
		row.put("bhv_type", predictionInfo.getUserBhv().getBhvType().toString());
		row.put("bhv_name", predictionInfo.getUserBhv().getBhvName());
		row.put("score", predictionInfo.getScore());
		db.insert("predicted_bhv", null, row);
		
		for(MatcherGroupResult matcherGroupResult : predictionInfo.getMatcherGroupResultMap().values()) {
			for(MatcherResult matcherResult : matcherGroupResult.getMatcherResultMap().values()) {
				matcherResultDao.storeMatcherResult(matcherResult);
			}
		}
//		Log.i("stored prediction info", predictionInfo.toString());
	}
	
	public void deletePredictionInfoBefore(Date timeDate){
		db.execSQL("" +
				"DELETE " +
				"FROM predicted_bhv " +
				"WHERE time < " + timeDate.getTime() +";");
	}
}
*/