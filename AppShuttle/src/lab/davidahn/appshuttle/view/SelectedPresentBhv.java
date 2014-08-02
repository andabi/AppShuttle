package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import android.text.format.DateUtils;

public class SelectedPresentBhv extends PresentBhv implements Comparable<SelectedPresentBhv> {
	private long lastUsedTime;
	
	public SelectedPresentBhv(UserBhv uBhv, long _lastUsedTime) {
		super(uBhv);
		lastUsedTime = _lastUsedTime;
	}
	
	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.SELECTED;
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
	public int compareTo(SelectedPresentBhv uBhv) {
		if(lastUsedTime > uBhv.lastUsedTime) return 1;
		else if(lastUsedTime == uBhv.lastUsedTime) return 0;
		else return -1;
	}
}
