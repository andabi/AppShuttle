package lab.davidahn.appshuttle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static DBHelper dbHelper = null;
	
	public static DBHelper getInstance(Context cxt){
		if(dbHelper == null){
			dbHelper = new DBHelper(cxt.getApplicationContext());
		}
		return dbHelper;
	}
	
	private DBHelper(Context cxt) {
		super(cxt, cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE).getString("database.name", new StringBuilder(cxt.getResources().getString(R.string.app_name)).append(".db").toString()), null, 22);
	}
	
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE context (context_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"time INTEGER, timezone TEXT, location TEXT, place TEXT, bhv_type TEXT, bhv_name TEXT);");
		db.execSQL("CREATE TABLE refined_context (context_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"s_time INTEGER, e_time INTEGER, timezone TEXT, locations TEXT, places TEXT, bhv_type TEXT, bhv_name TEXT);");
		db.execSQL("CREATE TABLE matched_context (context_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"time INTEGER, timezone TEXT, location TEXT, place TEXT, bhv_type TEXT, bhv_name TEXT, condition TEXT, likelihood REAL, related_cxt TEXT);");
		db.execSQL("CREATE TABLE user_bhv (bhv_type TEXT, bhv_name TEXT, PRIMARY KEY (bhv_type, bhv_name) );");
		db.execSQL("CREATE TABLE changed_env (time INTEGER, timezone TEXT, env_type TEXT, from_value TEXT, to_value TEXT, PRIMARY KEY (time, timezone, env_type) );");
		db.execSQL("CREATE TABLE duration_user_env (time INTEGER, duration INTEGER, end_time INTEGER, timezone TEXT, env_type TEXT, user_env TEXT, PRIMARY KEY (time, timezone, env_type) );");
		db.execSQL("CREATE TABLE predicted_bhv (context_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"time INTEGER, timezone TEXT, location TEXT, place TEXT, bhv_type TEXT, bhv_name TEXT, score REAL);");

	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS context");
		db.execSQL("DROP TABLE IF EXISTS refined_context");
		db.execSQL("DROP TABLE IF EXISTS matched_context");
		db.execSQL("DROP TABLE IF EXISTS user_bhv");
		db.execSQL("DROP TABLE IF EXISTS changed_env");
		db.execSQL("DROP TABLE IF EXISTS duration_user_env");
		db.execSQL("DROP TABLE IF EXISTS predicted_bhv");
		onCreate(db);
	}
}