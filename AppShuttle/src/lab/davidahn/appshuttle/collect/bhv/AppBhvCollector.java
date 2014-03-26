package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
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
	private List<String> lastAppHistory = null;
	
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
		 * (Modified 14.03.26)
		 * 지난 번과 현재의 최근 앱 목록을 비교하여 새로이 관찰된 앱 추가 
		 * 
		 * 설명
		 * - 두 개의 리스트 존재 (current / last)
		 * - current 리스트를 가장 오래된 것부터 하나씩 last 리스트와 비교해서 "순서대로" 매칭
		 * - last 리스트에서 건너뛰는 앱은 다시 실행해서 순서가 바뀌었거나, 사용저가 지웠단 뜻.
		 * - current 리스트에 있는 것 중에서, last 리스트에 매칭을 못시킨 애들은 새로 수행된 앱들.
		 * 
		 * 한계
		 * - Old history 에서와 정확히 같은 순서로 실행된 앱은 확인 불가
		 */
	    List<String> recentApps = getRecentAppHistory(Integer.MAX_VALUE, true);
	    
	    // 처음 켜질 때, 히스토리의 모든 앱이 추가되는 걸 방지 
	    if (lastAppHistory == null) {
	    	lastAppHistory = recentApps;
			return res;
		}

		List<BaseUserBhv> newAppBhvs = new ArrayList<BaseUserBhv>();
		
		ListIterator<String> li = recentApps.listIterator();
		ListIterator<String> li_last = lastAppHistory.listIterator();
		
		// Reverse iterator (old -> recent)
		while (li.hasPrevious()) {
			String app = li.previous();
			Boolean hasFound = false;
			
			while (li_last.hasPrevious()) {
				if (li_last.previous().equals(app)) {
					hasFound = true;
					break;
				}
			}
			
			if (!li_last.hasPrevious() && hasFound == false) {
				newAppBhvs.add(create(UserBhvType.APP, app));
			}
		}
		
		res.addAll(newAppBhvs);
		
		lastAppHistory = recentApps;
		
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
