package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import android.content.Context;
import android.text.format.DateUtils;


public class BlockedBhv extends ViewableUserBhv implements Comparable<BlockedBhv> {
	private long blockedTime;
	
	public BlockedBhv(UserBhv uBhv, long blockedTime){
		super(uBhv);
		this.blockedTime = blockedTime;
	}
	
	public long getBlockedTime() {
		return blockedTime;
	}
	
	@Override
	public int compareTo(BlockedBhv uBhv) {
		if(blockedTime > uBhv.blockedTime)
			return 1;
		else if(blockedTime == uBhv.blockedTime)
			return 0;
		else
			return -1;
	}

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		
		CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(blockedTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0);
		
		Context cxt = AppShuttleApplication.getContext();
		msg.append(String.format(cxt.getString(R.string.ignore_bhv_view_msg), relativeTimeSpan));

		viewMsg = msg.toString();
		
		return viewMsg;
	}

	@Override
	public Integer getNotibarContainerId() {
		return null;
	}

	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.BLOCKED;
	}
}
