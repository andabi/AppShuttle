package lab.davidahn.appshuttle.context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserCxtDao {
	private static UserCxtDao userCxtDao;
	private SQLiteDatabase db;

	private UserCxtDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public static UserCxtDao getInstance(Context cxt) {
		if (userCxtDao == null)
			userCxtDao = new UserCxtDao(cxt);
		return userCxtDao;
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
			row.put("bhv_type", uBhv.getBhvType().toString());
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
			BhvType bhvType= BhvType.valueOf(cur.getString(4));
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
}
