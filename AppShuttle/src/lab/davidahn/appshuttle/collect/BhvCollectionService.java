package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class BhvCollectionService extends Service {
	private Date currTimeDate;
	private TimeZone currTimeZone;
    private List<BhvCollector> collectors;

    @Override
	public void onCreate() {
		super.onCreate();
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
		senseBhvAndUpdateCxt(AppShuttleApplication.currUserCxt);
		extractAndStoreSnapshotAppUserBhvAsDurationUserBhv(AppShuttleApplication.currUserCxt);
		extractAndStoreDurationUserBhv(AppShuttleApplication.currUserCxt);

		return START_NOT_STICKY;
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
		collectors = new ArrayList<BhvCollector>();
		collectors.add(AppBhvCollector.getInstance());
		collectors.add(CallBhvCollector.getInstance());
		collectors.add(SensorOnCollector.getInstance());
	}

	private void senseBhvAndUpdateCxt(SnapshotUserCxt uCxt) {
		uCxt.setTime(currTimeDate);
		uCxt.setTimeZone(currTimeZone);
		for(BhvCollector collector : collectors){
			List<BaseUserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
		}
//		storeSnapshotCxt(uCxt);
	}
	
	private void extractAndStoreDurationUserBhv(SnapshotUserCxt uCxt) {
		Date currTimeDate = uCxt.getTimeDate();
		TimeZone currTimeZone = uCxt.getTimeZone();
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> durationUserBhvList = 
					collector.extractDurationUserBhv(currTimeDate, currTimeZone, uCxt.getUserBhvs());
			registerAndStoreDurationUserBhv(durationUserBhvList);
		}
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
		registerAndStoreDurationUserBhv(snapshotAppUserBhvList);
	}

	private void registerAndStoreDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
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

	private void preCollectCollectDurationUserBhv() {
		senseTime();
		for(BhvCollector collector : collectors){
			List<DurationUserBhv> preExtractedDurationUserBhvList = 
					collector.preExtractDurationUserBhv(currTimeDate, currTimeZone);
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
					collector.postExtractDurationUserBhv(currTimeDate, currTimeZone);
			registerAndStoreDurationUserBhv(postExtractedDurationUserBhvList);
		}
		
		Log.d("BhvCollectionService", "post-collection");
	}

	private void senseTime() {
		currTimeDate = new Date(System.currentTimeMillis());
		currTimeZone = Calendar.getInstance().getTimeZone();
	}
}
