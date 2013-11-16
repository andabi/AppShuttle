package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.InvalidUserSpeed;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.context.env.UserSpeed;
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
		prevUSpeed = currUSpeed;
		
		currUSpeed = InvalidUserSpeed.getInstance();
		
		if(locEnvSensor.isChanged()) {
			UserLoc currLoc = locEnvSensor.getCurrULoc();
			UserLoc prevLoc =  locEnvSensor.getPrevULoc();

			if(!currLoc.isValid() || !prevLoc.isValid()){
				Log.d("speed", "sensing failure: invalidUserLoc");
				return currUSpeed;
			}
			
			Date lastSensedTimeDate;
			if(durationUserEnvBuilder == null)
				lastSensedTimeDate = currTimeDate;
			else
				lastSensedTimeDate = durationUserEnvBuilder.getTimeDate();
			
			try {
				double speed = currLoc.distanceTo(prevLoc) / (currTimeDate.getTime() - lastSensedTimeDate.getTime());
				currUSpeed = UserSpeed.create(speed);
				
				Log.i("speed", "sensed: " + currUSpeed.toString());
			} catch (InvalidUserEnvException e) {
				Log.d("speed", "sensing failure: invalidUserLoc");
				return currUSpeed;
			}
			
			lastSensedTimeDate = currTimeDate;
		} else {
			currUSpeed = prevUSpeed;
			
			Log.d("speed", "not sensed yet");
		}
		
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
			if(locEnvSensor.isChanged() || isAutoExtractionTime(currTimeDate, currTimeZone)){
				res = durationUserEnvBuilder.setEnvType(uEnv.getEnvType()).setUserEnv(uEnv).setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone);
			}
		}
		return res;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		durationUserEnvBuilder = null;
		return null;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone) {
		return new DurationUserEnv.Builder()
			.setTime(currTimeDate)
			.setEndTime(currTimeDate)
			.setTimeZone(currTimeZone);
	}
}