package lab.davidahn.appshuttle.collect.env;

import java.util.Collections;
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
	public UserEnv sense(long currTime, TimeZone currTimeZone) {
		return null;
	}
	
	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(long currTime, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv uEnv) {
		return null;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(long currTime, TimeZone currTimeZone) {
		return null;
	}
	
	@Override
	public abstract boolean isChanged();

	public boolean isAutoExtractionTime(long currTime, TimeZone currTimeZone){
		long autoStoreMaxDuration = preferenceSettings.getLong("collection.common.auto_extraction_duration", AlarmManager.INTERVAL_HOUR);
		long collectionPeriod = preferenceSettings.getLong("collection.env.period", 120000);
		
		if(currTime % autoStoreMaxDuration < collectionPeriod)
			return true;
		
		return false;
	}
}
