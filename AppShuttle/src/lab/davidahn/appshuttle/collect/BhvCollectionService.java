package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import lab.davidahn.appshuttle.report.StatCollector;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class BhvCollectionService extends Service {
	private long currTime;
	private TimeZone currTimeZone;
    private List<BhvCollector> collectors;
    private List<UserBhv> collectedBhvs;
    
    @Override
	public void onCreate() {
		super.onCreate();
		collectors = new ArrayList<BhvCollector>();
		collectedBhvs = new ArrayList<UserBhv>();
		registerCollectors();
		if(!isDonePreCollection()){
			preCollectCollectDurationUserBhv();
			setDonePreCollection();
		}
	}
	
    @Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.d("BhvCollectionService", "started.");
		senseTime();
		senseBhvs();
		extractAndStoreSnapshotAppUserBhvAsDurationUserBhv();
		extractAndStoreDurationUserBhv();
		catchNewAppBhvForStatistics();
		updateCxt(AppShuttleApplication.currUserCxt);
		return START_NOT_STICKY;
	}
	
	private void catchNewAppBhvForStatistics() {
		Set<UserBhv> prevBhvs = new HashSet<UserBhv>(AppShuttleApplication.currUserCxt.getUserBhvs());
		Set<UserBhv> newBhvs = new HashSet<UserBhv>(collectedBhvs);
		newBhvs.removeAll(prevBhvs);
		for(UserBhv bhv : newBhvs) {
//			Log.i("statistics", "uBhv changed (" + bhv.getBhvName() + ")");
			StatCollector.getInstance().notifyBhvTransition(bhv, false);
		}
	}

	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		postCollectDurationUserBhv();
	}
	
	private void registerCollectors() {
		collectors.add(AppBhvCollector.getInstance());
		collectors.add(CallBhvCollector.getInstance());
		collectors.add(SensorOnCollector.getInstance());
	}

	private void senseBhvs() {
		collectedBhvs.clear();
		for(BhvCollector collector : collectors)
			collectedBhvs.addAll(collector.collect());
	}
	
	private void updateCxt(SnapshotUserCxt uCxt) {
		uCxt.setTime(currTime);
		uCxt.setTimeZone(currTimeZone);
		uCxt.clearUserBhvs();
		uCxt.addUserBhvs(collectedBhvs);
	}
	
	private void extractAndStoreDurationUserBhv() {
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> durationUserBhvList = 
					collector.extractDurationUserBhv(currTime, currTimeZone, collectedBhvs);
			registerAndStoreDurationUserBhv(durationUserBhvList);
		}
	}
	
    private void extractAndStoreSnapshotAppUserBhvAsDurationUserBhv() {
		List<DurationUserBhv> snapshotAppUserBhvList = new ArrayList<DurationUserBhv>();
		for(UserBhv uBhv : collectedBhvs){
			if(uBhv.getBhvType() != UserBhvType.APP)
				continue;
			AppBhvCollector bhvCollector = AppBhvCollector.getInstance();
			if(!bhvCollector.isTrackedForDurationUserBhv(uBhv)){
				snapshotAppUserBhvList.add(bhvCollector.createDurationUserBhvBuilder(
						currTime, currTime, currTimeZone, uBhv).build());
			}
		}
		registerAndStoreDurationUserBhv(snapshotAppUserBhvList);
	}

	private void registerAndStoreDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			BaseUserBhv uBhv = (BaseUserBhv)durationUserBhv.getUserBhv();
			if(uBhv.isValid()){
				UserBhvManager.getInstance().register(uBhv);
				DurationUserBhvDao.getInstance().store(durationUserBhv);
			}
		}
	}

	private void preCollectCollectDurationUserBhv() {
		senseTime();
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> preExtractedDurationUserBhvList = 
					collector.preExtractDurationUserBhv(currTime, currTimeZone);
			registerAndStoreDurationUserBhv(preExtractedDurationUserBhvList);
		}
//		Log.d("BhvCollectionService", "pre-collection");
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

	private void postCollectDurationUserBhv() {
		senseTime();
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> postExtractedDurationUserBhvList = 
					collector.postExtractDurationUserBhv(currTime, currTimeZone);
			registerAndStoreDurationUserBhv(postExtractedDurationUserBhvList);
		}
		
//		Log.d("BhvCollectionService", "post-collection");
	}

	private void senseTime() {
		currTime = System.currentTimeMillis();
		currTimeZone = Calendar.getInstance().getTimeZone();
	}
}
