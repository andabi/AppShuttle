package lab.davidahn.appshuttle.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.PredictedBhvInfo;
import lab.davidahn.appshuttle.predict.PresentBhv;
import lab.davidahn.appshuttle.predict.PresentBhvManager;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

public class ViewableUserBhv implements UserBhv, Viewable {
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
	public Object getMeta(String key) {
		return uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		uBhv.setMeta(key, val);
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
			icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
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
		if(bhvNameText != null)
			return bhvNameText;
			
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
			bhvNameText = (String) (uBhv).getMeta("cachedName");
			if(bhvNameText == null || bhvNameText.equals(""))
				bhvNameText = uBhv.getBhvName();
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

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();

		PresentBhv recentPresentBhv = PresentBhvManager.getPresentBhvs().get(uBhv);
		
		if(recentPresentBhv == null)
			return viewMsg;

		PredictedBhvInfo predictionInfo = recentPresentBhv.getRecentPredictionInfo();
		
//		if(uBhv.getBhvName().equals("com.android.chrome")){
//			Log.d("test", recentPresentBhv.hashCode() + "");
//			Log.d("test", predictionInfo.getMatcherResultMap().keySet() + "");
//		}
		
		if(predictionInfo == null)
			return viewMsg;
		
		Map<MatcherGroupType, MatcherGroupResult> macherGroupResults = predictionInfo.getMatcherGroupResultMap();
		List<MatcherGroupType> matcherGroupTypeList = new ArrayList<MatcherGroupType>(macherGroupResults.keySet());
		Collections.sort(matcherGroupTypeList, new MatcherGroupTypeComparator());
		Collections.reverse(matcherGroupTypeList);
		
		for (MatcherGroupType matcherGroupType : matcherGroupTypeList) {
			msg.append(macherGroupResults.get(matcherGroupType).getViewMsg()).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public Intent getLaunchIntent() {
		UserBhvType bhvType = uBhv.getBhvType();
		String bhvName = uBhv.getBhvName();
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
	public Integer getNotibarContainerId() {
		return null;
	}
}
