package lab.davidahn.appshuttle.collect;

import static lab.davidahn.appshuttle.context.bhv.UserBhv.create;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
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

/**
 * singleton 
 * @author andabi
 * 
 */
public class AppBhvCollector extends BaseBhvCollector {
	private ActivityManager _activityManager;
	private PackageManager _packageManager;
	private PowerManager _powerManager;
	private KeyguardManager _keyguardManager;

	private Map<UserBhv, DurationUserBhv.Builder> _durationUserBhvBuilderMap;

	private static AppBhvCollector _appBhvCollector = new AppBhvCollector();

	private AppBhvCollector(){
		super();
		_activityManager = (ActivityManager) _appShuttleContext.getSystemService(Context.ACTIVITY_SERVICE);
		_packageManager = _appShuttleContext.getPackageManager();
		_powerManager = (PowerManager) _appShuttleContext.getSystemService(Context.POWER_SERVICE); 
	    _keyguardManager = (KeyguardManager) _appShuttleContext.getSystemService(Context.KEYGUARD_SERVICE);
	    
		_durationUserBhvBuilderMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
	}
	
	public static AppBhvCollector getInstance(){
		return _appBhvCollector;
	}
	
	public List<UserBhv> collect() {
		List<UserBhv> res = new ArrayList<UserBhv>();
		
		res.addAll(collectActivityBhv());
		res.addAll(collectServiceBhv());
		
		return res;
	}
	
	private List<UserBhv> collectActivityBhv() {
		List<UserBhv> res = new ArrayList<UserBhv>();
		
	    if (!_powerManager.isScreenOn()) { //screen off
	    	res.add(create(BhvType.NONE, "screen.off"));
//			Log.d("collection", "screen off");
        } else if (_keyguardManager.inKeyguardRestrictedInputMode()) { //lock screen on
				res.add(create(BhvType.NONE, "lock.screen.on"));
//				Log.d("collection", "lock screen on");
	    } else {
			for(String bhvName : getPresentActivityList()){
				res.add(create(BhvType.APP, bhvName));
			}
	    }
	    return res;
	}

	@Override
	public List<DurationUserBhv> extractDurationUserBhv(Date currTime, TimeZone currTimezone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		long adjustment = _preferenceSettings.getLong("service.collection.period", 10000) / 2;

		if(_durationUserBhvBuilderMap.isEmpty()) {
			for(UserBhv uBhv : userBhvList){
				_durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
				, new Date(currTime.getTime() + adjustment)
				, currTimezone
				, uBhv));
			}
		} else {
			for(UserBhv uBhv : userBhvList){
				if(_durationUserBhvBuilderMap.containsKey(uBhv)){
					DurationUserBhv.Builder rfdUCxtBuilder = _durationUserBhvBuilderMap.get(uBhv);
					rfdUCxtBuilder.setEndTime(new Date(currTime.getTime() + adjustment)).setTimeZone(currTimezone);
				} else {
					_durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
					, new Date(currTime.getTime() + adjustment)
					, currTimezone
					, uBhv));
				}
			}
			for(UserBhv uBhv : new HashSet<UserBhv>((_durationUserBhvBuilderMap.keySet()))){
				DurationUserBhv.Builder _durationUserBhvBuilder = _durationUserBhvBuilderMap.get(uBhv);
				if(currTime.getTime() - _durationUserBhvBuilder.getEndTime().getTime() 
						> _preferenceSettings.getLong("service.collection.period", 10000) * 1.5){
					res.add(_durationUserBhvBuilder.build());
					_durationUserBhvBuilderMap.remove(uBhv);
				}
			}
		}
		return res;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

//		for(UserBhv ongoingBhv : new HashSet<UserBhv>(ongoingBhvBuilderMap.keySet())){
		for(UserBhv uBhv : _durationUserBhvBuilderMap.keySet()){
			DurationUserBhv.Builder durationUserBhvBuilder = _durationUserBhvBuilderMap.get(uBhv);
			if(currTimeDate.getTime() - durationUserBhvBuilder.getEndTime().getTime() 
					> _preferenceSettings.getLong("service.collection.period", 10000) * 1.5){
				res.add(durationUserBhvBuilder.build());
//				ongoingBhvBuilderMap.remove(ongoingBhv);
			}
		}
		
		_durationUserBhvBuilderMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
		
		return res;
	}
	
	private DurationUserBhv.Builder createDurationUserBhvBuilder(Date time, Date endTime, TimeZone currTimeZone, UserBhv bhv) {
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
		for(ApplicationInfo appInfo : _packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){
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
		for(RunningTaskInfo taskInfo : _activityManager.getRunningTasks(Integer.MAX_VALUE)){
			packageName = taskInfo.baseActivity.getPackageName();
			if(!includeSystemApp && isSystemApp(_packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
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
		for(RunningServiceInfo serviceInfo : _activityManager.getRunningServices(Integer.MAX_VALUE)) {
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
		ResolveInfo resolveInfo = _packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
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
