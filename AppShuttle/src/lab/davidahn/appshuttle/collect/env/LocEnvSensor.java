package lab.davidahn.appshuttle.collect.env;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocEnvSensor extends BaseEnvSensor {
	private LocationManager locationManager;
	private Location lastKnownLoc;
	private String bestProvider;
	
	private UserLoc prevULoc;
	private UserLoc currULoc;
    private DurationUserEnv.Builder durationUserEnvBuilder;

    private static LocEnvSensor locEnvSensor = new LocEnvSensor();
	
	public static LocEnvSensor getInstance(){
		return locEnvSensor;
	}
	
	private LocEnvSensor(){
		super();
		
		locationManager = (LocationManager) _appShuttleContext.getSystemService(Context.LOCATION_SERVICE);
		
		prevULoc = currULoc = InvalidUserLoc.getInstance();

		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		crit.setCostAllowed(true);
		crit.setPowerRequirement(Criteria.POWER_MEDIUM);

		List<String> providers = locationManager.getProviders(true);

		if(providers.isEmpty()){
//			Log.i("loc provider", "null");
			return;
		}
		
		bestProvider = locationManager.getBestProvider(crit, true);
// 		 Log.d("best provider", bestProvider);
		
		locationManager.requestLocationUpdates(bestProvider, 
				preferenceSettings.getLong("collection.location.tolerance.time", 300000), 
				preferenceSettings.getInt("collection.location.tolerance.distance", 500), 
				locationListener);

		lastKnownLoc = locationManager.getLastKnownLocation(bestProvider);
		
		if(lastKnownLoc == null){
			for(String provider : providers) {
//				Log.d("provider", provider);
				lastKnownLoc = locationManager.getLastKnownLocation(provider);
				if(lastKnownLoc != null) 
					break;
			}
		}
	}

	public UserLoc getCurrULoc() {
		return currULoc;
	}
	
	public UserLoc getPrevULoc() {
		return prevULoc;
	}
	
//	public Location getLastKnownLoc(){
//		return _lastKnownLoc;
//	}

	@Override
	public UserLoc sense(Date currTimeDate, TimeZone currTimeZone){
		prevULoc = currULoc;

		if(lastKnownLoc == null) {
			currULoc =  InvalidUserLoc.getInstance();
//			Log.i("location", "sensing failure");
		} else {
			currULoc =  UserLoc.create(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
//			Log.i("location", currULoc.toString());
		}

		return currULoc;
	}
	
	@Override
	public boolean isChanged(){
//		if(_prevULoc == null)
//			return false;
		
		if(!currULoc.equals(prevULoc)) {
//			Log.i("user env", "location moved");
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
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged() || isAutoExtractionTime(currTimeDate, currTimeZone)){
				res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	@Override
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
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
//			Log.i("changed location", "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
			lastKnownLoc = location;
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
			locationManager.requestLocationUpdates(bestProvider, 
					preferenceSettings.getLong("collection.location.tolerance.time", 300000), 
					preferenceSettings.getInt("collection.location.tolerance.distance", 500), 
					locationListener);
			
			lastKnownLoc = locationManager.getLastKnownLocation(bestProvider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
}
