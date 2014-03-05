package lab.davidahn.appshuttle.collect;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvSensor;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.HeadsetEnvSensor;
import lab.davidahn.appshuttle.collect.env.LocEnvSensor;
import lab.davidahn.appshuttle.collect.env.PlaceEnvSensor;
import lab.davidahn.appshuttle.collect.env.SpeedEnvSensor;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class EnvCollectionService extends Service {
	private Date currTimeDate;
	private TimeZone currTimeZone;
	private Map<EnvType, EnvSensor> sensors;
	private Map<EnvType, UserEnv> sensedEnvs;

    @Override
	public void onCreate() {
		super.onCreate();
		sensors = new HashMap<EnvType, EnvSensor>();
		sensedEnvs = new HashMap<EnvType, UserEnv>();
		registerSensors();
		if(!isDonePreCollection()){
			preCollectCollectDurationUserEnv();
			setDonePreCollection();
		}
	}
	
    @Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		Log.d("EnvCollectionService", "started.");

		senseTime();
		senseEnv();
		extractAndStoreDurationUserEnv();
		UpdateCxt(AppShuttleApplication.currUserCxt);
		
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		postCollectDurationUserEnv();
	}
	
	private void registerSensors() {
		sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance());
		sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance());
		sensors.put(EnvType.SPEED, SpeedEnvSensor.getInstance());
		sensors.put(EnvType.HEADSET, HeadsetEnvSensor.getInstance());
	}

	private void senseEnv() {
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			sensedEnvs.put(envType, sensor.sense(currTimeDate, currTimeZone));
		}
	}

	private void UpdateCxt(SnapshotUserCxt uCxt) {
		uCxt.setTime(currTimeDate);
		uCxt.setTimeZone(currTimeZone);
		uCxt.updateUserEnv(sensedEnvs);
	}
	
	private void extractAndStoreDurationUserEnv() {
		for(EnvType envType : sensors.keySet()){
			EnvSensor sensor = sensors.get(envType);
			DurationUserEnv durationUserEnv = sensor.extractDurationUserEnv(currTimeDate, currTimeZone, sensedEnvs.get(envType));
			storeDurationUserEnv(durationUserEnv);
		}
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		if(durationUserEnv == null)
			return;
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();
		durationUserEnvManager.store(durationUserEnv);
	}

	private void preCollectCollectDurationUserEnv() {
		senseTime();
		for(EnvSensor sensor : sensors.values()){
			List<DurationUserEnv> preExtractedDurationUserEnvList = sensor.preExtractDurationUserEnv(currTimeDate, currTimeZone);
			for(DurationUserEnv preExtractedDurationUserEnv : preExtractedDurationUserEnvList)
				storeDurationUserEnv(preExtractedDurationUserEnv);
		}
//		Log.d("EnvCollectionService", "pre-collection");
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

	private void postCollectDurationUserEnv() {
		senseTime();
		for(EnvSensor sensor : sensors.values()){
			DurationUserEnv postExtractedDurationUserEnv = sensor.postExtractDurationUserEnv(currTimeDate, currTimeZone);
			storeDurationUserEnv(postExtractedDurationUserEnv);
		}
	}

	private void senseTime() {
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();
	}
}
