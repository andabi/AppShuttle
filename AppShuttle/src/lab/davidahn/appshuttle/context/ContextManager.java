package lab.davidahn.appshuttle.context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.ChangedUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.mine.matcher.MatchedResult;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhv;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ContextManager {
	private static ContextManager contextManager;
	private SQLiteDatabase db;
	private ContextRefiner cxtRefiner;

	public ContextRefiner getCxtRefiner() {
		return cxtRefiner;
	}

	private ContextManager(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
		cxtRefiner = new ContextRefiner(cxt);
	}

	public static ContextManager getInstance(Context cxt) {
		if (contextManager == null)
			contextManager = new ContextManager(cxt);
		return contextManager;
	}

	public void storeCxt(UserCxt uCxt) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		for(UserBhv uBhv : uCxt.getUserBhvs()){
			ContentValues row = new ContentValues();
//			UserEnv uEnv = uCxt.getUserEnv();
			row.put("time", uCxt.getTime().getTime());
			row.put("timezone", uCxt.getTimeZone().getID());
			row.put("location", gson.toJson(((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)).getLoc()));
			row.put("place", gson.toJson(((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)).getPlace()));
			row.put("bhv_type", uBhv.getBhvType());
			row.put("bhv_name", uBhv.getBhvName());
			db.insert("context", null, row);
			Log.i("stored cxt", uCxt.toString());
		}
	}
	
	public List<UserCxt> retrieveCxt(Date sTime, Date eTime) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		Cursor cur = db.rawQuery("SELECT * FROM context WHERE time >= "
				+ sTime.getTime() + " AND time <= " + eTime.getTime()+";", null);
		List<UserCxt> res = new ArrayList<UserCxt>();
		
		UserCxt uCxt = null;
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(1));
			UserLoc location = gson.fromJson(cur.getString(2), UserLoc.class);
			UserLoc place = gson.fromJson(cur.getString(3), UserLoc.class);
			String bhvType= cur.getString(4);
			String bhvName= cur.getString(5);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);

			if(uCxt == null) {
				uCxt = new UserCxt(time, timezone);
				uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(location));
				uCxt.addUserEnv(EnvType.PLACE, new LocUserEnv(place));
				uCxt.addUserBhv(uBhv);
			}
			else {
				UserCxt tempCxt = new UserCxt(time, timezone);
				tempCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(location));
				tempCxt.addUserEnv(EnvType.PLACE, new LocUserEnv(place));
				
				if(tempCxt.getUserEnvs().equals(uCxt.getUserEnvs()))
					uCxt.addUserBhv(uBhv);
				else{
					res.add(uCxt);
					uCxt = new UserCxt(time, timezone);
					uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(location));
					uCxt.addUserEnv(EnvType.PLACE, new LocUserEnv(place));
					uCxt.addUserBhv(uBhv);
				}
			}
		}
		cur.close();
		return res;
	}

	public void storeRfdCxt(RfdUserCxt rfdUCxt) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("s_time", rfdUCxt.getStartTime().getTime());
		row.put("e_time", rfdUCxt.getEndTime().getTime());
//		row.put("timezone", gson.toJson(rfdUCxt.getTimeZone()));
		row.put("timezone", rfdUCxt.getTimeZone().getID());
		row.put("locations", gson.toJson(rfdUCxt.getLocs()));
		row.put("places", gson.toJson(rfdUCxt.getPlaces()));
		row.put("bhv_type", rfdUCxt.getBhv().getBhvType());
		row.put("bhv_name", rfdUCxt.getBhv().getBhvName());
		db.insert("refined_context", null, row);
		Log.i("stored refined cxt", rfdUCxt.toString());
	}

	public List<RfdUserCxt> retrieveRfdCxt(long sTime, long eTime) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
				+ sTime + " AND e_time <= " + eTime+";", null);
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
		while (cur.moveToNext()) {
//			int contextId = cur.getInt(0);
			Date startTime = new Date(cur.getLong(1));
			Date endTime = new Date(cur.getLong(2));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
			Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
			Map<Date, UserLoc>  locs = gson.fromJson(cur.getString(4), listType);
			Map<Date, UserLoc>  places = gson.fromJson(cur.getString(5), listType);
			String bhvType= cur.getString(6);
			String bhvName= cur.getString(7);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);
			RfdUserCxt rfdUCxt = new RfdUserCxt.Builder()
				.setStartTime(startTime)
				.setEndTime(endTime)
				.setTimeZone(timezone)
				.setBhv(uBhv)
				.setLocs(locs)
				.setPlaces(places)
