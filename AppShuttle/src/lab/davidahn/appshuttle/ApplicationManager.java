package lab.davidahn.appshuttle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class ApplicationManager {
	private static ApplicationManager applicationManager;
	
	private ActivityManager am;
	private PackageManager pm;
	private KeyguardManager km;
	private PowerManager powerM;
	
	private ApplicationManager(Context cxt){
		am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		pm = cxt.getPackageManager();
	    km = (KeyguardManager) cxt.getSystemService(Context.KEYGUARD_SERVICE);  
	    powerM = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE); 
	}
	
	public static ApplicationManager getInstance(Context cxt){
		if(applicationManager == null) applicationManager = new ApplicationManager(cxt);
		return applicationManager;
	}
	
	public List<String> getCurrentActivity(){
		List<String> res = new ArrayList<String>();
		try {
		    if (!powerM.isScreenOn()) { //screen off
		    	res.add("screen.off");
	        }
			else {
			    if (km.inKeyguardRestrictedInputMode()) { //lock screen on
			    	res.add("lock.screen.on");
			    } else {
					res.addAll(getCurrentApp(1, true));
//					getScreenLockPackage();
//					if(res.get(0).startsWith(getScreenLockPackage())) { //custom screen locker
//						res.remove(0);
//					}
					if(res.get(0).startsWith(getHomePackage())) { //home
						res.remove(0);
						res.add("home.screen");
					} else {
						
					}
			    }
		    }
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public Set<String> getCurrentService(){
		Set<String> res = new HashSet<String>();
		res.addAll(getCurrentService(0, false));
		return res;

	}
	
	private List<String> getInstalledApp(boolean getSysApp){
		List<String> res = new ArrayList<String>();
		for(ApplicationInfo appInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)){
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
		for(RunningTaskInfo taskInfo : am.getRunningTasks(Integer.MAX_VALUE)){
			packageName = taskInfo.baseActivity.getPackageName();
			if(!getSysApp && isSystemApp(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)))
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
		for(RunningServiceInfo serviceInfo : am.getRunningServices(Integer.MAX_VALUE)) {
			if(!getSysService && isSystemService(serviceInfo))
				continue;
			res.add(serviceInfo.service.getClassName());
			if(res.size() >= max) break;
		}
		return res;
	}

	private String getHomePackage(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
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
