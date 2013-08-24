package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MatchedResultDao {
	private static MatchedResultDao matchedResultDao;
	private SQLiteDatabase db;

	private MatchedResultDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public static MatchedResultDao getInstance(Context cxt) {
		if (matchedResultDao == null)
			matchedResultDao = new MatchedResultDao(cxt);
		return matchedResultDao;
	}

	public void storeMatchedResult(MatchedResult mCxt) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
//		Map<EnvType, UserEnv> uEnv = mCxt.getUserEnv();
		row.put("time", mCxt.getTime().getTime());
		row.put("timezone", mCxt.getTimeZone().getID());
		row.put("location", gson.toJson(((LocUserEnv)mCxt.getUserEnv(EnvType.LOCATION)).getLoc()));
		row.put("place", gson.toJson(((PlaceUserEnv)mCxt.getUserEnv(EnvType.PLACE)).getPlace()));
		row.put("bhv_type", mCxt.getUserBhvs().getBhvType().toString());
		row.put("bhv_name", mCxt.getUserBhvs().getBhvName());
		row.put("condition", mCxt.getMatcherType().toString());
		row.put("likelihood", mCxt.getLikelihood());
		row.put("related_cxt", gson.toJson(mCxt.getRelatedCxt()));
		db.insert("matched_context", null, row);

		Log.i("stored matched cxt", mCxt.toString());
	}
	
	public void deleteMatchedResult(long time){
		db.execSQL("DELETE FROM matched_context WHERE time < " + time +";");
	}
}
