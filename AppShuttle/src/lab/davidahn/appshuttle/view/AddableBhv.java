package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;

public class AddableBhv extends ViewableUserBhv implements Comparable<AddableBhv> {
	private long lastUsedTime;
	
	public AddableBhv(UserBhv uBhv, long _lastUsedTime){
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
		
		CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(lastUsedTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0);
		
		Context cxt = AppShuttleApplication.getContext();
		switch(uBhv.getBhvType()){
		case APP:
			msg.append(String.format(cxt.getString(R.string.addable_bhv_view_msg_app), relativeTimeSpan));
			break;
		case CALL:
			if(lastUsedTime > 0)
				msg.append(String.format(cxt.getString(R.string.addable_bhv_view_msg_call), relativeTimeSpan));
			break;
		default:
			msg.append(relativeTimeSpan);
		}		

		viewMsg = msg.toString();

		return viewMsg;
	}

	@Override
	public int compareTo(AddableBhv uBhv) {
		if(lastUsedTime > uBhv.lastUsedTime) return 1;
		else if(lastUsedTime == uBhv.lastUsedTime) return 0;
		else return -1;
	}

	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.ADDABLE;
	}
}