package lab.davidahn.appshuttle.collector;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.bean.RfdUserCxt;
import lab.davidahn.appshuttle.bean.UserCxt;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bhv.AppUserBhv;
import lab.davidahn.appshuttle.bhv.AppUserBhvSensor;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.UserBhvManager;
import lab.davidahn.appshuttle.exception.InvalidLocationException;
import lab.davidahn.appshuttle.utils.Utils;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

public class CollectingCxtService extends IntentService {
	private LocationManager locationManager;
	private String bestProvider;
	private AppUserBhvSensor appUserBhvSensor;
	private Calendar calendar;
	private ContextManager contextManager;
	private UserBhvManager userBhvManager;
//	private Properties property;
	private Location currentLoc;
//	private PatternManager patternManager;
	private PowerManager powerManager;
	private KeyguardManager keyguardManager;
    SharedPreferences settings;

	
	public CollectingCxtService() {
		this("CollectingCxtService");
	}
	
	public CollectingCxtService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
		
		//location
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
			currentLoc = locationManager.getLastKnownLocation(bestProvider);
			if(currentLoc == null){
				for(String provider : providers) {
//					Log.d("provider", provider);
					currentLoc = locationManager.getLastKnownLocation(provider);
					if(currentLoc != null) break;
				}
			}
			locationManager.requestLocationUpdates(bestProvider, settings.getLong("collection.location.tolerance.time", 6000)
					, settings.getInt("collection.location.tolerance.distance", 10), locationListener);
		}
		
		//time
		calendar = Calendar.getInstance();
		
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); 
	    keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  

		appUserBhvSensor = AppUserBhvSensor.getInstance(getApplicationContext());
		contextManager = ContextManager.getInstance(getApplicationContext());
		userBhvManager = UserBhvManager.getInstance(getApplicationContext());

//		patternManager = PatternManager.getInstance(getApplicationContext());
	}
	
	public void onHandleIntent(Intent intent){
		UserEnv uEnv = new UserEnv();
		senseAndSetTime(uEnv);
		senseAndSetLocation(uEnv);
		setPlace(uEnv);
		
		UserCxt uCxt = new UserCxt(uEnv);
		senseBhv(uCxt);
		
		ContextRefiner cxtRefiner = contextManager.getCxtRefiner();
		if(settings.getBoolean("collection.store_cxt.enabled", false)) contextManager.storeCxt(uCxt);
		List<RfdUserCxt> rfdUCxtList = cxtRefiner.refineCxt(uCxt);
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			contextManager.storeRfdCxt(rfdUCxt);
			userBhvManager.registerBhv(rfdUCxt.getBhv());
//			cxtRefiner.storeRfdCxtByBhv(rfdUCxt);
//			List<Pattern> patternList = patternManager.getPatternMiner().minePattern(rfdUCxt, tableName);
//			patternManager.getPatternMiner().storePattern(patternList);
			
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
	}
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.i("changed location", "latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude());
			currentLoc = location;
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
			locationManager.requestLocationUpdates(provider, 5000, 0, locationListener);
			currentLoc = locationManager.getLastKnownLocation(provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	
	public void senseAndSetLocation(UserEnv uEnv) {
		if (currentLoc == null) {
			uEnv.setLoc(new UserLoc(0, 0, UserLoc.Validity.INVALID));
			Log.d("location", "sensing failure");
		}
		else {
			uEnv.setLoc(new UserLoc(currentLoc.getLatitude(), currentLoc.getLongitude(), UserLoc.Validity.VALID));
//				Log.d("location", "latitude: " + latitude + ", longitude: " + longitude);
		}
		GlobalState.currentUEnv = uEnv;
	}
	
	public void setPlace(UserEnv uEnv) {
		if(GlobalState.place == null){
			GlobalState.place = GlobalState.currentUEnv.getLoc();
			GlobalState.moved = false;
		}
		try {
			if(!Utils.Proximity(GlobalState.place, GlobalState.currentUEnv.getLoc(), settings.getInt("location.min_distance", 2000))){
				GlobalState.place = GlobalState.currentUEnv.getLoc();
				GlobalState.moved = true;
			} else {
				GlobalState.moved = false;
			}
		} catch (InvalidLocationException e) {
			;
		}
		uEnv.setPlace(GlobalState.place);
	}
	
	public void senseAndSetTime(UserEnv uEnv) {
		uEnv.setTime(new Date(System.currentTimeMillis()));
		uEnv.setTimeZone(calendar.getTimeZone());
	}
	
	public void senseSenserInfo(UserEnv uEnv){
		
	}

	public void senseSystemStatusEnv(UserEnv uEnv) {
		
	}
	
	public void senseBhv(UserCxt uCxt){
		boolean isPresent = senseInvalidBhv(uCxt);
		if(isPresent) senseActivityBhv(uCxt);
		senseServiceBhv(uCxt);
		senseCallBhv(uCxt);
		senseMsgBhv(uCxt);
		senseSystemStatusBhv(uCxt);
	}
	
	private boolean senseInvalidBhv(UserCxt uCxt) {
	    if (!powerManager.isScreenOn()) { //screen off
			uCxt.addUserBhv(new UserBhv("invalid", "screen.off"));
			Log.d("collection", "screen off");
			return false;
        }
		else {
		    if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				uCxt.addUserBhv(new UserBhv("invalid", "lock.screen.on"));
				Log.d("collection", "lock screen on");
				return false;
		    }
	    }
	    return true;
	}

	public void senseActivityBhv(UserCxt uCxt) {
		for(String bhvName : appUserBhvSensor.getCurrentActivity()){
			uCxt.addUserBhv(new AppUserBhv("activity", bhvName));
		}
	}
	
	public void senseServiceBhv(UserCxt uCxt) {
//		for(String bhvName : applicationManager.getCurrentService()){
//			uCxt.addUserBhv(new AppUserBhv("service", bhvName));
//		}
	}
	
	public void senseCallBhv(UserCxt uCxt) {
		
	}
	
	public void senseMsgBhv(UserCxt uCxt){
		
	}

	public void senseSystemStatusBhv(UserCxt uCxt) {
		
	}
}
