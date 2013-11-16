package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.ZeroUserSpeed;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserSpeed;
import android.location.Location;
import android.util.Log;

public class SpeedEnvSensor extends BaseEnvSensor {
	private UserSpeed prevUSpeed;
	private UserSpeed currUSpeed;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	
    private static SpeedEnvSensor speedEnvSensor = new SpeedEnvSensor();

    private SpeedEnvSensor(){
    	super();
		prevUSpeed = currUSpeed = ZeroUserSpeed.getInstance();
	}
	
	public static SpeedEnvSensor getInstance(){
		return speedEnvSensor;
	}
	
	public UserSpeed sense(){
		prevUSpeed = currUSpeed;
		
		Location loc = LocEnvSensor.getInstance().getLastKnownLoc();
		
		currUSpeed = ZeroUserSpeed.getInstance();

		if(!loc.hasSpeed()) {
			Log.d("speed", "value :"+loc.getSpeed());
			return currUSpeed;
		}
		
		currUSpeed = UserSpeed.create(loc.getSpeed());
		
		Log.i("speed", currUSpeed.toString());
		
		return currUSpeed;
	}
	
	public boolean isChanged(){
		if(!currUSpeed.equals(prevUSpeed)) {
			Log.i("user env", "user speed changed");
			return true;
		} else {
			return false;
		}
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged() || isAutoExtractionTime(currTimeDate, currTimeZone)){
				res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		DurationUserEnv res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
		durationUserEnvBuilder = null;
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return new DurationUserEnv.Builder()
			.setTime(currTimeDate)
			.setEndTime(currTimeDate)
			.setTimeZone(currTimeZone)
			.setEnvType(uEnv.getEnvType())
			.setUserEnv(uEnv);
	}
}