package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocEnvSensor implements EnvSensor {
	private static LocEnvSensor locEnvSensor;
	private LocationManager locationManager;
	private String bestProvider;
	private Location lastKnownLoc;
	private UserLoc prevULoc;
	private UserLoc currULoc;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	private SharedPreferences preferenceSettings;
	
	private LocEnvSensor(Context cxt){
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		
		locationManager = (LocationManager) cxt.getSystemService(Context.LOCATION_SERVICE);
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		crit.setCostAllowed(true);
		crit.setPowerRequirement(Criteria.POWER_HIGH);
		List<String> providers = locationManager.getProviders(true);
		if(providers.isEmpty()){
			Log.i("loc provider", "null");
		} else {
			bestProvider = locationManager.getBestProvider(crit, true);
//			Log.d("best provider", bestProvider);
			lastKnownLoc = locationManager.getLastKnownLocation(bestProvider);
			if(lastKnownLoc == null){
				for(String provider : providers) {
//					Log.d("provider", provider);
					lastKnownLoc = locationManager.getLastKnownLocation(provider);
					if(lastKnownLoc != null) break;
				}
			}
			locationManager.requestLocationUpdates(bestProvider, 
					preferenceSettings.getLong("collection.location.tolerance.time", 6000), 
					preferenceSettings.getInt("collection.location.tolerance.distance", 100), 
					locationListener);
		}
		
		prevULoc = currULoc = null;
	}
	
	public synchronized static LocEnvSensor getInstance(Context cxt){
		if(locEnvSensor == null) locEnvSensor = new LocEnvSensor(cxt);
		return locEnvSensor;
	}
	
	public UserLoc getCurrULoc() {
		return currULoc;
	}

	public UserLoc sense(){
		prevULoc = currULoc;
		if (lastKnownLoc == null) {
//			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(0, 0, UserLoc.Validity.INVALID)));
			Log.d("location", "sensing failure");
			currULoc = new UserLoc(0, 0, UserLoc.Validity.INVALID);
		}
		else {
			currULoc =  new UserLoc(lastKnownLoc.getLongitude(), lastKnownLoc.getLatitude(), UserLoc.Validity.VALID);
//			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(currentLoc.getLatitude(), currentLoc.getLongitude(), UserLoc.Validity.VALID)));
		}
		return currULoc;
	}
	
	public boolean isChanged(){
//		UserLoc currULoc = ((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
		boolean changed = false;
		//moved check
		if(prevULoc != null){
//			prevULoc = ((LocUserEnv)GlobalState.prevUCxt.getUserEnv(EnvType.LOCATION)).getLoc();
			try {
//				if(!currULoc.getLoc().proximity(prevULoc.getLoc(), settings.getInt("collection.location.tolerance.distance", 100))) {
				if(!currULoc.isSame(prevULoc)) {
					Log.i("changed env", "loc moved");
					changed = true;
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		return changed;
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
			if(isChanged()){
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
			.setEnvType(EnvType.LOCATION)
			.setUserEnv(uEnv);
	}
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.i("changed location", "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
			lastKnownLoc = location;
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
			locationManager.requestLocationUpdates(bestProvider, 
					preferenceSettings.getLong("collection.location.tolerance.time", 6000), 
					preferenceSettings.getInt("collection.location.tolerance.distance", 100), 
					locationListener);
			lastKnownLoc = locationManager.getLastKnownLocation(provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
}
