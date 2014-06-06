package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;

public class CandidateFavoriteBhv extends ViewableUserBhv implements Comparable<CandidateFavoriteBhv> {
	private long lastUsedTime;
	
	public CandidateFavoriteBhv(UserBhv uBhv, long _lastUsedTime){
		super(uBhv);
		lastUsedTime = _lastUsedTime;
	}

	public long getSetTime() {
		return lastUsedTime;
	}
	
	@Override
	public String getBhvNameText() {
		bhvNameText = "";
		
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
			String contactName = CallBhvCollector.getInstance().getContactName(bhvName);
			if (contactName == null)
				contactName = (String) uBhv.getMetas().get("cachedName");
			if(contactName == null)
				contactName = "";
			bhvNameText = contactName + " (" + bhvName +")";
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
		
		msg.append(DateUtils.getRelativeTimeSpanString(lastUsedTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0
				));
		
		viewMsg = msg.toString();

		return viewMsg;
	}

	@Override
	public int compareTo(CandidateFavoriteBhv uBhv) {
		if(lastUsedTime > uBhv.lastUsedTime) return 1;
		else if(lastUsedTime == uBhv.lastUsedTime) return 0;
		else return -1;
	}

	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.CANDIDATE_FAVORITE;
	}
}