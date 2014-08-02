package lab.davidahn.appshuttle.collect.env;

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
	
    private static LocEnvSensor locEnvSensor = new LocEnvSensor();
	
	public static LocEnvSensor getInstance(){
		return locEnvSensor;
	}
	
	private LocEnvSensor(){
		super();
		
		locationManager = (LocationManager) cxt.getSystemService(Context.LOCATION_SERVICE);
		prevEnv = currEnv = InvalidUserLoc.getInstance();

		extractBestProvider();
		extractLocation();
	}

	private void extractLocation() {
		if(bestProvider != null)
			lastKnownLoc = locationManager.getLastKnownLocation(bestProvider);
			
		if(lastKnownLoc != null)
			return;

		List<String> providers = locationManager.getProviders(true);
		if(providers.isEmpty()){
//			Log.i("loc provider", "null");
			return;
		}
		
		for(String provider : providers) {
//			Log.d("provider", provider);
			lastKnownLoc = locationManager.getLastKnownLocation(provider);
			if(lastKnownLoc != null) 
				break;
		}
	}
	
	@Override
	public UserLoc sense(long currTime, TimeZone currTimeZone){
		if(lastKnownLoc == null) {
			return InvalidUserLoc.getInstance();
//			Log.i("location", "sensing failure");
		} else {
			return  UserLoc.create(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
//			Log.i("location", currULoc.toString());
		}
	}
	
	private void extractBestProvider() {
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_COARSE);
		crit.setCostAllowed(true);
		crit.setPowerRequirement(Criteria.POWER_MEDIUM);
		
		bestProvider = locationManager.getBestProvider(crit, true);
		
//		Log.d("best provider", bestProvider);
		
		if(bestProvider == null)
			return;
		
		locationManager.requestLocationUpdates(bestProvider, 
				preferenceSettings.getLong("collection.env.location.tolerance.time", 300000), 
				preferenceSettings.getInt("collection.env.location.tolerance.distance", 500), 
				locationListener);
	}

	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
//			Log.i("changed location", "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
			lastKnownLoc = location;
		}

		public void onProviderDisabled(String provider) {
			extractBestProvider();
			extractLocation();
		}

		public void onProviderEnabled(String provider) {
			extractBestProvider();
			extractLocation();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			;
		}
	};
}
