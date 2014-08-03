package lab.davidahn.appshuttle.view;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.report.Sharable;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ViewableUserBhv implements UserBhv, Viewable, Sharable {
	protected UserBhv uBhv;
	protected Drawable icon;
	protected String bhvNameText;
	protected String viewMsg;
	protected Intent launchIntent;

	public ViewableUserBhv(UserBhv bhvInfo) {
		uBhv = bhvInfo;
	}
	
	public UserBhv getUserBhv() {
		return uBhv;
	}
	
	@Override
	public UserBhvType getBhvType() {
		return uBhv.getBhvType();
	}
	@Override
	public void setBhvType(UserBhvType bhvType) {
		uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		uBhv.setBhvName(bhvName);
	}
	@Override
	public Map<String, Object> getMetas() {
		return uBhv.getMetas();
	}
	@Override
	public void setMetas(Map<String, Object> metas) {
		uBhv.setMetas(metas);
	}
	@Override
	public boolean isValid() {
		return uBhv.isValid();
	}
	
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.NONE;
	}

	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return uBhv.hashCode();
	}
	
	@Override
	public Drawable getIcon() {
		if(icon != null)
			return icon;
		
		icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
		
		UserBhvType bhvType = uBhv.getBhvType();
		String bhvName = uBhv.getBhvName();
		switch(bhvType){
		case APP:
			PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
			String packageName = uBhv.getBhvName();
			try {
				icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
			} catch (NameNotFoundException e) {}
			break;
		case CALL:
			icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.call);
			break;
		case SENSOR_ON:
			if(bhvName.equals(SensorType.WIFI.name()))
				icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.wifi);
			break;
		case NONE:
			break;
		default:
			;
		}
		
		return icon;
	}

	@Override
	public String getBhvNameText() {
		/*
		if(bhvNameText != null)
			return bhvNameText;
		*/
		bhvNameText = "no name";
		
		UserBhvType bhvType = uBhv.getBhvType();
		String bhvName = uBhv.getBhvName();
		switch(bhvType){
		case APP:
			PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
			String packageName = uBhv.getBhvName();
			try {
				ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
				bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
			} catch (NameNotFoundException e) {}
			break;
		case CALL:
			bhvNameText = CallBhvCollector.getInstance().getContactName(bhvName);
			if (bhvNameText == null)
				bhvNameText = (String) uBhv.getMetas().get("cachedName");
			if(bhvNameText == null || bhvNameText.equals(""))
				bhvNameText = bhvName;
			break;
		case SENSOR_ON:
			if(bhvName.equals(SensorType.WIFI.name()))
				bhvNameText = AppShuttleApplication.getContext().getResources().getString(R.string.predict_notibar_msg_sensor_wifi);
			break;
		case NONE:
			break;
		default:
			;
		}
		
		return bhvNameText;
	}

	public String getViewMsg() {
		return null;
	}
	
	@Override
	public Intent getLaunchIntent() {
		if (uBhv == null)
			return null;
		
		return uBhv.getLaunchIntent();
	}
	
	public Integer getNotibarContainerId() {
		return null;
	}
	
	/**
	 * normal = not favorite & not blocked
	 */
	public static Set<UserBhv> getNormalBhvSet() {
		Set<UserBhv> res = new HashSet<UserBhv>(UserBhvManager.getInstance().getRegisteredBhvSet());
		res.removeAll(FavoriteBhvManager.getInstance().getFavoriteBhvSet());
		res.removeAll(BlockedBhvManager.getInstance().getBlockedBhvSet());
		return res;
	}

	@Override
	public boolean isSharable(){
		switch(getBhvType()){
		case SENSOR_ON:
			return false;
		default:
			return true;
		}
	}
	
	@Override
	public String getSharingMsg() {
		if(!isSharable())
			return null;
		
		return String.format(getSharingMsgFormat(), 
				getViewMsg().toLowerCase(Locale.getDefault()),
				getBhvNameText(),
				getSharingLink());
	}
	
	protected String getSharingMsgFormat() {
		final Context cxt = AppShuttleApplication.getContext();
		switch(getBhvType()){
		case APP:
			return cxt.getString(R.string.action_msg_share_app);
		case CALL:
			return cxt.getString(R.string.action_msg_share_call);
		default:
			return null;
		}
	}
	
	protected String getSharingLink(){
		switch(getBhvType()){
		case APP:
			return "https://play.google.com/store/apps/details?id=" + getBhvName();
		case CALL:
			return getBhvName();
		default:
			return null;
		}
	}
}
