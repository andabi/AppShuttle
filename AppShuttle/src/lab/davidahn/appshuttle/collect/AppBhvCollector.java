package lab.davidahn.appshuttle.collect;

import static lab.davidahn.appshuttle.Settings.preferenceSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.AppUserBhv;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.PowerManager;
import android.util.Log;

public class AppBhvCollector implements BhvCollector {
	private static AppBhvCollector appBhvCollector;
	
	private ActivityManager activityManager;
	private PackageManager packageManager;
	private PowerManager powerManager;
	private KeyguardManager keyguardManager;

	private Map<UserBhv, DurationUserBhv.Builder> ongoingBhvMap;

	private AppBhvCollector(Context cxt){
		activityManager = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		packageManager = cxt.getPackageManager();
		powerManager = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE); 
	    keyguardManager = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);
		ongoingBhvMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
	}
	
	public synchronized static AppBhvCollector getInstance(Context cxt){
		if(appBhvCollector == null) 
			appBhvCollector = new AppBhvCollector(cxt);
		return appBhvCollector;
	}
	
	public List<UserBhv> collect() {
		List<UserBhv> res = new ArrayList<UserBhv>();
		res.addAll(collectActivityBhv());
		res.addAll(collectServiceBhv());
		return res;
	}
	
	private List<UserBhv> collectActivityBhv() {
		List<UserBhv> res = new ArrayList<UserBhv>();
	    if (!powerManager.isScreenOn()) { //screen off
	    	res.add(new UserBhv(BhvType.NONE, "screen.off"));
			Log.d("collection", "screen off");
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				res.add(new UserBhv(BhvType.NONE, "lock.screen.on"));
				Log.d("collection", "lock screen on");
	    } else {
			for(String bhvName : getCurrentActivity()){
				res.add(new AppUserBhv(BhvType.APP, bhvName));
			}
	    }
	    return res;
	}
	
	public List<DurationUserBhv> refineDurationUserBhv(Date currTime, TimeZone timezone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		long adjustment = preferenceSettings.getLong("service.collection.period", 10000) / 2;

		if(ongoingBhvMap.isEmpty()) {
			for(UserBhv uBhv : userBhvList){
				ongoingBhvMap.put(uBhv, makeDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
				, new Date(currTime.getTime() + adjustment)
				, timezone
				, uBhv));
			}
		} else {
			for(UserBhv uBhv : userBhvList){
				if(ongoingBhvMap.containsKey(uBhv)){
					DurationUserBhv.Builder rfdUCxtBuilder = ongoingBhvMap.get(uBhv);
					rfdUCxtBuilder.setEndTime(new Date(currTime.getTime() + adjustment));
				} else {
					ongoingBhvMap.put(uBhv, makeDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
					, new Date(currTime.getTime() + adjustment)
					, timezone
					, uBhv));
				}
			}
			Set<UserBhv> ongoingBhvSet = new HashSet<UserBhv>(ongoingBhvMap.keySet());
			for(UserBhv ongoingBhv : ongoingBhvSet){
				DurationUserBhv.Builder ongoingRfdUCxtBuilder = ongoingBhvMap.get(ongoingBhv);
				if(currTime.getTime() - ongoingRfdUCxtBuilder.getEndTime().getTime() 
						> preferenceSettings.getLong("service.collection.period", 6000) * 1.5){
					res.add(ongoingRfdUCxtBuilder.build());
					ongoingBhvMap.remove(ongoingBhv);
				}
			}
		}
		return res;
	}
	
	private DurationUserBhv.Builder makeDurationUserBhvBuilder(Date time, Date endTime, TimeZone currTimeZone, UserBhv bhv) {
		return new DurationUserBhv.Builder()
		.setTime(time)
		.setEndTime(endTime)
		.setTimeZone(currTimeZone)
		.setBhv(bhv);
	}
	
	private List<UserBhv> collectServiceBhv() {
		List<UserBhv> res = new ArrayList<UserBhv>();
//		for(String bhvName : applicationManager.getCurrentService()){
//			uCxt.addUserBhv(new AppUserBhv("service", bhvName));
//		}
	    return res;
	}
	
	private List<String> getCurrentActivity(){
		List<String> res = new ArrayList<String>();
		try {
			res.addAll(getCurrentApp(1, true));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	private Set<String> getCurrentService(){
		Set<String> res = new HashSet<String>();
		res.addAll(getCurrentService(0, false));
		return res;

	}
	
	private List<String> getInstalledApp(boolean getSysApp){
		List<String> res = new ArrayList<String>();
		for(ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){
			if(!getSysApp && isSystemApp(appInfo))
				continue;
			res.add(appInfo.packageName);
		}
		return res;
	}

	private boolean isSystemApp(ApplicationInfo appInfo) {
	    return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
	}

	private boolean isSystemService(RunningServiceInfo serviceInfo) {
	    return ((serviceInfo.flags & RunningServiceInfo.FLAG_SYSTEM_PROCESS) != 0) ? true : false;
	}


	private List<String> getCurrentApp(int max, boolean getSysApp) throws NameNotFoundException{
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		List<String> res = new ArrayList<String>();
		String packageName = "";
		for(RunningTaskInfo taskInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)){
			packageName = taskInfo.baseActivity.getPackageName();
			if(!getSysApp && isSystemApp(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
				continue;
//			res.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA).processName);
			res.add(packageName);
			if(res.size() >= max) break;
		}
		return res;
	}

	private List<String> getCurrentService(int max, boolean getSysService){
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		List<String> res = new ArrayList<String>();
		for(RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if(!getSysService && isSystemService(serviceInfo))
				continue;
			res.add(serviceInfo.service.getClassName());
			if(res.size() >= max) break;
		}
		return res;
	}

	public String getHomePackage(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return resolveInfo.activityInfo.packageName;
	}
	
//	private String getScreenLockPackage(){
//		Intent intent = new Intent(Intent.ACTION_SCREEN_ON);
////		intent.addCategory(Intent.CATEGORY_HOME);
//		ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//		Log.d("locker", resolveInfo.activityInfo.packageName);
//		return resolveInfo.activityInfo.packageName;
//	}

	//for text
	//	for (ApplicationInfo packageInfo : am.getInstalledApplication(false)) {
	//    Log.d("installed task", "Installed package :" + packageInfo.packageName);
	//}
	//for (String app : am.getCurrentApp(10)){
	//	Log.d("currentApp", app);
	//}
	//for (String service : am.getCurrentService(10)){
	//	Log.d("currentService", service);
	//}
}
