package lab.davidahn.appshuttle;

import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhv;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	private SharedPreferences _preferenceSettings;
	
	public static long launchTime;
	public static SnapshotUserCxt currUserCxt;
//	public static Set<BaseUserBhv> recentPredictedBhvSet;
	public static List<PredictedBhv> recentPredictedBhvList ;

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

//	public List<PredictedBhvInfo> getRecentPredictedBhvInfoList() {
//		return _recentPredictedBhvInfoList;
//	}
//
//	public void setRecentPredictedBhvInfoList(List<PredictedBhvInfo> recentPredictedBhvInfoList) {
//		_recentPredictedBhvInfoList = recentPredictedBhvInfoList;
//	}
	
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
