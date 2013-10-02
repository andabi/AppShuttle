package lab.davidahn.appshuttle.context.bhv;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.AppBhvCollector;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AppUserBhv extends UserBhv {
	
	public AppUserBhv(BhvType bhvType, String bhvName) {
		super(bhvType, bhvName);
	}
	
	@Override
	public boolean isValid(){
		AppShuttleApplication appShuttleContext = AppShuttleApplication.getContext();
		
		PackageManager packageManager = appShuttleContext.getPackageManager();
		Intent launchIntent = packageManager.getLaunchIntentForPackage(_bhvName);
		
		if(launchIntent == null || _bhvName.equals(appShuttleContext.getApplicationInfo().packageName)) //self
			return false;
		
		AppBhvCollector appUserBhvSensor = AppBhvCollector.getInstance();
		if(_bhvName.startsWith(appUserBhvSensor.getHomePackageName())) { //home
			return false;
		}
		
		return true;
	}
}
//	getScreenLockPackage();
//	if(res.get(0).startsWith(getScreenLockPackage())) { //custom screen locker
//		res.remove(0);
//	}