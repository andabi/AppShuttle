package lab.davidahn.appshuttle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppShuttleDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = AppShuttleApplication.getContext().getPreferences().getString("database.name", "AppShuttle.db");
	private static final int DB_VERSION = 41;
	
	private static AppShuttleDBHelper dbHelper = new AppShuttleDBHelper(AppShuttleApplication.getContext());
	public static AppShuttleDBHelper getInstance() {
		return dbHelper;
	}

	private AppShuttleDBHelper(Context cxt) {
		super(cxt, DB_NAME, null, DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS history_user_env ("
				+ "time INTEGER, duration INTEGER, end_time INTEGER, timezone TEXT, env_type TEXT, user_env TEXT, "
				+ "PRIMARY KEY (time, timezone, env_type) " + ");");
		db.execSQL("CREATE INDEX idx1_history_user_env on history_user_env (time)");
		db.execSQL("CREATE INDEX idx2_history_user_env on history_user_env (time, end_time, env_type)");

		db.execSQL("CREATE TABLE IF NOT EXISTS history_user_bhv ("
				+ "time INTEGER, duration INTEGER, end_time INTEGER, timezone TEXT, bhv_type TEXT, bhv_name TEXT, "
				+ "PRIMARY KEY (time, timezone, bhv_type, bhv_name) " + ");");		
		db.execSQL("CREATE INDEX idx1_history_user_bhv on history_user_bhv (time)");
		db.execSQL("CREATE INDEX idx2_history_user_bhv on history_user_bhv (time, bhv_type, bhv_name)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS list_user_bhv ("
				+ "bhv_type TEXT, bhv_name TEXT, metas TEXT, blocked INTEGER DEFAULT 0, blocked_time INTEGER DEFAULT 0, favorates INTEGER DEFAULT 0, favorates_time INTEGER DEFAULT 0, is_notifiable INTEGER DEFAULT 0, "
				+ "PRIMARY KEY (bhv_type, bhv_name) " + ");");
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
}
