package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.AlarmManager;
import android.content.SharedPreferences;

public abstract class BaseEnvSensor implements EnvSensor {
	protected AppShuttleApplication _appShuttleContext;
	protected SharedPreferences _preferenceSettings;

	public BaseEnvSensor(){
		_appShuttleContext = AppShuttleApplication.getContext();
		_preferenceSettings = _appShuttleContext.getPreferences();
//		preferenceSettings = ((AppShuttleApplication)cxt.getApplicationContext()).getPreferenceSettings();
	}
	
	public UserEnv sense() {
		return null;
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return null;
	}
	
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return null;
	}
	
	public abstract boolean isChanged();

	public boolean isAutoExtractionTime(Date currTimeDate, TimeZone currTimeZone){
		long autoStoreMaxDuration = _preferenceSettings.getLong("collection.common.auto_store.max_duration", AlarmManager.INTERVAL_HOUR);
		long collectionPeriod = _preferenceSettings.getLong("service.collection.period", 30000);
		
		if(currTimeDate.getTime() % autoStoreMaxDuration < collectionPeriod)
			return true;
		
		return false;
	}
}
