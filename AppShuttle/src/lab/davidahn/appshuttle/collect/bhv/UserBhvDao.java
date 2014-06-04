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
	public static final String tableName = "list_user_bhv";
	public static final String columnBhvType = "bhv_type";
	public static final String columnBhvName = "bhv_name";
	public static final String columnMetas = "metas";
	public static final String columnBlocked = "blocked";
	public static final String columnBlockedTime = "blocked_time";
	public static final String columnFavorite = "favorates";
	public static final String columnFavoriteTime = "favorates_time";
	public static final String columnIsNotifiable = "is_notifiable";
	public static final String columnFavoriteOrder = "favorite_order";
	
	private SQLiteDatabase db;
	private Gson gson;
	
	private static UserBhvDao userBhvDao = new UserBhvDao();
	private UserBhvDao() {
		db = AppShuttleDBHelper.getInstance().getWritableDatabase();
		gson = new Gson();
	}
	public static UserBhvDao getInstance() {
		return userBhvDao;
	}

	public void createTable() {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ columnBhvType + " TEXT, "
				+ columnBhvName + " TEXT, "
				+ columnMetas + " TEXT, "
				+ columnBlocked + " INTEGER DEFAULT 0, "
				+ columnBlockedTime + " INTEGER DEFAULT 0, "
				+ columnFavorite + " INTEGER DEFAULT 0, "
				+ columnFavoriteTime + " INTEGER DEFAULT 0, "
				+ columnIsNotifiable + " INTEGER DEFAULT 0, "
				+ columnFavoriteOrder + " INTEGER DEFAULT 0, "
				+ "PRIMARY KEY (" + columnBhvType + ", " + columnBhvName + ") "
				+ ");");
	}
	
	public void storeUserBhv(UserBhv uBhv) {
		ContentValues row = new ContentValues();
		row.put(columnBhvType, uBhv.getBhvType().toString());
		row.put(columnBhvName, uBhv.getBhvName());
		row.put(columnMetas, gson.toJson(uBhv.getMetas()));
		db.insertWithOnConflict(tableName, null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		Log.i("stored user bhv", uBhv.toString());
	}
	
	public List<BaseUserBhv> retrieveUserBhv() {
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM " + tableName
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
	
	public void deleteUserBhv(UserBhv uBhv) {
		db.execSQL(
				"DELETE FROM " + tableName +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	public List<FavoriteBhv> retrieveFavoriteUserBhv() {
		Cursor cur = db.rawQuery(
				"SELECT *" +
				" FROM " + tableName +
				" WHERE " + columnFavorite + " = 1"
			, null);
		List<FavoriteBhv> res = new ArrayList<FavoriteBhv>();
		while (cur.moveToNext()) {
			UserBhvType bhvType= UserBhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			Type listType = new TypeToken<HashMap<String, Object>>(){}.getType();
			Map<String, Object> metas = gson.fromJson(cur.getString(2), listType);
			long setTime = cur.getLong(6);
			boolean isNotifiable = (cur.getInt(7) == 1) ? true : false;
			int order = cur.getInt(8);
			
			UserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
			((BaseUserBhv)uBhv).setMetas(metas);
			FavoriteBhv favoriteUserBhv = new FavoriteBhv(uBhv, setTime, isNotifiable, order);
			res.add(favoriteUserBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}
	
	public void favorite(FavoriteBhv uBhv) {
		int notifiable = (uBhv.isNotifiable()) ? 1 : 0;
		int order = uBhv.getOrder();
		
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnFavorite + " = 1, "
						+ columnFavoriteTime + " = " + uBhv.getSetTime() + " , "
						+ columnIsNotifiable + " = " + notifiable + " , "
						+ columnFavoriteOrder + " = " + order + " " +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	
	public void unfavorite(FavoriteBhv uBhv) {
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnFavorite + " = 0, "
						+ columnFavoriteTime + " = 0, "
						+ columnIsNotifiable + " = 0, "
						+ columnFavoriteOrder + " = 0" +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	
	public void updateNotifiable(FavoriteBhv uBhv) {
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnIsNotifiable + " = 1" +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	
	public void updateUnNotifiable(FavoriteBhv uBhv) {
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnIsNotifiable + " = 0" +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	
	public void updateOrder(FavoriteBhv uBhv, int order) {
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnFavoriteOrder + " = " + order +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	
	public List<BlockedBhv> retrieveBlockedUserBhv() {
	//		int isBlockedInt = (isBlocked) ? 1 : 0;
			Cursor cur = db.rawQuery(
					"SELECT *" +
					" FROM " + tableName +
					" WHERE " + columnBlocked + " = 1"
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
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnBlocked + " = 1, "
						+ columnBlockedTime + " = " + uBhv.getBlockedTime() + 
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
	public void unblock(BlockedBhv uBhv) {
		db.execSQL(
				"UPDATE " + tableName +
				" SET " + columnBlocked + " = 0" +
				" WHERE " + columnBhvType + " = '" + uBhv.getBhvType() + "'" +
					" AND " + columnBhvName + " = '" + uBhv.getBhvName() + "';");
	}
}