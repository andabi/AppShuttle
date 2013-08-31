package lab.davidahn.appshuttle.collect;

import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
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
	private LocUserEnv prevULoc;
	private LocUserEnv currULoc;
    private SharedPreferences settings;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	
	private LocEnvSensor(Context cxt){
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);

		//location
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
					settings.getLong("collection.location.tolerance.time", 6000), 
					settings.getInt("collection.location.tolerance.distance", 100), 
					locationListener);
		}
		
		prevULoc = null;
		currULoc = null;
	}
	
	public static LocEnvSensor getInstance(Context cxt){
		if(locEnvSensor == null) locEnvSensor = new LocEnvSensor(cxt);
		return locEnvSensor;
	}
	
	public LocUserEnv getCurrULoc() {
		return currULoc;
	}

	public LocUserEnv sense(){
		prevULoc = currULoc;
		if (lastKnownLoc == null) {
//			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(0, 0, UserLoc.Validity.INVALID)));
			Log.d("location", "sensing failure");
			currULoc = new LocUserEnv(new UserLoc(0, 0, UserLoc.Validity.INVALID));
		}
		else {
//			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(currentLoc.getLatitude(), currentLoc.getLongitude(), UserLoc.Validity.VALID)));
			currULoc =  new LocUserEnv(new UserLoc(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude(), UserLoc.Validity.VALID));
		}
		return currULoc;
	}
	
	public boolean isChanged(){
//		UserLoc currULoc = ((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
		boolean changed = false;
		//moved check
		if(prevULoc != null){
//			prevULoc = ((LocUserEnv)GlobalState.prevUCxt.getUserEnv(EnvType.LOCATION)).getLoc();
//			try {
//				if(!currULoc.getLoc().proximity(prevULoc.getLoc(), settings.getInt("collection.location.tolerance.distance", 100))) {
				if(!currULoc.equals(prevULoc)) {
					Log.i("changed env", "loc moved");
					changed = true;
				}
//			} catch (InvalidLocationException e) {
//				;
//			}
		}
		return changed;
	}
	
	public DurationUserEnv refineDurationUserEnv(SnapshotUserCxt uCxt) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
		} else {
			if(isChanged()){
				durationUserEnvBuilder.setEndTime(uCxt.getTime());
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
			}
		}
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(SnapshotUserCxt uCxt) {
		return new DurationUserEnv.Builder()
			.setTime(uCxt.getTime())
			.setEndTime(uCxt.getTime())
			.setTimeZone(uCxt.getTimeZone())
			.setUserEnv(uCxt.getUserEnv(EnvType.LOCATION));
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
					settings.getLong("collection.location.tolerance.time", 6000), 
					settings.getInt("collection.location.tolerance.distance", 100), 
					locationListener);
			lastKnownLoc = locationManager.getLastKnownLocation(provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
}
