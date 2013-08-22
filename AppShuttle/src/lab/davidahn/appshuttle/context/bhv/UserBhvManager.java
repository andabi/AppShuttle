package lab.davidahn.appshuttle.context.bhv;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserBhvManager {
	private static UserBhvManager userBhvManager;
	private SQLiteDatabase db;
	private Context cxt;
	
	private UserBhvManager(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
		this.cxt = cxt;
	}

	public static UserBhvManager getInstance(Context cxt) {
		if (userBhvManager == null)
			userBhvManager = new UserBhvManager(cxt);
		return userBhvManager;
	}

	public void registerBhv(UserBhv uBhv){
		if(isValid(uBhv) && uBhv.isValid(cxt))
			storeBhv(uBhv);
	}
	
	public void storeBhv(UserBhv uBhv) {
		ContentValues row = new ContentValues();
		row.put("bhv_type", uBhv.getBhvType());
		row.put("bhv_name", uBhv.getBhvName());
//		db.insert("user_bhv", null, row);
		db.insertWithOnConflict("user_bhv", null, row, SQLiteDatabase.CONFLICT_IGNORE);
//		Log.i("stored userBhv", uBhv.toString());
	}
	
	public List<UserBhv> retrieveBhv() {
		Cursor cur = db.rawQuery("SELECT * FROM user_bhv;", null);
		List<UserBhv> res = new ArrayList<UserBhv>();
		
		while (cur.moveToNext()) {
			String bhvType= cur.getString(0);
			String bhvName= cur.getString(1);
			UserBhv uBhv = new UserBhv(bhvType, bhvName);
			res.add(uBhv);
		}
		cur.close();
//		Log.i("retrieved userBhv", res.toString());
		return res;
	}

	public boolean isValid(UserBhv uBhv) {
		if(uBhv.getBhvType().equals("invalid")) 
			return false;
		else 
			return true;
	}
}
