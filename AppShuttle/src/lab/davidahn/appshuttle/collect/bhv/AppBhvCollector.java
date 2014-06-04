package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
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
	private String recentRunningAppPackageName = "";
//	private List<String> recentApps = new ArrayList<String>();
	
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
	public List<DurationUserBhv> preExtractDurationUserBhv(long currTime, TimeZone currTimeZone) {
		List<String> recentApps = getRecentAppHistory(Integer.MAX_VALUE, true);
		
		if(recentApps.isEmpty())
			return Collections.emptyList();
		
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		long depreciation = preferenceSettings.getLong("collection.bhv.app.pre.depreciation", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5);
		for(int i=0;i<recentApps.size();i++){
			long time = currTime - i * depreciation;
			res.add(new DurationUserBhv.Builder()
			.setBhv(create(UserBhvType.APP, recentApps.get(i)))
			.setTime(time)
			.setEndTime(time)
			.setTimeZone(currTimeZone)
			.build());
		}
		
		return res;
	}
	
	public List<UserBhv> collect() {
		List<UserBhv> res = new ArrayList<UserBhv>();
		res.addAll(collectActivityBhv());
//		res.addAll(collectServiceBhv());

		return res;
	}
	
	private List<BaseUserBhv> collectActivityBhv() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		
		//Currently running app
		BaseUserBhv runningAppBhv;
	    if (!powerManager.isScreenOn()) { //screen off
	    	runningAppBhv = create(UserBhvType.NONE, "screen.off");
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
        	runningAppBhv = create(UserBhvType.NONE, "lock.screen.on");
	    } else
	    	runningAppBhv = create(UserBhvType.APP, getPresentApp(1, true).get(0));
	    res.add(runningAppBhv);
	
	    /**
	     * New apps in history. 
	     * This is NOT complete even though the best currently.
	     * Some apps may not be caught in case recentRunningAppPackageName become same or
	     * user explicitly delete app history.
	     */
		List<String> recentApps = getRecentAppHistory(Integer.MAX_VALUE, true);
		List<BaseUserBhv> newAppBhvs = new ArrayList<BaseUserBhv>();
		if(recentApps.contains(recentRunningAppPackageName)){ //user did not manage history
			for(String app : recentApps){
				if(app.equals(recentRunningAppPackageName)) break;
				if(app.equals(runningAppBhv.getBhvName())) continue;
				newAppBhvs.add(create(UserBhvType.APP, app));
			}
		}
		res.addAll(newAppBhvs);
		
		recentRunningAppPackageName = runningAppBhv.getBhvName();
		
		return res;
	}

	/**
	 * @param boolean value about whether systemApp is included or not
	 * @return List of appName & icon
	 */
//	public static List<Pair<String, Drawable>> getInstalledAppList(boolean includeSystemApp){
//		PackageManager pm = AppShuttleApplication.getContext().getPackageManager();
//		List<Pair<String, Drawable>> res = new ArrayList<Pair<String, Drawable>>();
//		for(ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)){
//			if(!includeSystemApp && isSystemApp(appInfo))
//				continue;
//			try {
//				Drawable icon = (BitmapDrawable) pm.getApplicationIcon(appInfo.packageName);
////				String appname = pm.getApplicationLabel(appInfo).toString();
//				res.add(new Pair<String, Drawable>(appInfo.packageName, icon));
//			} catch (NameNotFoundException e) {}
//		}
//		return res;
//	}
	
	public List<UserBhv> getInstalledAppList(boolean includeSystemApp){
		List<UserBhv> res = new ArrayList<UserBhv>();
		for(ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){
			if(!includeSystemApp && isSystemApp(appInfo))
				continue;
			res.add(create(UserBhvType.APP, appInfo.packageName));
		}
		return res;
	}
	
	private static boolean isSystemApp(ApplicationInfo appInfo) {
	    return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
	}

	public List<String> getPresentApp(int max, boolean includeSystemApp) {
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		List<String> res = new ArrayList<String>();
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
	
	private List<String> getRecentAppHistory(int max, boolean includeSystemApp) {
		if(max < 0) return null;
		if(max == 0) max = Integer.MAX_VALUE;
		
		List<String> res = new ArrayList<String>();
		for(ActivityManager.RecentTaskInfo recentInfo : activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE)){
			Intent intent = new Intent(recentInfo.baseIntent);
            if(recentInfo.origActivity != null)
                intent.setComponent(recentInfo.origActivity);
            
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
            if(resolveInfo == null)
            	continue;
            
            String packageName = resolveInfo.activityInfo.packageName;
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
