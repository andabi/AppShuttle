package lab.davidahn.appshuttle;

import java.util.HashMap;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	public static long launchTime;
	public static Map<BaseUserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;
	public static long lastPredictionTime;
	public static SnapshotUserCxt currUserCxt;
	public static Map<UserBhv, PredictionInfo> currentPredictionInfoByUserBhv;
	public static Map<UserBhv, PredictedBhv> predictedBhvs;
	public static int numFavoriteNotifiable;

	public AppShuttleApplication(){}

	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		currentPredictionInfoByUserBhv = new HashMap<UserBhv, PredictionInfo>();
		predictedBhvs = new HashMap<UserBhv, PredictedBhv>();
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}
