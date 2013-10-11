package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PredictedBhvInfoDao {
	private SQLiteDatabase _db;
	private MatchedResultDao _matchedResultDao;

	private static PredictedBhvInfoDao predictedBhvInfoDao = new PredictedBhvInfoDao();
	private PredictedBhvInfoDao() {
		_db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		_matchedResultDao = MatchedResultDao.getInstance();
	}
	public static PredictedBhvInfoDao getInstance() {
		return predictedBhvInfoDao;
	}

	public void storePredictedBhv(PredictedBhvInfo predictedBhvInfo) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", predictedBhvInfo.getTime().getTime());
		row.put("timezone", predictedBhvInfo.getTimeZone().getID());
		row.put("user_envs", gson.toJson(predictedBhvInfo.getUserEnvMap()));
		row.put("bhv_type", predictedBhvInfo.getUserBhv().getBhvType().toString());
		row.put("bhv_name", predictedBhvInfo.getUserBhv().getBhvName());
		row.put("score", predictedBhvInfo.getScore());
		_db.insert("predicted_bhv", null, row);

		for(MatchedResult matchedRes : predictedBhvInfo.getMatchedResultMap().values()) {
			_matchedResultDao.storeMatchedResult(matchedRes);
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
