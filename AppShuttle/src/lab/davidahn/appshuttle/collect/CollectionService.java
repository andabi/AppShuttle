package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.context.DuratinoUserBhvDao;
import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.SnapshotUserCxtDao;
import lab.davidahn.appshuttle.context.bhv.AppUserBhv;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvDao;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CollectionService extends IntentService {
	private Calendar calendar;
    SharedPreferences settings;
    Map<EnvType, EnvSensor> sensors;
    List<BhvCollector> collectors;
	
	public CollectionService() {
		this("CollectionService");
	}
	
	public CollectionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
		calendar = Calendar.getInstance();

		sensors = new HashMap<EnvType, EnvSensor>();
		sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance(getApplicationContext()));
		sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance(getApplicationContext()));
		
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance(getApplicationContext()));
		collectors.add(CallBhvCollector.getInstance(getApplicationContext()));
	}
	
	public void onHandleIntent(Intent intent){
		SnapshotUserCxt uCxt = new SnapshotUserCxt();

		uCxt.setTime(new Date(System.currentTimeMillis()));
		uCxt.setTimeZone(calendar.getTimeZone());
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			UserEnv uEnv = sensor.sense();
			
			uCxt.addUserEnv(envType, uEnv);

			DurationUserEnv durationUserEnv = sensor.refineDurationUserEnv(uCxt);
			if(durationUserEnv != null)
				storeDurationUserEnv(durationUserEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<UserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
			
			List<DurationUserBhv> durationUserBhvList = 
					collector.refineDurationUserBhv(uCxt.getTime(), uCxt.getTimeZone(), userBhvList);
			storeDurationUserBhv(durationUserBhvList);

			for(DurationUserBhv durationUserBhv : durationUserBhvList){
				registerBhv(durationUserBhv.getBhv());
			}
		}
		
		GlobalState.currUserCxt = uCxt;
		storeSnapshotCxt(uCxt);
	}
	
	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
		if(settings.getBoolean("collection.store_cxt.enabled", false)) {
			SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance(getApplicationContext());
			snapshotUserCxtDao.storeCxt(uCxt);
		}
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		durationUserEnvDao.storeDurationUserEnv(durationUserEnv);
	}
	
	private void storeDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		DuratinoUserBhvDao durationUserBhvDao = DuratinoUserBhvDao.getInstance(getApplicationContext());
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.storeRfdCxt(durationUserBhv);
		}		
	}

	public void onDestroy() {
		super.onDestroy();
	}
	
	public void registerBhv(UserBhv uBhv){
		UserBhvDao userBhvDao = UserBhvDao.getInstance(getApplicationContext());

		if(uBhv.getBhvType() == BhvType.NONE) {
			;
		} else {
			if(uBhv.getBhvType() == BhvType.APP) {
				if(((AppUserBhv)uBhv).isValid(getApplicationContext()))
						userBhvDao.storeUserBhv(uBhv);
			} else {
				userBhvDao.storeUserBhv(uBhv);
			}
		}
	}
}
