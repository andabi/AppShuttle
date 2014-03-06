package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashMap;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;


public class BaseUserBhv implements UserBhv {
	protected UserBhvType bhvType;
	protected String bhvName;
	protected Map<String, Object> metas;
	
	public BaseUserBhv(UserBhvType bhvType, String bhvName) {
		this.bhvType = bhvType;
		this.bhvName = bhvName;
		metas = new HashMap<String, Object>();
	}
	
	//TODO userBhv pool 관리
	public static BaseUserBhv create(UserBhvType bhvType, String bhvName){
		switch (bhvType){
		case NONE:
			return new NoneUserBhv(bhvType, bhvName);
		case APP:
			return new AppUserBhv(bhvType, bhvName);
		case CALL:
			return new CallUserBhv(bhvType, bhvName);
		case SENSOR_ON:
			return new SensorOnUserBhv(bhvType, bhvName);
		default:
			throw new IllegalArgumentException("unknown bhv type");
		}
	}
	
	@Override
	public Intent getLaunchIntent() {
		Intent launchIntent = null;
		
		UserBhvType bhvType = getBhvType();
		String bhvName = getBhvName();
		switch(bhvType){
		case APP:
			PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
			launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
			break;
		case CALL:
			launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
			break;
		case SENSOR_ON:
			if(bhvName.equals(SensorType.WIFI.name()))
				launchIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
			break;
		case NONE:
			break;
		default:
			;
		}
		
		return launchIntent;
	}

	@Override
	public UserBhvType getBhvType() {
		return bhvType;
	}
	
	@Override
	public void setBhvType(UserBhvType bhvType) {
		this.bhvType = bhvType;
	}
	
	@Override
	public String getBhvName() {
		return bhvName;
	}
	
	@Override
	public void setBhvName(String bhvName) {
		this.bhvName = bhvName;
	}
	
	public Map<String, Object> getMetas() {
		return metas;
	}
	
	public void setMetas(Map<String, Object> metas) {
		this.metas = metas;
	}

	@Override
	public Object getMeta(String key) {
		return metas.get(key);
	}
	
	@Override
	public void setMeta(String key, Object val){
		metas.put(key, val);
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(bhvType.name()).append(": ").append(bhvName);
//		msg.append("metas: ").append(metas.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if((o instanceof UserBhv) 
				&& bhvName.equals(((UserBhv)o).getBhvName()) 
				&& bhvType == ((UserBhv)o).getBhvType())
			return true;
		else 
			return false;
	}

	@Override
	public int hashCode(){
		return bhvType.hashCode() ^ bhvName.hashCode();
	}
	
	public boolean isValid() {
		return true;
	}
}