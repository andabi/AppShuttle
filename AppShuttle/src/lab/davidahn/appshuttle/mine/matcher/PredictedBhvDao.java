package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PredictedBhvDao {
	private SQLiteDatabase _db;
	private MatchedResultDao _matchedResultDao;

	private static PredictedBhvDao predictedBhvDao = new PredictedBhvDao();
	private PredictedBhvDao() {
		_db = DBHelper.getInstance().getWritableDatabase();
		_matchedResultDao = MatchedResultDao.getInstance();
	}

	public static PredictedBhvDao getInstance() {
		return predictedBhvDao;
	}

	public void storePredictedBhv(PredictedBhv predictedBhv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", predictedBhv.getTime().getTime());
		row.put("timezone", predictedBhv.getTimeZone().getID());
		row.put("user_envs", gson.toJson(predictedBhv.getUserEnvMap()));
		row.put("bhv_type", predictedBhv.getUserBhv().getBhvType().toString());
		row.put("bhv_name", predictedBhv.getUserBhv().getBhvName());
		row.put("score", predictedBhv.getScore());
		_db.insert("predicted_bhv", null, row);

		for(MatchedResult matchedRes : predictedBhv.getMatchedResultMap().values()) {
			_matchedResultDao.storeMatchedResult(matchedRes);
		}
		
		Log.i("stored predicted bhv", predictedBhv.toString());
	}
	
	public void deletePredictedBhv(Date timeDate){
		_db.execSQL("" +
				"DELETE " +
				"FROM predicted_bhv " +
				"WHERE time < " + timeDate.getTime() +";");
	}
}
