package lab.davidahn.appshuttle.collect.env;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
	public UserSpeed sense(long currTime, TimeZone currTimeZone){
		if(!locEnvSensor.isChanged()) {
//			Log.d("speed", "not sensed yet");
			return currUSpeed;
		}
		
		prevUSpeed = currUSpeed;
		
		UserLoc currLoc = locEnvSensor.getCurrULoc();
		UserLoc prevLoc =  locEnvSensor.getPrevULoc();

		if(!currLoc.isValid() || !prevLoc.isValid()){
			currUSpeed = InvalidUserSpeed.getInstance();
//			Log.d("speed", "sensing failure: invalidUserLoc");
			return currUSpeed;
		}
		
		long lastSensedTime;
		if(durationUserEnvBuilder == null)
			lastSensedTime = currTime;
		else
			lastSensedTime = durationUserEnvBuilder.getTime();
		
		try {
			double speed = currLoc.distanceTo(prevLoc) / (currTime - lastSensedTime) * 1000;
			currUSpeed = UserSpeed.create(speed);
			
//			Log.i("speed", "sensed: " + currUSpeed.toString());
		} catch (InvalidUserEnvException e) {
//			Log.d("speed", "sensing failure: invalidUserLoc");
			return currUSpeed;
		}
			
		lastSensedTime = currTime;
		
		return currUSpeed;
	}
	
	@Override
	public boolean isChanged(){
		if(!currUSpeed.equals(prevUSpeed)) {
//			Log.i("user env", "user speed changed");
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(long currTime,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTime, currTimeZone);
		} else {
			if(locEnvSensor.isChanged()){
				res = durationUserEnvBuilder.setEnvType(uEnv.getEnvType()).setUserEnv(uEnv).setEndTime(currTime).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTime, currTimeZone);
			} else {
				if(currUSpeed.getLevel() == UserSpeed.Level.VEHICLE && isAutoExtractionTime(currTime, currTimeZone)){
					res = durationUserEnvBuilder.setEnvType(uEnv.getEnvType()).setUserEnv(uEnv).setEndTime(currTime).setTimeZone(currTimeZone).build();
					durationUserEnvBuilder = makeDurationUserEnvBuilder(currTime, currTimeZone);
					currUSpeed = UserSpeed.create(0.0);
				}
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
		long minAcceptableSpeedKmph = 5;
		long minWaitingTime = (preferenceSettings.getInt("collection.env.location.tolerance.distance", 500) / 1000)
				/ minAcceptableSpeedKmph
				* (1000 * 60 * 60); //6min
//		long reasonableWaitingTime = (long)(1.2 * minWaitingTime);

		long lastSensedTime = durationUserEnvBuilder.getTime();
		
		if(currTime - lastSensedTime > minWaitingTime) {
//			Log.d("speed", "Auto extracted by exceeding reasonable waiting time");
			return true;
		}
		
		return false;
	}

	private DurationUserEnv.Builder makeDurationUserEnvBuilder(long currTimeDate, TimeZone currTimeZone) {
		return new DurationUserEnv.Builder()
		.setTime(currTimeDate)
		.setEndTime(currTimeDate)
		.setTimeZone(currTimeZone);
	}
}
