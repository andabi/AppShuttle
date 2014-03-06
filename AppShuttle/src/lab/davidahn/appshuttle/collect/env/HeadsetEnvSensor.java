package lab.davidahn.appshuttle.collect.env;

import java.util.Date;
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
	public HeadsetEnv sense(Date currTimeDate, TimeZone currTimeZone){
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
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv env) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, env);
		} else {
			if(headsetEnvSensor.isChanged()){
				res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, env);
			} else {
				if((currHeadsetEnv.isPlugged() && isAutoExtractionTime(currTimeDate, currTimeZone))){
					res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
					durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, env);
				}
			}
		}
		return res;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		durationUserEnvBuilder = null;
		return null;
	}
	
	@Override
	public boolean isAutoExtractionTime(Date currTimeDate, TimeZone currTimeZone){
		long duration = preferenceSettings.getLong("collection.headset.auto_extraction_duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES);
		Date lastSensedTimeDate = durationUserEnvBuilder.getTimeDate();
		if(currTimeDate.getTime() - lastSensedTimeDate.getTime() > duration)
			return true;
		else return false;
	}

	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone, UserEnv env) {
		return new DurationUserEnv.Builder()
		.setTime(currTimeDate)
		.setEndTime(currTimeDate)
		.setTimeZone(currTimeZone)
		.setEnvType(env.getEnvType())
		.setUserEnv(env);
	}
}
