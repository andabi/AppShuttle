package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PredictedBhvDao {
	private SQLiteDatabase _db;
	private MatchedResultDao _matchedResultDao;

	private static PredictedBhvDao predictedBhvInfoDao = new PredictedBhvDao();
	private PredictedBhvDao() {
		_db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		_matchedResultDao = MatchedResultDao.getInstance();
	}
	public static PredictedBhvDao getInstance() {
		return predictedBhvInfoDao;
	}

	public void storePredictedBhv(PredictionInfo predictionInfo) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", predictionInfo.getTime().getTime());
		row.put("timezone", predictionInfo.getTimeZone().getID());
		row.put("user_envs", gson.toJson(predictionInfo.getUserEnvMap()));
		row.put("bhv_type", predictionInfo.getUserBhv().getBhvType().toString());
		row.put("bhv_name", predictionInfo.getUserBhv().getBhvName());
		row.put("score", predictionInfo.getScore());
		_db.insert("predicted_bhv", null, row);
		
		for(MatcherGroupResult matcherGroupResult : predictionInfo.getMatcherGroupResultMap().values()) {
			for(MatcherResult matchedResult : matcherGroupResult.getMatcherResultMap().values()) {
				_matchedResultDao.storeMatchedResult(matchedResult);
			}
		}
		
//		Log.i("stored predicted bhv", predictedBhvInfo.toString());
	}
	
	public void deletePredictedBhvInfo(Date timeDate){
		_db.execSQL("" +
				"DELETE " +
				"FROM predicted_bhv " +
				"WHERE time < " + timeDate.getTime() +";");
	}
}
