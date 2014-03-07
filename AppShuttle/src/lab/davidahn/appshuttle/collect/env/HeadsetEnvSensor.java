package lab.davidahn.appshuttle.collect.env;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.content.Context;
import android.media.AudioManager;

public class HeadsetEnvSensor extends BaseEnvSensor {
	private AudioManager audioManager;
	private HeadsetEnv prevHeadsetEnv, currHeadsetEnv;
    private DurationUserEnv.Builder durationUserEnvBuilder;
    
    private static HeadsetEnvSensor headsetEnvSensor = new HeadsetEnvSensor();

    public static HeadsetEnvSensor getInstance(){
    	return headsetEnvSensor;
    }

    private HeadsetEnvSensor(){
    	super();
    	audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
    	prevHeadsetEnv = currHeadsetEnv = HeadsetEnv.getInstance(false);
	}
	
	@Override
	public HeadsetEnv sense(long currTime, TimeZone currTimeZone){
		prevHeadsetEnv = currHeadsetEnv;
		currHeadsetEnv =  HeadsetEnv.getInstance(isHeadsetPlugged());
//		Log.d("HeadsetEnvSensor", "isPlugged: " + isPlugged);
		return currHeadsetEnv;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isHeadsetPlugged(){
		return audioManager.isWiredHeadsetOn();
	}
	
	@Override
	public boolean isChanged(){
		if(!currHeadsetEnv.equals(prevHeadsetEnv)) return true;
		else return false;
	}
	
	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv env) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTime, currTimeZone, env);
		} else {
			if(headsetEnvSensor.isChanged() || isAutoExtractionTime(currTime, currTimeZone)){
				res = durationUserEnvBuilder.setEndTime(currTime).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTime, currTimeZone, env);
			}
		}
		return res;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(long currTime, TimeZone currTimeZone) {
		durationUserEnvBuilder = null;
		return null;
	}
	
	@Override
	public boolean isAutoExtractionTime(long currTime, TimeZone currTimeZone){
		long duration = preferenceSettings.getLong("collection.common.auto_extraction_duration", AlarmManager.INTERVAL_HOUR);
		if(currHeadsetEnv.isPlugged())
			duration = preferenceSettings.getLong("collection.headset.auto_extraction_duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES);
		long lastSensedTime = durationUserEnvBuilder.getTime();
		if(currTime - lastSensedTime > duration)
			return true;
		else return false;
	}

	private DurationUserEnv.Builder makeDurationUserEnvBuilder(long currTime, TimeZone currTimeZone, UserEnv env) {
		return new DurationUserEnv.Builder()
		.setTime(currTime)
		.setEndTime(currTime)
		.setTimeZone(currTimeZone)
		.setEnvType(env.getEnvType())
		.setUserEnv(env);
	}
}
