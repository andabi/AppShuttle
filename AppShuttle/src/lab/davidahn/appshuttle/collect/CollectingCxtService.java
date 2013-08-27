package lab.davidahn.appshuttle.collect;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.context.ContextRefiner;
import lab.davidahn.appshuttle.context.RfdUserCxt;
import lab.davidahn.appshuttle.context.RfdUserCxtDao;
import lab.davidahn.appshuttle.context.UserCxt;
import lab.davidahn.appshuttle.context.UserCxtDao;
import lab.davidahn.appshuttle.context.bhv.AppUserBhv;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvDao;
import lab.davidahn.appshuttle.context.env.ChangeUserEnv;
import lab.davidahn.appshuttle.context.env.ChangeUserEnvDao;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidLocationException;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
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
					, settings.getInt("collection.location.tolerance.distance", 100), locationListener);
		}
		
		//time
		calendar = Calendar.getInstance();
		
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); 
	    keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  

		appUserBhvSensor = AppUserBhvSensor.getInstance(getApplicationContext());

//		patternManager = PatternManager.getInstance(getApplicationContext());
	}
	
	public void onHandleIntent(Intent intent){
		//sense
		UserCxt uCxt = new UserCxt();
		senseAndSetTime(uCxt);
		senseAndSetLocation(uCxt);
		senseAndSetPlace(uCxt);
		senseBhv(uCxt);

		//update global state
		updateGlobalState(uCxt);

		//store cxt data
		UserCxtDao userCxtDao = UserCxtDao.getInstance(getApplicationContext());

		if(settings.getBoolean("collection.store_cxt.enabled", false)) 
			userCxtDao.storeCxt(uCxt);
		storeChangeUserEnv(uCxt);

		ContextRefiner cxtRefiner = ContextRefiner.getInstance(getApplicationContext());
		List<RfdUserCxt> rfdUCxtList = cxtRefiner.refineDurationUserBhv(uCxt);
		RfdUserCxtDao rfdUserCxtDao = RfdUserCxtDao.getInstance(getApplicationContext());
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			rfdUserCxtDao.storeRfdCxt(rfdUCxt);
			registerBhv(rfdUCxt.getBhv());
		}
		List<DurationUserEnv> durationUserEnvList = cxtRefiner.refineDurationUserEnv(uCxt);
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		for(DurationUserEnv durationUserEnv : durationUserEnvList){
			durationUserEnvDao.storeDurationUserEnv(durationUserEnv);
		}
	}
	
	private void storeChangeUserEnv(UserCxt uCxt) {
		ChangeUserEnvDao changedUserEnvDao = ChangeUserEnvDao.getInstance(getApplicationContext());

		if(GlobalState.placeMoved && GlobalState.prevUCxt != null) 
			changedUserEnvDao.storeChangedUserEnv(new ChangeUserEnv(uCxt.getTime()
				, uCxt.getTimeZone()
				, EnvType.PLACE
				, GlobalState.prevUCxt.getUserEnv(EnvType.PLACE)
				, uCxt.getUserEnv(EnvType.PLACE)));
		
		if(GlobalState.locMoved && GlobalState.prevUCxt != null) 
			changedUserEnvDao.storeChangedUserEnv(new ChangeUserEnv(uCxt.getTime()
				, uCxt.getTimeZone()
				, EnvType.LOCATION
				, GlobalState.prevUCxt.getUserEnv(EnvType.LOCATION)
				, uCxt.getUserEnv(EnvType.LOCATION)));
	}

	private void updateGlobalState(UserCxt uCxt) {
		//update globalState
		if(GlobalState.currentUCxt == null) {
			;
		} else {
			GlobalState.prevUCxt = GlobalState.currentUCxt;
		}
		
		GlobalState.currentUCxt = uCxt;
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
	
	//TODO check about invalid loc
	public void senseAndSetLocation(UserCxt uCxt) {
		if (currentLoc == null) {
			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(0, 0, UserLoc.Validity.INVALID)));
			Log.d("location", "sensing failure");
		}
		else {
			uCxt.addUserEnv(EnvType.LOCATION, new LocUserEnv(new UserLoc(currentLoc.getLatitude(), currentLoc.getLongitude(), UserLoc.Validity.VALID)));

			//moved check
			UserLoc prevULoc = null;
			UserLoc currULoc = ((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
			if(GlobalState.prevUCxt == null || ((LocUserEnv)GlobalState.prevUCxt.getUserEnv(EnvType.LOCATION)).getLoc() == null){
				GlobalState.locMoved = false;
			} else {
				prevULoc = ((LocUserEnv)GlobalState.prevUCxt.getUserEnv(EnvType.LOCATION)).getLoc();
				try {
					if(!currULoc.proximity(prevULoc, settings.getInt("collection.location.tolerance.distance", 100))) {
						GlobalState.locMoved = true;
						Log.i("changed env", "loc moved");
					} else {
						GlobalState.locMoved = false;
					}
				} catch (InvalidLocationException e) {
					;
				}
			}
		}
	}
	
	//TODO check about invalid loc
	public void senseAndSetPlace(UserCxt uCxt) {
		UserLoc currULoc = ((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
		UserLoc currUPlace = null;
		if(GlobalState.prevUCxt == null){
			GlobalState.placeMoved = false;
			currUPlace = currULoc;
		} else {
			UserLoc prevUPlace = ((PlaceUserEnv) GlobalState.prevUCxt.getUserEnv(EnvType.PLACE)).getPlace();
			if(prevUPlace == null){
				GlobalState.placeMoved = false;
				currUPlace = currULoc;
			} else {
				try {
					if(!currULoc.proximity(prevUPlace, settings.getInt("collection.place.tolerance.distance", 5000))) {
						currUPlace = currULoc;
						GlobalState.placeMoved = true;
						Log.i("changed env", "place moved");
					} else {
						currUPlace = prevUPlace;
						GlobalState.placeMoved = false;
					}
				} catch (InvalidLocationException e) {
					;
				}
			}
		}
		uCxt.addUserEnv(EnvType.PLACE, new PlaceUserEnv(currUPlace));
	}
	
	public void senseAndSetTime(UserCxt uCxt) {
		uCxt.setTime(new Date(System.currentTimeMillis()));
		uCxt.setTimeZone(calendar.getTimeZone());
	}
	
	public void senseSenserInfo(UserEnv uEnv){
		
	}

	public void senseSystemStatusEnv(UserEnv uEnv) {
		
	}
	
	public void senseBhv(UserCxt uCxt){
		senseAppBhv(uCxt);
		senseCallBhv(uCxt);
		senseMsgBhv(uCxt);
		senseSystemStatusBhv(uCxt);
	}
	
	public void senseAppBhv(UserCxt uCxt) {
		senseActivityBhv(uCxt);
		senseServiceBhv(uCxt);
	}

	public void senseActivityBhv(UserCxt uCxt) {
	    if (!powerManager.isScreenOn()) { //screen off
			uCxt.addUserBhv(new UserBhv(BhvType.NONE, "screen.off"));
			Log.d("collection", "screen off");
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				uCxt.addUserBhv(new UserBhv(BhvType.NONE, "lock.screen.on"));
				Log.d("collection", "lock screen on");
	    } else {
			for(String bhvName : appUserBhvSensor.getCurrentActivity()){
				uCxt.addUserBhv(new AppUserBhv(BhvType.APP, bhvName));
			}
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
	
	public void registerBhv(UserBhv uBhv){
		UserBhvDao userBhvDao = UserBhvDao.getInstance(getApplicationContext());

		if(uBhv.getBhvType() == BhvType.NONE) {
			;
		} else if(uBhv.getBhvType() == BhvType.APP) {
			if(((AppUserBhv)uBhv).isValid(getApplicationContext()))
					userBhvDao.storeUserBhv(uBhv);
		} else {
			;
		}
	}
}
