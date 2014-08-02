package lab.davidahn.appshuttle.collect.env;

import java.util.TimeZone;

public class SpeedEnvSensor extends BaseEnvSensor {
	private LocEnvSensor locEnvSensor;
    
    private static SpeedEnvSensor speedEnvSensor = new SpeedEnvSensor();

    private SpeedEnvSensor(){
    	super();
    	locEnvSensor = LocEnvSensor.getInstance();
    	prevEnv = currEnv = UserSpeed.create(0.0);
	}
	
	public static SpeedEnvSensor getInstance(){
		return speedEnvSensor;
	}
	
	@Override
	public UserSpeed sense(long currTime, TimeZone currTimeZone){
		if(!locEnvSensor.isChanged()) {
//			Log.d("speed", "not sensed yet");
			return (UserSpeed)prevEnv;
		}
		
		UserSpeed uSpeed = InvalidUserSpeed.getInstance();
		
		UserLoc currLoc = (UserLoc)locEnvSensor.getCurrEnv();
		UserLoc prevLoc =  (UserLoc)locEnvSensor.getPrevEnv();
		if(!currLoc.isValid() || !prevLoc.isValid()){
//			Log.d("speed", "sensing failure: invalidUserLoc");
			return uSpeed;
		}
		
		long lastSensedTime;
		if(durationUserEnvBuilder == null)
			lastSensedTime = currTime;
		else
			lastSensedTime = durationUserEnvBuilder.getTime();
		
		try {
			double speed = currLoc.distanceTo(prevLoc) / (currTime - lastSensedTime) * 1000;
			uSpeed = UserSpeed.create(speed);
//			Log.i("speed", "sensed: " + currUSpeed.toString());
		} catch (InvalidUserEnvException e) {
//			Log.d("speed", "sensing failure: invalidUserLoc");
			return uSpeed;
		}

		lastSensedTime = currTime;
		
		return uSpeed;
	}
	
	@Override
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
		} else {
			if(locEnvSensor.isChanged()	|| isAutoExtractionTime(currTime, currTimeZone)){
				durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, uEnv);
				durationUserEnvBuilder = setEndTimeForDurationUserEnvBuilder(durationUserEnvBuilder, currTime);
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
			} else if(((UserSpeed)currEnv).isSpeedVehicle() && isAfterAcceptableWaitingTime(currTime, currTimeZone)){
				durationUserEnvBuilder = setEnvForDurationUserEnvBuilder(durationUserEnvBuilder, uEnv);
				durationUserEnvBuilder = setEndTimeForDurationUserEnvBuilder(durationUserEnvBuilder, currTime);
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = createDurationUserEnvBuilder(currTime, currTimeZone);
				currEnv = UserSpeed.create(0.0);
			}
		}
		return res;
	}
	
	protected boolean isAfterAcceptableWaitingTime(long currTime, TimeZone currTimeZone){
		long minAcceptableSpeedKmph = 5;
		long minWaitingTime = (preferenceSettings.getInt("collection.env.location.tolerance.distance", 500) / 1000)
				/ minAcceptableSpeedKmph
				* (1000 * 60 * 60); //6min
//		long reasonableWaitingTime = (long)(1.2 * minWaitingTime);
		long lastSensedTime = durationUserEnvBuilder.getTime();
		if(currTime - lastSensedTime > minWaitingTime) return true;
//		Log.d("speed", "Auto extracted by exceeding reasonable waiting time");
		return false;
	}
}
