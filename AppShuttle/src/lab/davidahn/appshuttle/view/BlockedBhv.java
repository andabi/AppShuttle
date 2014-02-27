package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
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
//		long blockedTime = ((BlockedUserBhv)_uBhv).getBlockedTime();
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		
		msg.append(DateUtils.getRelativeTimeSpanString(blockedTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0
				));
		viewMsg = msg.toString();
		
		return viewMsg;
	}

	@Override
	public Integer getNotibarContainerId() {
		return null;
	}
}
