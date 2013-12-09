package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.List;

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

public class AppBhvCollector extends BaseBhvCollector {
	private ActivityManager activityManager;
	private PackageManager packageManager;
	private PowerManager powerManager;
	private KeyguardManager keyguardManager;

//	private Map<BaseUserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;

	private static AppBhvCollector appBhvCollector = new AppBhvCollector();

	private AppBhvCollector(){
		super();
		activityManager = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		packageManager = cxt.getPackageManager();
		powerManager = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE); 
	    keyguardManager = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);
	    
//		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
	}
	
	public static AppBhvCollector getInstance(){
		return appBhvCollector;
	}
	
	public List<BaseUserBhv> collect() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		res.addAll(collectActivityBhv());
		res.addAll(collectServiceBhv());
		return res;
	}
	
	private List<BaseUserBhv> collectActivityBhv() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		
	    if (!powerManager.isScreenOn()) { //screen off
	    	res.add(create(UserBhvType.NONE, "screen.off"));
//			Log.d("collection", "screen off");
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				res.add(create(UserBhvType.NONE, "lock.screen.on"));
//				Log.d("collection", "lock screen on");
	    } else {
			for(String bhvName : getPresentActivityList()){
				res.add(create(UserBhvType.APP, bhvName));
			}
	    }
	    return res;
	}

//	@Override
//	public List<DurationUserBhv> extractDurationUserBhv(Date currTime, TimeZone currTimezone, List<BaseUserBhv> userBhvList) {
//		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
//		long adjustment = preferenceSettings.getLong("collection.period", 10000) / 2;
//
//		if(durationUserBhvBuilderMap.isEmpty()) {
//			for(BaseUserBhv uBhv : userBhvList){
//				durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
//				, new Date(currTime.getTime() + adjustment)
//				, currTimezone
//				, uBhv));
//			}
//		} else {
//			for(BaseUserBhv uBhv : userBhvList){
//				if(durationUserBhvBuilderMap.containsKey(uBhv)){
//					DurationUserBhv.Builder durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
//					durationUserBhvBuilder.setEndTime(new Date(currTime.getTime() + adjustment)).setTimeZone(currTimezone);
//				} else {
//					durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
//					, new Date(currTime.getTime() + adjustment)
//					, currTimezone
//					, uBhv));
//				}
//			}
//			for(BaseUserBhv uBhv : new HashSet<BaseUserBhv>((durationUserBhvBuilderMap.keySet()))){
//				DurationUserBhv.Builder _durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
//				if(currTime.getTime() - _durationUserBhvBuilder.getEndTime().getTime() 
//						> preferenceSettings.getLong("collection.period", 10000) * 1.5){
//					res.add(_durationUserBhvBuilder.build());
//					durationUserBhvBuilderMap.remove(uBhv);
//				}
//			}
//		}
//		return res;
//	}
//	
//	@Override
//	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
//		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
//
////		for(UserBhv ongoingBhv : new HashSet<UserBhv>(ongoingBhvBuilderMap.keySet())){
//		for(BaseUserBhv uBhv : durationUserBhvBuilderMap.keySet()){
//			DurationUserBhv.Builder durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
//			if(currTimeDate.getTime() - durationUserBhvBuilder.getEndTime().getTime() 
//					> preferenceSettings.getLong("collection.period", 10000) * 1.5){
//				res.add(durationUserBhvBuilder.build());
////				ongoingBhvBuilderMap.remove(ongoingBhv);
//			}
//		}
//		
//		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
//		
//		return res;
//	}
//	
//	private DurationUserBhv.Builder createDurationUserBhvBuilder(Date time, Date endTime, TimeZone currTimeZone, BaseUserBhv bhv) {
//		return new DurationUserBhv.Builder()
//		.setTime(time)
//		.setEndTime(endTime)
//		.setTimeZone(currTimeZone)
//		.setBhv(bhv);
//	}
	
	private List<BaseUserBhv> collectServiceBhv() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
//		for(String bhvName : applicationManager.getCurrentService()){
//			uCxt.addUserBhv(new AppUserBhv("service", bhvName));
//		}
	    return res;
	}
	
	private List<String> getPresentActivityList(){
		List<String> res = new ArrayList<String>();
		try {
			res.addAll(getPresentApp(1, true));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@SuppressWarnings("unused")
	private List<String> getPresentServiceList(){
		List<String> res = new ArrayList<String>();
		res.addAll(getPresentService(0, false));
		return res;

	}
	
	@SuppressWarnings("unused")
	private List<String> getInstalledAppList(boolean includeSystemApp){
		List<String> res = new ArrayList<String>();
		for(ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){
			if(!includeSystemApp && isSystemApp(appInfo))
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

	private List<String> getPresentApp(int max, boolean includeSystemApp) throws NameNotFoundException{
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		List<String> res = new ArrayList<String>();
		String packageName = "";
		for(RunningTaskInfo taskInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)){
			packageName = taskInfo.baseActivity.getPackageName();
			if(!includeSystemApp && isSystemApp(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
				continue;
//			res.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA).processName);
			res.add(packageName);
			if(res.size() >= max) break;
		}
		return res;
	}

	private List<String> getPresentService(int max, boolean includeSystemApp){
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		List<String> res = new ArrayList<String>();
		for(RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if(!includeSystemApp && isSystemService(serviceInfo))
				continue;
			res.add(serviceInfo.service.getClassName());
			if(res.size() >= max) break;
		}
		return res;
	}

	public String getHomePackageName(){
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
