package lab.davidahn.appshuttle;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	private SharedPreferences _preferenceSettings;
	
	private long _launchTime;
	private SnapshotUserCxt _currUserCxt;
	
	
	public AppShuttleApplication(){
		instance = this;
		_launchTime = System.currentTimeMillis();
	}
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferenceSettings(){
		if(_preferenceSettings == null)
			_preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		return _preferenceSettings;
	}

	public long getLaunchTime(){
		return _launchTime;
	}
	
	public SnapshotUserCxt getCurrUserCxt() {
		return _currUserCxt;
	}
	public void setCurrUserCxt(SnapshotUserCxt currUserCxt) {
		_currUserCxt = currUserCxt;
	}
}
