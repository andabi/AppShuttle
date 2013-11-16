package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.SnapshotUserCxtDao;
import lab.davidahn.appshuttle.context.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class CollectionService extends Service {
	private Date currTimeDate;
	private TimeZone currTimeZone;
	private Map<EnvType, EnvSensor> sensors;
    private List<BhvCollector> collectors;

    @Override
	public void onCreate() {
		super.onCreate();
		
		registerSensors();
		registerCollectors();

		preCollectCollectDurationUserContext();
	}
	
    private void registerSensors() {
    	sensors = new HashMap<EnvType, EnvSensor>();
    	sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance());
    	sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance());
    	sensors.put(EnvType.SPEED, SpeedEnvSensor.getInstance());
    }
    
    private void registerCollectors() {
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance());
		collectors.add(CallBhvCollector.getInstance());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();
		
	
		SnapshotUserCxt uCxt = CollectSnapshotUserContext();
		extractDurationUserContext(uCxt);

		AppShuttleApplication.currUserCxt = uCxt;
		
		return START_NOT_STICKY;
	}
	
    @Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onDestroy() {
		postCollectDurationUserContext();
		super.onDestroy();
	}
	
//	@Override
//	public void onLowMemory(){
//		postCollectDurationUserContext();
//		super.onLowMemory();
//	}
	
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
//		Log.d("collection", "pre collection");
	}
	
	private SnapshotUserCxt CollectSnapshotUserContext() {
		SnapshotUserCxt uCxt = new SnapshotUserCxt();

		uCxt.setTime(currTimeDate);
		uCxt.setTimeZone(currTimeZone);
		
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			UserEnv uEnv = sensor.sense(uCxt.getTimeDate(), uCxt.getTimeZone());
			uCxt.addUserEnv(envType, uEnv);
		}
		
		for(BhvCollector collector : collectors){
			List<BaseUserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
		}
		
		storeSnapshotCxt(uCxt);
		
		return uCxt;
	}

	private void extractDurationUserContext(SnapshotUserCxt uCxt) {
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
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			BaseUserBhv uBhv = (BaseUserBhv)durationUserBhv.getUserBhv();
			if(uBhv.isValid())
				userBhvManager.registerBhv(uBhv);
		}
	}

	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		if(!preferenceSettings.getBoolean("collection.store_snapshot_cxt.enabled", false))
			return;
		
		SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance();
		snapshotUserCxtDao.storeCxt(uCxt);
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		if(durationUserEnv == null)
			return;
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();
		durationUserEnvManager.store(durationUserEnv);
	}
	
	private void storeDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.store(durationUserBhv);
		}		
	}
}
