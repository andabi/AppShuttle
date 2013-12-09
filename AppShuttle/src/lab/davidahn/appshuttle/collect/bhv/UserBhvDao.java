package lab.davidahn.appshuttle.collect.bhv;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleDBHelper;
import lab.davidahn.appshuttle.view.BlockedUserBhv;
import lab.davidahn.appshuttle.view.FavoratesUserBhv;
import lab.davidahn.appshuttle.view.OrdinaryUserBhv;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
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

	public void storeUserBhv(OrdinaryUserBhv uBhv) {
//		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		Gson gson = new Gson();

		ContentValues row = new ContentValues();
		row.put("bhv_type", uBhv.getBhvType().toString());
		row.put("bhv_name", uBhv.getBhvName());
		row.put("metas", gson.toJson(((BaseUserBhv)uBhv.getUserBhv()).getMetas()));
//		db.insert("user_bhv", null, row);
		_db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		db.insertWithOnConflict("list_user_bhv", null, row, SQLiteDatabase.CONFLICT_REPLACE);
//		Log.i("stored user bhv", uBhv.toString());
	}
	
	public List<OrdinaryUserBhv> retrieveOrdinaryUserBhv() {
		Gson gson = new Gson();

		Cursor cur = _db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE blocked = 0 " +
					"AND favorates = 0"
				, null);
		List<OrdinaryUserBhv> res = new ArrayList<OrdinaryUserBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);

			BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			uBhv.setMetas(metas);
			OrdinaryUserBhv ordinaryUBhv = new OrdinaryUserBhv(uBhv);
			res.add(ordinaryUBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public List<BlockedUserBhv> retrieveBlockedUserBhv() {
		Gson gson = new Gson();

//		int isBlockedInt = (isBlocked) ? 1 : 0;
		Cursor cur = _db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE blocked = 1"
				, null);
		List<BlockedUserBhv> res = new ArrayList<BlockedUserBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			long blocked_time = cur.getLong(4);
			
			BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			uBhv.setMetas(metas);
			BlockedUserBhv blockedUBhv = new BlockedUserBhv(uBhv, blocked_time);
			res.add(blockedUBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void block(BlockedUserBhv uBhv) {
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET blocked = 1, blocked_time = " + uBhv.getBlockedTime() + " " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void unblock(BlockedUserBhv uBhv) {
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET blocked = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public List<FavoratesUserBhv> retrieveFavoratesUserBhv() {
		Gson gson = new Gson();
		Cursor cur = _db.rawQuery(
				"SELECT * " +
				"FROM list_user_bhv " +
				"WHERE favorates = 1"
				, null);
		List<FavoratesUserBhv> res = new ArrayList<FavoratesUserBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			long setTime = cur.getLong(6);
			boolean isNotifiable = (cur.getInt(7) == 1) ? true : false;
			
			UserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			((BaseUserBhv)uBhv).setMetas(metas);
			FavoratesUserBhv favoratesUserBhv = new FavoratesUserBhv(uBhv, setTime, isNotifiable);
//			if(isNotifiable)
//				favoratesUserBhv.trySetNotifiable();
//				FavoratesUserBhv.trySetNotifiable(favoratesUserBhv);
			res.add(favoratesUserBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void favorates(FavoratesUserBhv uBhv) {
		int notifiable = (uBhv.isNotifiable()) ? 1 : 0;
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET favorates = 1, favorates_time = " + uBhv.getSetTime() + " , is_notifiable = " + notifiable + " " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void unfavorates(FavoratesUserBhv uBhv) {
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET favorates = 0, is_notifiable = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void updateNotifiable(FavoratesUserBhv uBhv) {
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET is_notifiable = 1 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void updateUnNotifiable(FavoratesUserBhv uBhv) {
		_db.execSQL("" +
				"UPDATE list_user_bhv " +
				"SET is_notifiable = 0 " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
	
	public void deleteUserBhv(UserBhv uBhv) {
		_db.execSQL("" +
				"DELETE " +
				"FROM list_user_bhv " +
				"WHERE bhv_type = '" + uBhv.getBhvType() + "' " +
						"AND bhv_name = '" + uBhv.getBhvName() +"';");
	}
}