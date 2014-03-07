package lab.davidahn.appshuttle.collect.bhv;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AppUserBhv extends BaseUserBhv {
	
	public AppUserBhv(UserBhvType bhvType, String bhvName) {
		super(bhvType, bhvName);
	}
	
	@Override
	public boolean isValid(){
		AppShuttleApplication cxt = AppShuttleApplication.getContext();
		PackageManager packageManager = cxt.getPackageManager();
		Intent launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
		
		if(launchIntent == null)
			return false;
		
		if(bhvName.equals(cxt.getApplicationInfo().packageName)) //self
			return false;
		
		AppBhvCollector appUserBhvSensor = AppBhvCollector.getInstance();
		if(bhvName.startsWith(appUserBhvSensor.getHomePackageName())) //home
			return false;
		
		return true;
	}
}
//	getScreenLockPackage();
//	if(res.get(0).startsWith(getScreenLockPackage())) { //custom screen locker
//		res.remove(0);
//	}