//				.setContextId(contextId)
				.build();
			res.add(rfdUCxt);
		}
		cur.close();
		return res;
	}
	
	public void removeRfdCxt(Date sTime, Date eTime){
		db.execSQL("DELETE * FROM refined_context WHERE s_time >= "
				+ sTime.getTime() + " AND e_time <= " + eTime.getTime() +";");
	}
	
	public void removeRfdCxtBefore(long time){
		db.execSQL("DELETE * FROM refined_context WHERE s_time < " + time +";");
	}
	
	public List<RfdUserCxt> retrieveRfdCxtByBhv(long sTime, long eTime, UserBhv uBhv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
		
		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
				+ sTime + " AND e_time <= " + eTime+" AND bhv_type = '"+uBhv.getBhvType()+"' AND bhv_name = '"+uBhv.getBhvName()+"';", null);
		while (cur.moveToNext()) {
//			int contextId = cur.getInt(0);
			Date startTime = new Date(cur.getLong(1));
			Date endTime = new Date(cur.getLong(2));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(3));
			Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
			Map<Date, UserLoc>  locs = gson.fromJson(cur.getString(4), listType);
			Map<Date, UserLoc>  places = gson.fromJson(cur.getString(5), listType);
			RfdUserCxt rfdUCxt = new RfdUserCxt.Builder()
				.setStartTime(startTime)
				.setEndTime(endTime)
				.setTimeZone(timezone)
				.setBhv(uBhv)
				.setLocs(locs)
				.setPlaces(places)
//				.setContextId(contextId)
				.build();
			res.add(rfdUCxt);
		}
		cur.close();
		return res;
	}
	
//	public String storeRfdCxtByBhv(RfdUserCxt rfdUCxt) {
//		Gson gson = new Gson();
//		ContentValues row = new ContentValues();
//		String bhvName = rfdUCxt.getBhv().getBhvName();
//		String tableName = "user_behavior_"+bhvName.replace('.', '_');
//		row.put("s_time", rfdUCxt.getStartTime().getTime());
//		row.put("e_time", rfdUCxt.getEndTime().getTime());
////		row.put("timezone", gson.toJson(rfdUCxt.getTimeZone()));
//		row.put("timezone", rfdUCxt.getTimeZone().getID());
//		row.put("location_list", gson.toJson(rfdUCxt.getLocFreqList()));
//		db.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+" (context_id INTEGER PRIMARY KEY AUTOINCREMENT, s_time INTEGER, e_time INTEGER, timezone TEXT, location_list TEXT);");
//		db.insert(tableName, null, row);
//		Log.i("stored classified refined cxt", row.toString());
//		return tableName;
//	}

	public void storeMatchedCxt(MatchedResult mCxt) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
