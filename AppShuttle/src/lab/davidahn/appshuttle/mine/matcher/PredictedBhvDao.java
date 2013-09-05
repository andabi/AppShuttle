package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PredictedBhvDao {
	private static PredictedBhvDao predictedBhvDao;
	private SQLiteDatabase db;
	private MatchedResultDao matchedResultDao;

	private PredictedBhvDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
		matchedResultDao = MatchedResultDao.getInstance(cxt);
	}

	public synchronized static PredictedBhvDao getInstance(Context cxt) {
		if (predictedBhvDao == null)
			predictedBhvDao = new PredictedBhvDao(cxt);
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
		db.insert("predicted_bhv", null, row);

		for(MatchedResult matchedRes : predictedBhv.getMatchedResultMap().values()) {
			matchedResultDao.storeMatchedResult(matchedRes);
		}
		
		Log.i("stored predicted bhv", predictedBhv.toString());
	}
	
	public void deletePredictedBhv(long time){
		db.execSQL("DELETE FROM predicted_bhv WHERE time < " + time +";");
	}
}
