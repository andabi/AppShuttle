package lab.davidahn.appshuttle.collect.env;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.content.Context;
import android.media.AudioManager;

public class HeadsetEnvSensor extends BaseEnvSensor {
	private AudioManager audioManager;
    
    private static HeadsetEnvSensor headsetEnvSensor = new HeadsetEnvSensor();

    public static HeadsetEnvSensor getInstance(){
    	return headsetEnvSensor;
    }

    private HeadsetEnvSensor(){
    	super();
    	audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
    	prevEnv = currEnv = HeadsetEnv.getInstance(false);
	}
	
	@Override
	public HeadsetEnv sense(long currTime, TimeZone currTimeZone){
//		Log.d("HeadsetEnvSensor", "isPlugged: " + isPlugged);
		return HeadsetEnv.getInstance(isHeadsetPlugged());
	}
	
	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv env) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
			durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, env);
		} else {
			if(isChanged()
					|| (!((HeadsetEnv)currEnv).isPlugged() && isAutoExtractionTime(currTime, currTimeZone)) 
					|| ((HeadsetEnv)currEnv).isPlugged() && isAutoExtractionTimeHeadsetOn(currTime, currTimeZone)) {
				durationUserEnvBuilder = setEndTimeForDurationUserEnvBuilder(durationUserEnvBuilder, currTime);
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
				durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, env);
			}
		}
		return res;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isHeadsetPlugged(){
		return audioManager.isWiredHeadsetOn();
	}
	
	protected boolean isAutoExtractionTimeHeadsetOn(long currTime, TimeZone currTimeZone){
		long duration = preferenceSettings.getLong("collection.headset.auto_extraction_duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES);
		long lastSensedTime = durationUserEnvBuilder.getTime();
		if(currTime - lastSensedTime > duration) return true;
		else return false;
	}
}
