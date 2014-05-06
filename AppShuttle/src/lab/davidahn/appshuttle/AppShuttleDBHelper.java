package lab.davidahn.appshuttle;

import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhvDao;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.HistoryPresentBhvDao;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppShuttleDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = AppShuttleApplication.getContext().getPreferences().getString("database.name", "AppShuttle.db");
	private static final int DB_VERSION = 44;

	private static AppShuttleDBHelper dbHelper = new AppShuttleDBHelper(AppShuttleApplication.getContext());
	public static AppShuttleDBHelper getInstance() {
		return dbHelper;
	}

	private AppShuttleDBHelper(Context cxt) {
		super(cxt, DB_NAME, null, DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		DurationUserEnvDao.getInstance().createTable();
		DurationUserBhvDao.getInstance().createTable();
		UserBhvDao.getInstance().createTable();
		HistoryPresentBhvDao.getInstance().createTable();
		StatCollector.getInstance().createTable();
	}

	/*
	 * Comments on DB_VERSION
	 * 
	 * 42: history_present_bhv table added.
	 * 43: stat_bhv_transition table added.
	 * 44: 'favorite_order' column added in 'list_user_bhv' table.
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion <= 41)
			HistoryPresentBhvDao.getInstance().createTable();

		if(oldVersion <= 42)
			StatCollector.getInstance().createTable();
		
		if(oldVersion <= 43){
			db.execSQL(String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0", 
					UserBhvDao.tableName, UserBhvDao.columnFavoriteOrder));
		}
	}
}