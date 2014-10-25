package lab.davidahn.appshuttle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	public static long launchTime;
	public static long lastPredictionTime;
	public static Map<UserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;
	public volatile static SnapshotUserCxt currUserCxt;
	public volatile static Map<UserBhv, PredictedBhv> predictedBhvMap;
	public volatile static Map<UserBhv, PredictedPresentBhv> predictedPresentBhvMap;
	public static List<ViewableUserBhv> lastNotibarBhvs;
	public static int numFavoriteNotifiable;
	public static long lastPredictionLatency;
	public static long maxPredictionLatency;
	
	public AppShuttleApplication(){}

	@Override
	public void onCreate(){
		instance = this;
		launchTime = System.currentTimeMillis();
		durationUserBhvBuilderMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
		currUserCxt = new SnapshotUserCxt();
		predictedBhvMap = new HashMap<UserBhv, PredictedBhv>();
		predictedPresentBhvMap = new HashMap<UserBhv, PredictedPresentBhv>();
		lastNotibarBhvs = null;
	}
	
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
}