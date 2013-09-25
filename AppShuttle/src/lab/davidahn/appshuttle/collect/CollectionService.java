package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.SnapshotUserCxtDao;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class CollectionService extends Service {
	private Calendar calendar;
    private Map<EnvType, EnvSensor> sensors;
    private List<BhvCollector> collectors;
	private SharedPreferences preferenceSettings;
	
//	public CollectionService() {
//		this("CollectionService");
//	}
//	
//	public CollectionService(String name) {
//		super(name);
//	}

	public void onCreate() {
		super.onCreate();
		
		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		calendar = Calendar.getInstance();

		sensors = new HashMap<EnvType, EnvSensor>();
		sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance(getApplicationContext()));
		sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance(getApplicationContext()));
		
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance(getApplicationContext()));
		collectors.add(CallBhvCollector.getInstance(getApplicationContext()));
		
		preCollection();
	}
	
	private void preCollection() {
		Date currTimeDate = new Date(System.currentTimeMillis());
		TimeZone currTimeZone = calendar.getTimeZone();
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);

			List<DurationUserEnv> preExtractedDurationUserEnvList = sensor.preExtractDurationUserEnv(currTimeDate, currTimeZone);
			for(DurationUserEnv preExtractedDurationUserEnv : preExtractedDurationUserEnvList)
				storeDurationUserEnv(preExtractedDurationUserEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> preExtractedDurationUserBhvList = 
					collector.preExtractDurationUserBhv(currTimeDate, currTimeZone);
			storeDurationUserBhv(preExtractedDurationUserBhvList);

			UserBhvManager userBhvManager = UserBhvManager.getInstance(getApplicationContext());
			for(DurationUserBhv preExtractedDurationUserBhv : preExtractedDurationUserBhvList){
				UserBhv uBhv = preExtractedDurationUserBhv.getBhv();
				if(uBhv.isValid(getApplicationContext()))
					userBhvManager.registerBhv(uBhv);
			}
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		SnapshotUserCxt uCxt = new SnapshotUserCxt();

		Date currTimeDate = new Date(System.currentTimeMillis());
		TimeZone currTimeZone = calendar.getTimeZone();

		uCxt.setTime(currTimeDate);
		uCxt.setTimeZone(currTimeZone);
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			UserEnv uEnv = sensor.sense();
			
			uCxt.addUserEnv(envType, uEnv);

			DurationUserEnv durationUserEnv = sensor.extractDurationUserEnv(currTimeDate, currTimeZone, uEnv);
			if(durationUserEnv != null)
				storeDurationUserEnv(durationUserEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<UserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
			
			List<DurationUserBhv> durationUserBhvList = 
					collector.extractDurationUserBhv(currTimeDate, currTimeZone, userBhvList);
			storeDurationUserBhv(durationUserBhvList);

			UserBhvManager userBhvManager = UserBhvManager.getInstance(getApplicationContext());
			for(DurationUserBhv durationUserBhv : durationUserBhvList){
				UserBhv uBhv = durationUserBhv.getBhv();
				if(uBhv.isValid(getApplicationContext()))
					userBhvManager.registerBhv(uBhv);
			}
		}
		
		if(preferenceSettings.getBoolean("collection.store_cxt.enabled", false))
			storeSnapshotCxt(uCxt);

		((AppShuttleApplication)getApplicationContext()).setCurrUserCxt(uCxt);
		
		return START_NOT_STICKY;
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}

	public void onHandleIntent(Intent intent){		

	}
	
	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
		SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance(getApplicationContext());
		snapshotUserCxtDao.storeCxt(uCxt);
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		durationUserEnvDao.storeDurationUserEnv(durationUserEnv);
	}
	
	private void storeDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance(getApplicationContext());
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.storeDurationBhv(durationUserBhv);
		}		
	}

	public void onDestroy() {
		super.onDestroy();
	}
}
