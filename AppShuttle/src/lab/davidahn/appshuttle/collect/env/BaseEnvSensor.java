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

	protected UserEnv prevEnv, currEnv;
	protected DurationUserEnv.Builder durationUserEnvBuilder;

	public BaseEnvSensor(){
		cxt = AppShuttleApplication.getContext();
		preferenceSettings = cxt.getPreferences();
	}
	
	public UserEnv getCurrEnv() {
		return currEnv;
	}
	public UserEnv getPrevEnv() {
		return prevEnv;
	}
	
	@Override
	public UserEnv senseAndGet(long currTime, TimeZone currTimeZone){
		prevEnv = currEnv;
		currEnv = sense(currTime, currTimeZone);
		return currEnv;
	}
	
	protected UserEnv sense(long currTime, TimeZone currTimeZone) {
		return null;
	}

	@Override
	public boolean isChanged(){
		if(!currEnv.equals(prevEnv)) return true;
		else return false;
	}

	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(long currTime, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv env) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
			durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, env);
		} else {
			if(isChanged() || isAutoExtractionTime(currTime, currTimeZone)){
				durationUserEnvBuilder = setEndTimeForDurationUserEnvBuilder(durationUserEnvBuilder, currTime);
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
				durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, env);
			}
		}
		return res;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(long currTime, TimeZone currTimeZone) {
		DurationUserEnv res = durationUserEnvBuilder.setEndTime(currTime).setTimeZone(currTimeZone).build();
		durationUserEnvBuilder = null;
		return res;
	}
	
	protected boolean isAutoExtractionTime(long currTime, TimeZone currTimeZone){
		long duration = preferenceSettings.getLong("collection.common.auto_extraction_duration", AlarmManager.INTERVAL_HOUR);
		long lastSensedTime = durationUserEnvBuilder.getTime();
		if(currTime - lastSensedTime > duration) return true;
		else return false;
	}
	
//	protected boolean isAutoExtractionTime(long currTime, TimeZone currTimeZone){
//		long duration = preferenceSettings.getLong("collection.common.auto_extraction_duration", AlarmManager.INTERVAL_HOUR);
//		long collectionPeriod = preferenceSettings.getLong("collection.env.period", 300000);
//		
//		if(currTime % duration < collectionPeriod)
//			return true;
//		
//		return false;
//	}
	
	protected DurationUserEnv.Builder createDurationUserEnvBuilder(long currTime, TimeZone currTimeZone) {
		return new DurationUserEnv.Builder()
			.setTime(currTime)
			.setEndTime(currTime)
			.setTimeZone(currTimeZone);
	}
	
	protected DurationUserEnv.Builder setEnvForDurationUserEnvBuilder(DurationUserEnv.Builder builder, UserEnv uEnv) {
		return builder.setUserEnv(uEnv);
	}
	

	protected DurationUserEnv.Builder setEndTimeForDurationUserEnvBuilder(DurationUserEnv.Builder builder, long endTime) {
		return builder.setEndTime(endTime);
	}
}
