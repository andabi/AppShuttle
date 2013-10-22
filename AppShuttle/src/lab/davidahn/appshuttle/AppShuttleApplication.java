package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	private SharedPreferences _preferenceSettings;
	
	public static long launchTime;
	public static SnapshotUserCxt currUserCxt;
	public static Set<UserBhv> recentPredictedBhvSet;

	public AppShuttleApplication(){}

	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
		_preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferenceSettings(){
		return _preferenceSettings;
	}

//	public long getLaunchTime(){
//		return _launchTime;
//	}
	
//	public SnapshotUserCxt getCurrUserCxt() {
//		return _currUserCxt;
//	}
//	public void setCurrUserCxt(SnapshotUserCxt currUserCxt) {
//		_currUserCxt = currUserCxt;
//	}
	
}
