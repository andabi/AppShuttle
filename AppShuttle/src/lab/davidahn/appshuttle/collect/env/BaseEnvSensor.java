package lab.davidahn.appshuttle.collect.env;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.AlarmManager;
import android.content.SharedPreferences;

public abstract class BaseEnvSensor implements EnvSensor {
	protected AppShuttleApplication cxt;
	protected SharedPreferences preferenceSettings;

	public BaseEnvSensor(){
		cxt = AppShuttleApplication.getContext();
		preferenceSettings = cxt.getPreferences();
//		preferenceSettings = ((AppShuttleApplication)cxt.getApplicationContext()).getPreferenceSettings();
	}
	
	@Override
	public UserEnv sense(Date currTimeDate, TimeZone currTimeZone) {
		return null;
	}
	
	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return null;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return null;
	}
	
	@Override
	public abstract boolean isChanged();

	public boolean isAutoExtractionTime(Date currTimeDate, TimeZone currTimeZone){
		long autoStoreMaxDuration = preferenceSettings.getLong("collection.common.auto_store.max_duration", AlarmManager.INTERVAL_HOUR);
		long collectionPeriod = preferenceSettings.getLong("collection.env.period", 120000);
		
		if(currTimeDate.getTime() % autoStoreMaxDuration < collectionPeriod)
			return true;
		
		return false;
	}
}
