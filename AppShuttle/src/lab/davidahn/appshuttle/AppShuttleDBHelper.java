package lab.davidahn.appshuttle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppShuttleDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = AppShuttleApplication.getContext().getSharedPreferences(
			"AppShuttle", Context.MODE_PRIVATE).getString("database.name", 
					new StringBuilder(AppShuttleApplication.getContext().getResources().getString(R.string.app_name)).append(".db").toString());

	private static AppShuttleDBHelper dbHelper = new AppShuttleDBHelper(AppShuttleApplication.getContext());
	public static AppShuttleDBHelper getInstance() {
		return dbHelper;
	}

	private AppShuttleDBHelper(Context cxt) {
		super(cxt, DB_NAME, null, 30);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS history_user_env ("
				+ "time INTEGER, duration INTEGER, end_time INTEGER, timezone TEXT, env_type TEXT, user_env TEXT, "
				+ "PRIMARY KEY (time, timezone, env_type) " + ");");

		db.execSQL("CREATE TABLE IF NOT EXISTS history_user_bhv ("
				+ "time INTEGER, duration INTEGER, end_time INTEGER, timezone TEXT, bhv_type TEXT, bhv_name TEXT, "
				+ "PRIMARY KEY (time, timezone, bhv_type, bhv_name) " + ");");

		db.execSQL("CREATE TABLE IF NOT EXISTS list_user_bhv ("
				+ "bhv_type TEXT, bhv_name TEXT, metas TEXT, "
				+ "PRIMARY KEY (bhv_type, bhv_name) " + ");");

		db.execSQL("CREATE TABLE IF NOT EXISTS matched_result ("
				+ "time INTEGER, timezone TEXT, bhv_type TEXT, bhv_name TEXT, matcher_type TEXT, likelihood REAL, inverse_entropy REAL, "
				+ "PRIMARY KEY (time, timezone, bhv_type, bhv_name, matcher_type)"
				+ ");");

		db.execSQL("CREATE TABLE IF NOT EXISTS predicted_bhv ("
				+ "time INTEGER, timezone TEXT, user_envs TEXT, bhv_type TEXT, bhv_name TEXT, score REAL, "
				+ "PRIMARY KEY (time, timezone, bhv_type, bhv_name)" + ");");

		// db.execSQL("CREATE TABLE IF NOT EXISTS snapshot_context (time INTEGER, timezone TEXT, user_envs TEXT, bhv_type TEXT, bhv_name TEXT, "
		// +
		// "PRIMARY KEY (time, timezone, bhv_type, bhv_name) );");

	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS history_user_env");
		db.execSQL("DROP TABLE IF EXISTS history_user_bhv");
		db.execSQL("DROP TABLE IF EXISTS list_user_bhv");
		db.execSQL("DROP TABLE IF EXISTS matched_result");
		db.execSQL("DROP TABLE IF EXISTS predicted_bhv");
		// db.execSQL("DROP TABLE IF EXISTS snapshot_context");
		onCreate(db);
	}
}