//		Map<EnvType, UserEnv> uEnv = mCxt.getUserEnv();
		row.put("time", mCxt.getTime().getTime());
		row.put("timezone", mCxt.getTimeZone().getID());
		row.put("location", gson.toJson(((LocUserEnv)mCxt.getUserEnv(EnvType.LOCATION)).getLoc()));
		row.put("place", gson.toJson(((PlaceUserEnv)mCxt.getUserEnv(EnvType.PLACE)).getPlace()));
		row.put("bhv_type", mCxt.getUserBhvs().getBhvType());
		row.put("bhv_name", mCxt.getUserBhvs().getBhvName());
		row.put("condition", mCxt.getMatcherType().toString());
		row.put("likelihood", mCxt.getLikelihood());
		row.put("related_cxt", gson.toJson(mCxt.getRelatedCxt()));
		db.insert("matched_context", null, row);

		Log.i("stored matched cxt", mCxt.toString());
	}
	
	public void storePredictedBhv(PredictedBhv predictedBhv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		ContentValues row = new ContentValues();
		row.put("time", predictedBhv.getTime().getTime());
		row.put("timezone", predictedBhv.getTimeZone().getID());
		row.put("location", gson.toJson(((LocUserEnv)predictedBhv.getUserEnv(EnvType.LOCATION)).getLoc()));
		row.put("place", gson.toJson(((PlaceUserEnv)predictedBhv.getUserEnv(EnvType.PLACE)).getPlace()));
		row.put("bhv_type", predictedBhv.getUserBhv().getBhvType());
		row.put("bhv_name", predictedBhv.getUserBhv().getBhvName());
		row.put("score", predictedBhv.getScore());
		db.insert("predicted_bhv", null, row);

		Log.i("stored predicted bhv", predictedBhv.toString());
	}
	
