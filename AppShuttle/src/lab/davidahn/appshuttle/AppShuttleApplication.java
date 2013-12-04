package lab.davidahn.appshuttle;

import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	public static long launchTime;
//	public static boolean isPredictionServiceRunning;
	public static long lastPredictionTime;
	public static SnapshotUserCxt currUserCxt;
	public static Map<UserBhv, PredictionInfo> recentPredictionInfoMap ;
	public static int numFavoratesNotifiable;

	public AppShuttleApplication(){}

	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
//		isPredictionServiceRunning = false;
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}
