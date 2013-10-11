package lab.davidahn.appshuttle.context.bhv;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class UserBhvDao {
	private SQLiteDatabase _db;
	
	private static UserBhvDao userBhvDao = new UserBhvDao();
	private UserBhvDao() {
		_db = AppShuttleDBHelper.getInstance().getWritableDatabase();
	}
	public static UserBhvDao getInstance() {
		return userBhvDao;
	}

	public void storeUserBhv(UserBhv uBhv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		ContentValues row = new ContentValues();
		row.put("bhv_type", uBhv.getBhvType().toString());
		row.put("bhv_name", uBhv.getBhvName());
		row.put("metas", gson.toJson(uBhv.getMetas()));
//		db.insert("user_bhv", null, row);
		_db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_REPLACE);
		Log.i("stored user bhv", uBhv.toString());
	}
	
	public List<UserBhv> retrieveUserBhv() {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		Cursor cur = _db.rawQuery("SELECT * FROM list_user_bhv;", null);
		List<UserBhv> res = new ArrayList<UserBhv>();
		while (cur.moveToNext()) {
			BhvType bhvType= BhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);
			uBhv.setMetas(metas);
			res.add(uBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void deleteUserBhv(UserBhv uBhv) {
		_db.execSQL("" +
				"DELETE " +
				"FROM list_user_bhv " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
}