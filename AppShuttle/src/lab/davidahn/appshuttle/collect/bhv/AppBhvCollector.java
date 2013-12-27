package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import android.app.ActivityManager;
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

	private static AppBhvCollector appBhvCollector = new AppBhvCollector();

	private AppBhvCollector(){
		super();
		activityManager = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		packageManager = cxt.getPackageManager();
		powerManager = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE); 
	    keyguardManager = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);
	}
	
	public static AppBhvCollector getInstance(){
		return appBhvCollector;
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(String packageName : getRecentApp(Integer.MAX_VALUE, true)){
			res.add(new DurationUserBhv.Builder()
			.setBhv(create(UserBhvType.APP, packageName))
			.setTime(currTimeDate)
			.setEndTime(currTimeDate)
			.setTimeZone(currTimeZone)
			.build());
		}
		return res;
	}
	
	public List<BaseUserBhv> collect() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		res.addAll(collectActivityBhv());
//		res.addAll(collectServiceBhv());
		return res;
	}
	
	private List<BaseUserBhv> collectActivityBhv() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		
	    if (!powerManager.isScreenOn()) { //screen off
	    	res.add(create(UserBhvType.NONE, "screen.off"));
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				res.add(create(UserBhvType.NONE, "lock.screen.on"));
	    } else {
			for(String bhvName : getPresentApp(1, true)){
				res.add(create(UserBhvType.APP, bhvName));
			}
	    }
	    return res;
	}

	@SuppressWarnings("unused")
	private Set<String> getInstalledAppList(boolean includeSystemApp){
		Set<String> res = new HashSet<String>();
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

	private Set<String> getPresentApp(int max, boolean includeSystemApp) {
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		Set<String> res = new HashSet<String>();
		String packageName = "";
		for(RunningTaskInfo taskInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)){
			packageName = taskInfo.baseActivity.getPackageName();
			try {
				if(!includeSystemApp && isSystemApp(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
					continue;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
//			res.add(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA).processName);
			res.add(packageName);
			if(res.size() >= max) break;
		}
		return res;
	}
	
	private Set<String> getRecentApp(int max, boolean includeSystemApp) {
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		Set<String> res = new HashSet<String>();
		String packageName = "";
		for(ActivityManager.RecentTaskInfo recentInfo : activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE)){
			Intent intent = new Intent(recentInfo.baseIntent);
            if(recentInfo.origActivity != null)
                intent.setComponent(recentInfo.origActivity);

            packageName = packageManager.resolveActivity(intent, 0).activityInfo.packageName;
			try {
				if(!includeSystemApp && isSystemApp(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
					continue;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			res.add(packageName);
			if(res.size() >= max) break;
		}
		return res;
	}
	

//	private List<BaseUserBhv> collectServiceBhv() {
//		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
//		for(String bhvName : getPresentService(0, false)){
//			uCxt.addUserBhv(new AppUserBhv("service", bhvName));
//		}
//	    return res;
//	}
	
//	private boolean isSystemService(RunningServiceInfo serviceInfo) {
//    return ((serviceInfo.flags & RunningServiceInfo.FLAG_SYSTEM_PROCESS) != 0) ? true : false;
//}
	
//	private Set<String> getPresentService(int max, boolean includeSystemApp){
//		if(max < 0) return null;
//		if(max == 0) max = Integer.MAX_VALUE;
//		
//		Set<String> res = new HashSet<String>();
//		for(RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
//			if(!includeSystemApp && isSystemService(serviceInfo))
//				continue;
//			res.add(serviceInfo.service.getClassName());
//			if(res.size() >= max) break;
//		}
//		return res;
//	}

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
