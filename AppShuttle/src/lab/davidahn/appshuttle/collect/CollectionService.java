package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.BhvCollector;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.SensorOnCollector;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvSensor;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.HeadsetEnvSensor;
import lab.davidahn.appshuttle.collect.env.LocEnvSensor;
import lab.davidahn.appshuttle.collect.env.PlaceEnvSensor;
import lab.davidahn.appshuttle.collect.env.SpeedEnvSensor;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
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
		if(!isDonePreCollection()){
			preCollectCollectDurationUserContext();
			setDonePreCollection();
		}
	}
	
    private void registerSensors() {
    	sensors = new HashMap<EnvType, EnvSensor>();
    	sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance());
    	sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance());
    	sensors.put(EnvType.SPEED, SpeedEnvSensor.getInstance());
    	sensors.put(EnvType.HEADSET, HeadsetEnvSensor.getInstance());
    }
    
    private void registerCollectors() {
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance());
		collectors.add(CallBhvCollector.getInstance());
		collectors.add(SensorOnCollector.getInstance());
	}

	private boolean isDonePreCollection() {
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		boolean isDone = pref.getBoolean("collection.pre.done", false);
		return isDone;
	}

	private void setDonePreCollection(){
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("collection.pre.done", true);
		editor.commit();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		Log.d("collection", "CollectionService started.");
		
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();
		
		SnapshotUserCxt uCxt = CollectSnapshotUserContext();
		extractAndStoreSnapshotAppUserBhvAsDurationUserBhv(uCxt);
		extractAndStoreDurationUserContext(uCxt);

		AppShuttleApplication.currUserCxt = uCxt;
		
		NotiBarNotifier.getInstance().updateNotification();
//		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW"));
		
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
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
			RegisterAndStoreDurationUserBhv(preExtractedDurationUserBhvList);
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
		
//		storeSnapshotCxt(uCxt);
		
		return uCxt;
	}
	
    private void extractAndStoreSnapshotAppUserBhvAsDurationUserBhv(SnapshotUserCxt uCxt) {
		List<DurationUserBhv> snapshotAppUserBhvList = new ArrayList<DurationUserBhv>();
		for(UserBhv uBhv : uCxt.getUserBhvs()){
			if(uBhv.getBhvType() != UserBhvType.APP)
				continue;
			
			AppBhvCollector bhvCollector = AppBhvCollector.getInstance();
			if(!bhvCollector.isTrackedForDurationUserBhv(uBhv)){
				snapshotAppUserBhvList.add(bhvCollector.createDurationUserBhvBuilder(
						uCxt.getTimeDate(), uCxt.getTimeDate(), uCxt.getTimeZone(), uBhv).build());
			}
		}
		
		RegisterAndStoreDurationUserBhv(snapshotAppUserBhvList);
	}

	private void extractAndStoreDurationUserContext(SnapshotUserCxt uCxt) {
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
			RegisterAndStoreDurationUserBhv(durationUserBhvList);
		}
	}
	
	private void postCollectDurationUserContext() {
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();

		postCollectDurationUserBhv();
		postCollectDurationUserEnv();
		
//		Log.d("collection", "post collection");
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
			RegisterAndStoreDurationUserBhv(postExtractedDurationUserBhvList);
		}
	}

//	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
//		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
//
//		if(!preferenceSettings.getBoolean("collection.store_snapshot_cxt.enabled", false))
//			return;
//		
//		SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance();
//		snapshotUserCxtDao.storeCxt(uCxt);
//	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		if(durationUserEnv == null)
			return;
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();
		durationUserEnvManager.store(durationUserEnv);
	}
	
	private void RegisterAndStoreDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		registerUserBhvs(durationUserBhvList);

		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.store(durationUserBhv);
		}
	}
	
	private void registerUserBhvs(List<DurationUserBhv> durationUserBhvList) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			BaseUserBhv uBhv = (BaseUserBhv)durationUserBhv.getUserBhv();
			if(uBhv.isValid())
				userBhvManager.register(uBhv);
		}
	}
}
