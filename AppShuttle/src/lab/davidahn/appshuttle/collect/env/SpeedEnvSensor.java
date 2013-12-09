package lab.davidahn.appshuttle.collect.env;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.util.Log;

public class SpeedEnvSensor extends BaseEnvSensor {
	private LocEnvSensor locEnvSensor;
	private UserSpeed prevUSpeed, currUSpeed;
    private DurationUserEnv.Builder durationUserEnvBuilder;
    
    private static SpeedEnvSensor speedEnvSensor = new SpeedEnvSensor();

    private SpeedEnvSensor(){
    	super();
    	locEnvSensor = LocEnvSensor.getInstance();
    	prevUSpeed = currUSpeed = UserSpeed.create(0.0);
	}
	
	public static SpeedEnvSensor getInstance(){
		return speedEnvSensor;
	}
	
	@Override
	public UserSpeed sense(Date currTimeDate, TimeZone currTimeZone){
		if(!locEnvSensor.isChanged()) {
//			Log.d("speed", "not sensed yet");
			return currUSpeed;
		}
		
		prevUSpeed = currUSpeed;
		
		UserLoc currLoc = locEnvSensor.getCurrULoc();
		UserLoc prevLoc =  locEnvSensor.getPrevULoc();

		if(!currLoc.isValid() || !prevLoc.isValid()){
			currUSpeed = InvalidUserSpeed.getInstance();
			Log.d("speed", "sensing failure: invalidUserLoc");
			return currUSpeed;
		}
		
		Date lastSensedTimeDate;
		if(durationUserEnvBuilder == null)
			lastSensedTimeDate = currTimeDate;
		else
			lastSensedTimeDate = durationUserEnvBuilder.getTimeDate();
		
		try {
			double speed = currLoc.distanceTo(prevLoc) / (currTimeDate.getTime() - lastSensedTimeDate.getTime()) * 1000;
			currUSpeed = UserSpeed.create(speed);
			
			Log.i("speed", "sensed: " + currUSpeed.toString());
		} catch (InvalidUserEnvException e) {
			Log.d("speed", "sensing failure: invalidUserLoc");
			return currUSpeed;
		}
			
		lastSensedTimeDate = currTimeDate;
		
		return currUSpeed;
	}
	
	@Override
	public boolean isChanged(){
		if(!currUSpeed.equals(prevUSpeed)) {
			Log.i("user env", "user speed changed");
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone);
		} else {
			if(locEnvSensor.isChanged()){
				res = durationUserEnvBuilder.setEnvType(uEnv.getEnvType()).setUserEnv(uEnv).setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone);
			} else {
				if(currUSpeed.getLevel() == UserSpeed.Level.VEHICLE && isAutoExtractionTime(currTimeDate, currTimeZone)){
					res = durationUserEnvBuilder.setEnvType(uEnv.getEnvType()).setUserEnv(uEnv).setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
					durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone);
					currUSpeed = UserSpeed.create(0.0);
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
		long minAcceptableSpeedMph = 5000;
		long minWaitingTime = preferenceSettings.getInt("collection.location.tolerance.distance", 500) * (1000 * 60 * 60 / minAcceptableSpeedMph);
		long reasonableWaitingTime = (long)(1.5 * minWaitingTime);

		Date lastSensedTimeDate = durationUserEnvBuilder.getTimeDate();
		
		if(currTimeDate.getTime() - lastSensedTimeDate.getTime() > reasonableWaitingTime) {
			Log.d("speed", "Auto extracted by exceeding reasonable waiting time");
			return true;
		}
		
		return false;
	}

	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone) {
		return new DurationUserEnv.Builder()
		.setTime(currTimeDate)
		.setEndTime(currTimeDate)
		.setTimeZone(currTimeZone);
	}
}
