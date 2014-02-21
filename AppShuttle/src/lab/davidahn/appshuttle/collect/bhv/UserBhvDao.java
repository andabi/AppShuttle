package lab.davidahn.appshuttle.collect.bhv;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserBhvDao {
	private SQLiteDatabase db;
	
	private static UserBhvDao userBhvDao = new UserBhvDao();
	private UserBhvDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
	}
	public static UserBhvDao getInstance() {
		return userBhvDao;
	}

	public void storeUserBhv(BaseUserBhv uBhv) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		Gson gson = new Gson();

		ContentValues row = new ContentValues();
		row.put("bhv_type", uBhv.getBhvType().toString());
		row.put("bhv_name", uBhv.getBhvName());
		row.put("metas", gson.toJson(uBhv.getMetas()));
//		db.insert("user_bhv", null, row);
		db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.i("stored user bhv", uBhv.toString());
	}
	
	public List<BaseUserBhv> retrieveNormalUserBhv() {
		Gson gson = new Gson();

		Cursor cur = db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE blocked = 0 " +
					"AND favorates = 0"
				, null);
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);

			BaseUserBhv uBhv = BaseUserBhv.create(bhvType, bhvName);
			uBhv.setMetas(metas);
			res.add(uBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public List<BlockedBhv> retrieveBlockedUserBhv() {
		Gson gson = new Gson();

//		int isBlockedInt = (isBlocked) ? 1 : 0;
		Cursor cur = db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE blocked = 1"
				, null);
		List<BlockedBhv> res = new ArrayList<BlockedBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			long blocked_time = cur.getLong(4);
			
			BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			uBhv.setMetas(metas);
			BlockedBhv blockedUBhv = new BlockedBhv(uBhv, blocked_time);
			res.add(blockedUBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void block(BlockedBhv uBhv) {
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET blocked = 1, blocked_time = " + uBhv.getBlockedTime() + " " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void unblock(BlockedBhv uBhv) {
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET blocked = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public List<FavoriteBhv> retrieveFavoriteUserBhv() {
		Gson gson = new Gson();
		Cursor cur = db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE favorates = 1"
				, null);
		List<FavoriteBhv> res = new ArrayList<FavoriteBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			long setTime = cur.getLong(6);
			boolean isNotifiable = (cur.getInt(7) == 1) ? true : false;
			
			UserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			((BaseUserBhv)uBhv).setMetas(metas);
			FavoriteBhv favoriteUserBhv = new FavoriteBhv(uBhv, setTime, isNotifiable);
			res.add(favoriteUserBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void favorite(FavoriteBhv uBhv) {
		int notifiable = (uBhv.isNotifiable()) ? 1 : 0;
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET favorates = 1, favorates_time = " + uBhv.getSetTime() + " , is_notifiable = " + notifiable + " " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void unfavorite(FavoriteBhv uBhv) {
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET favorates = 0, is_notifiable = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void updateNotifiable(FavoriteBhv uBhv) {
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET is_notifiable = 1 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void updateUnNotifiable(FavoriteBhv uBhv) {
		db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET is_notifiable = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void deleteUserBhv(UserBhv uBhv) {
		db.execSQL("" +
				"DELETE " +
				"FROM list_user_bhv " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
}