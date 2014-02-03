package lab.davidahn.appshuttle;

import java.util.HashMap;
import java.util.Map;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.predict.PresentBhv;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	public static long launchTime;
	public static long lastPredictionTime;
	public volatile static Map<BaseUserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;
	public volatile static SnapshotUserCxt currUserCxt;
	public volatile static Map<UserBhv, PresentBhv> presentBhvMap;
	public volatile static int numFavoriteNotifiable;

	public AppShuttleApplication(){}

	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		presentBhvMap = new HashMap<UserBhv, PresentBhv>();
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}