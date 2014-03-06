package lab.davidahn.appshuttle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	public static long launchTime;
	public volatile static long lastPredictionTime;
	public volatile static Map<BaseUserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;
	public volatile static SnapshotUserCxt currUserCxt;
	public volatile static Map<UserBhv, PredictedBhv> predictedBhvMap;
	public volatile static Map<UserBhv, PredictedPresentBhv> predictedPresentBhvMap;
	public volatile static int numFavoriteNotifiable;
	
	public AppShuttleApplication(){}

	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		predictedBhvMap = new ConcurrentHashMap<UserBhv, PredictedBhv>();
		predictedPresentBhvMap = new ConcurrentHashMap<UserBhv, PredictedPresentBhv>();
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}