//	public List<RfdUserCxt> loadMatchedCxt(Context cxt, String fileName, Date sTime, Date eTime) {
//		Gson gson = new Gson();
//		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
//				+ sTime.getTime() + " AND e_time <= " + eTime.getTime()+";", null);
//
//		Cursor cur = db.rawQuery("SELECT context_id, s_time, e_time, bhv_name FROM refined_context WHERE e_time < "+validEndTime+";", null);
//		while (cur.moveToNext()) {
//			int contextId = cur.getInt(0);
//			String bhvName= cur.getString(3);
//			
//			if(!numTotalCxtMap.containsKey(bhvName)) numTotalCxtMap.put(bhvName, 0);
//			numTotalCxtMap.put(bhvName, numTotalCxtMap.get(bhvName) + 1);
//			
//			if(!relatedCxtMap.containsKey(bhvName)) relatedCxtMap.put(bhvName, new SparseArray<Double>());
//			SparseArray<Double> relatedCxt = relatedCxtMap.get(bhvName);
//			double relatedness = calcRelatedness(cur, uEnv);
//			relatedCxt.put(contextId, relatedness);
//			relatedCxtMap.put(bhvName, relatedCxt);
//		}
//		cur.close();
//
//		
//		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
//		while (cur.moveToNext()) {
//			Date startTime = new Date(cur.getLong(1));
//			Date endTime = new Date(cur.getLong(2));
//			TimeZone timezone = gson.fromJson(cur.getString(3), TimeZone.class);
//			Type listType = new TypeToken<ArrayList<LocFreq>>(){}.getType();
//			List<LocFreq> locFreqList = gson.fromJson(cur.getString(4), listType);
//			String bhvType= cur.getString(5);
//			String bhvName= cur.getString(6);
//			UserBhv uBhv = new UserBhv(bhvType, bhvName);
//			RfdUserCxt rfdUCxt = new RfdUserCxt(startTime, endTime, timezone, locFreqList, uBhv);
//			res.add(rfdUCxt);
//		}
//		cur.close();
//		return res;
//	}
	
	
	public File loadCxtAsCsvFile(Context cxt, String fileName, Date sTime, Date eTime) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		Cursor cur = db.rawQuery("SELECT * FROM context WHERE time >= "
				+ sTime.getTime() + " AND time <= " + eTime.getTime()+";", null);

		try {
			FileOutputStream fos = cxt.openFileOutput(fileName + ".csv", Context.MODE_PRIVATE);
			String columnnames = "";
			for(String columnname : cur.getColumnNames()) columnnames+=columnname+",";
			columnnames = columnnames.substring(0, columnnames.length()-1);
			columnnames+="\n";
//			columnnames = columnnames.replaceAll("location", "longitude,latitude");
			fos.write(columnnames.getBytes());
			while (cur.moveToNext()) {
				int contextId = cur.getInt(0);
				String time = new Date(cur.getLong(1)).toString();
				String timezone = cur.getString(2);
				String location = gson.fromJson(cur.getString(3), UserLoc.class).toString();
				String place = gson.fromJson(cur.getString(4), UserLoc.class).toString();
				String bhvType= cur.getString(5);
				String bhvName= cur.getString(6);
				String row = contextId+","+time+","+timezone+",\""+location+"\",\""+place+"\","+bhvType+","+bhvName+"\n";
				fos.write(row.getBytes());
			}
			cur.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(cxt.getFilesDir() + "/" + fileName
				+ ".csv");
	}
	
	public File loadRfdCxtAsCsvFile(Context cxt, String fileName, Date sTime, Date eTime) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		Cursor cur = db.rawQuery("SELECT * FROM refined_context WHERE s_time >= "
				+ sTime.getTime() + " AND e_time <= " + eTime.getTime()+";", null);
		try {
			FileOutputStream fos = cxt.openFileOutput(fileName + ".csv", Context.MODE_PRIVATE);
			String columnnames = "";
			for(String columnname : cur.getColumnNames()) columnnames+=columnname+",";
			columnnames = columnnames.substring(0, columnnames.length()-1);
			columnnames+="\n";
			fos.write(columnnames.getBytes());
			while (cur.moveToNext()) {
				int contextId = cur.getInt(0);
				String startTime = new Date(cur.getLong(1)).toString();
				String endTime = new Date(cur.getLong(2)).toString();
//				String timezone = gson.fromJson(cur.getString(3), TimeZone.class).getID();
				String timezone = cur.getString(3);
				Type listType = new TypeToken<TreeMap<Date, UserLoc>>(){}.getType();
				String locFreqList = gson.fromJson(cur.getString(4), listType).toString();
				String placeFreqList = gson.fromJson(cur.getString(5), listType).toString();
				String bhvType= cur.getString(6);
				String bhvName= cur.getString(7);
				String row = contextId+","+bhvType+","+bhvName+","+startTime+","+endTime+","+timezone+",\""+locFreqList+"\",\"" + placeFreqList + "\"\n";
				fos.write(row.getBytes());
			}
			cur.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new File(cxt.getFilesDir() + "/" + fileName
				+ ".csv");
	}

	public void storeChangedUserEnv(ChangedUserEnv changedUserEnv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		ContentValues row = new ContentValues();
		row.put("time", changedUserEnv.getTime().getTime());
		row.put("timezone", changedUserEnv.getTimeZone().getID());
		row.put("env_type", changedUserEnv.getEnvType().toString());
		row.put("from_value", gson.toJson(changedUserEnv.getFromUserEnv()));
		row.put("to_value", gson.toJson(changedUserEnv.getToUserEnv()));
		db.insert("changed_env", null, row);
		Log.i("stored changed env", changedUserEnv.toString());
	}
	
	public List<ChangedUserEnv> retrieveChangedUserEnv(Date sTime, Date eTime, EnvType envType) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		Cursor cur = db.rawQuery("SELECT * FROM changed_env WHERE time >= "
				+ sTime.getTime() + " AND time <= " + eTime.getTime() +" AND env_type = '" + envType.toString() + "';", null);
		List<ChangedUserEnv> res = new ArrayList<ChangedUserEnv>();
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(1));
			UserEnv from = gson.fromJson(cur.getString(3), UserEnv.class);
			UserEnv to = gson.fromJson(cur.getString(4), UserEnv.class);

			ChangedUserEnv changedUserEnv = new ChangedUserEnv(time, timezone, envType, from, to);
			Log.i("retrieved changed env", changedUserEnv.toString());
			res.add(changedUserEnv);
		}
		cur.close();
		return res;
	}
}
