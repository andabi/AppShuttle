package lab.davidahn.appshuttle.context.bhv;

import lab.davidahn.appshuttle.collect.AppUserBhvSensor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AppUserBhv extends UserBhv {
	
	public AppUserBhv(String bhvType, String bhvName) {
		super(bhvType, bhvName);
	}
	
	public boolean isValid(Context cxt){
		PackageManager packageManager = cxt.getPackageManager();
		Intent launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
		
		if(launchIntent == null || bhvName.equals(cxt.getApplicationInfo().packageName)) //self
			return false;
		
		AppUserBhvSensor appUserBhvSensor = AppUserBhvSensor.getInstance(cxt);
		if(bhvName.startsWith(appUserBhvSensor.getHomePackage())) { //home
			return false;
		}
		
		return true;
	//	getScreenLockPackage();
	//	if(res.get(0).startsWith(getScreenLockPackage())) { //custom screen locker
	//		res.remove(0);
	//	}
	}
}