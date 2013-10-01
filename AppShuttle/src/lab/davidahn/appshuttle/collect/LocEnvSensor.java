package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.context.env.UserLoc.UserLocValidity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocEnvSensor extends BaseEnvSensor {
	private LocationManager _locationManager;
	private Location _lastKnownLoc;
	private String _bestProvider;
	
	private UserLoc _prevULoc;
	private UserLoc _currULoc;
    private DurationUserEnv.Builder _durationUserEnvBuilder;

    private static LocEnvSensor _locEnvSensor = new LocEnvSensor();
	
	public static LocEnvSensor getInstance(){
		return _locEnvSensor;
	}
	
	private LocEnvSensor(){
		super();
		
		_locationManager = (LocationManager) _appShuttleContext.getSystemService(Context.LOCATION_SERVICE);
		
		_prevULoc = _currULoc = null;

		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		crit.setCostAllowed(true);
		crit.setPowerRequirement(Criteria.POWER_HIGH);

		List<String> providers = _locationManager.getProviders(true);

		if(providers.isEmpty()){
			Log.i("loc provider", "null");
			return;
		}
		
		_bestProvider = _locationManager.getBestProvider(crit, true);
// 		 Log.d("best provider", bestProvider);
		
		_locationManager.requestLocationUpdates(_bestProvider, 
				_preferenceSettings.getLong("collection.location.tolerance.time", 300000), 
				_preferenceSettings.getInt("collection.location.tolerance.distance", 500), 
				locationListener);

		_lastKnownLoc = _locationManager.getLastKnownLocation(_bestProvider);
		
		if(_lastKnownLoc == null){
			for(String provider : providers) {
//				Log.d("provider", provider);
				_lastKnownLoc = _locationManager.getLastKnownLocation(provider);
				if(_lastKnownLoc != null) 
					break;
			}
		}
	}

	public UserLoc getCurrULoc() {
		return _currULoc;
	}

	public UserLoc sense(){
		_prevULoc = _currULoc;

		UserLocValidity validity = (_lastKnownLoc == null) ? UserLocValidity.VALID : UserLocValidity.INVALID;
		_currULoc =  UserLoc.create(validity, _lastKnownLoc.getLongitude(), _lastKnownLoc.getLatitude());
//		Log.d("location", "sensing failure");

		return _currULoc;
	}
	
	public boolean isChanged(){
		if(_prevULoc == null)
			return false;
		
		try {
			if(!_currULoc.isSame(_prevULoc)) {
				Log.i("user env", "location moved");
				return true;
			} else {
				return false;
			}
		} catch (InvalidUserEnvException e) {
			return false;
		}
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(_durationUserEnvBuilder == null) {
			_durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged()){
				res = _durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				_durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		DurationUserEnv res = _durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
		_durationUserEnvBuilder = null;
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
			Log.i("changed location", "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
			_lastKnownLoc = location;
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
			_locationManager.requestLocationUpdates(_bestProvider, 
					_preferenceSettings.getLong("collection.location.tolerance.time", 300000), 
					_preferenceSettings.getInt("collection.location.tolerance.distance", 500), 
					locationListener);
			
			_lastKnownLoc = _locationManager.getLastKnownLocation(_bestProvider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
}
