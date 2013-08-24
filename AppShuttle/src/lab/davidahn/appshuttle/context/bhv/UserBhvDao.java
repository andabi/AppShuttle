package lab.davidahn.appshuttle.context.bhv;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserBhvDao {
	private static UserBhvDao userBhvDao;
	private SQLiteDatabase db;
//	private Context cxt;
	
	private UserBhvDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
//		this.cxt = cxt;
	}

	public static UserBhvDao getInstance(Context cxt) {
		if (userBhvDao == null)
			userBhvDao = new UserBhvDao(cxt);
		return userBhvDao;
	}

	public void storeUserBhv(UserBhv uBhv) {
		ContentValues row = new ContentValues();
		row.put("bhv_type", uBhv.getBhvType().toString());
		row.put("bhv_name", uBhv.getBhvName());
//		db.insert("user_bhv", null, row);
		db.insertWithOnConflict("user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		Log.i("stored userBhv", uBhv.toString());
	}
	
	public List<UserBhv> retrieveUserBhv() {
		Cursor cur = db.rawQuery("SELECT * FROM user_bhv;", null);
		List<UserBhv> res = new ArrayList<UserBhv>();
		
		while (cur.moveToNext()) {
			BhvType bhvType= BhvType.valueOf(cur.getString(0));
			String bhvName= cur.getString(1);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);
			res.add(uBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}

//	public boolean isValid(UserBhv uBhv) {
//		if(uBhv.getBhvType().equals("invalid")) 
//			return false;
//		else 
//			return true;
//	}
}
