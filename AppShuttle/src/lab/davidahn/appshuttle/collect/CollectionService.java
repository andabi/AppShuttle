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
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
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
import android.util.Log;

public class CollectionService extends Service {
	private Date currTimeDate;
	private TimeZone currTimeZone;
	private Map<EnvType, EnvSensor> sensors;
    private List<BhvCollector> collectors;
	
//	public CollectionService() {
//		this("CollectionService");
//	}
//	
//	public CollectionService(String name) {
//		super(name);
//	}

	public void onCreate() {
		super.onCreate();
		
		sensors = new HashMap<EnvType, EnvSensor>();
		sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance(getApplicationContext()));
		sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance(getApplicationContext()));
		
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance(getApplicationContext()));
		collectors.add(CallBhvCollector.getInstance(getApplicationContext()));

		preCollectCollectDurationUserContext();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();
		
		SnapshotUserCxt uCxt = CollectSnapshotUserContext();
		CollectDurationUserContext(uCxt);

		((AppShuttleApplication)getApplicationContext()).setCurrUserCxt(uCxt);
		
		return START_NOT_STICKY;
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		postCollectDurationUserContext();
	}
	
	private void preCollectCollectDurationUserContext() {
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();

		for(EnvSensor sensor : sensors.values()){
			List<DurationUserEnv> preExtractedDurationUserEnvList = sensor.preExtractDurationUserEnv(currTimeDate, currTimeZone);
			for(DurationUserEnv preExtractedDurationUserEnv : preExtractedDurationUserEnvList)
				storeDurationUserEnv(preExtractedDurationUserEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> preExtractedDurationUserBhvList = 
					collector.preExtractDurationUserBhv(currTimeDate, currTimeZone);
			storeDurationUserBhv(preExtractedDurationUserBhvList);

			registerEachBhv(preExtractedDurationUserBhvList);
		}
		Log.d("collection", "pre collection");
	}
	
	private SnapshotUserCxt CollectSnapshotUserContext() {
		SnapshotUserCxt uCxt = new SnapshotUserCxt();

		uCxt.setTime(currTimeDate);
		uCxt.setTimeZone(currTimeZone);
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			UserEnv uEnv = sensor.sense();
			uCxt.addUserEnv(envType, uEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<UserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
		}
		
		storeSnapshotCxt(uCxt);
		
		return uCxt;
	}

	private void CollectDurationUserContext(SnapshotUserCxt uCxt) {
		Date currTimeDate = uCxt.getTimeDate();
		TimeZone currTimeZone = uCxt.getTimeZone();
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			DurationUserEnv durationUserEnv = sensor.extractDurationUserEnv(currTimeDate, currTimeZone, uCxt.getUserEnv(envType));
			storeDurationUserEnv(durationUserEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> durationUserBhvList = 
					collector.extractDurationUserBhv(currTimeDate, currTimeZone, uCxt.getUserBhvs());
			storeDurationUserBhv(durationUserBhvList);

			registerEachBhv(durationUserBhvList);
		}
	}
	
	private void postCollectDurationUserContext() {
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();

		postCollectDurationUserBhv();
		postCollectDurationUserEnv();
		
		Log.d("collection", "post collection");
	}

	private void postCollectDurationUserEnv() {
		for(EnvSensor sensor : sensors.values()){
			DurationUserEnv postExtractedDurationUserEnv = sensor.postExtractDurationUserEnv(currTimeDate, currTimeZone);
			storeDurationUserEnv(postExtractedDurationUserEnv);
		}
	}

	private void postCollectDurationUserBhv() {
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> postExtractedDurationUserBhvList = 
					collector.postExtractDurationUserBhv(currTimeDate, currTimeZone);
			storeDurationUserBhv(postExtractedDurationUserBhvList);

			registerEachBhv(postExtractedDurationUserBhvList);
		}
	}

	private void registerEachBhv(List<DurationUserBhv> durationUserBhvList) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance(getApplicationContext());
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			UserBhv uBhv = durationUserBhv.getBhv();
			if(uBhv.isValid(getApplicationContext()))
				userBhvManager.registerBhv(uBhv);
		}
	}

	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
		SharedPreferences preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

		if(!preferenceSettings.getBoolean("collection.store_cxt.enabled", false))
			return;
		SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance(getApplicationContext());
		snapshotUserCxtDao.storeCxt(uCxt);
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		if(durationUserEnv == null)
			return;
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		durationUserEnvDao.storeDurationUserEnv(durationUserEnv);
	}
	
	private void storeDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance(getApplicationContext());
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.storeDurationBhv(durationUserBhv);
		}		
	}
}
