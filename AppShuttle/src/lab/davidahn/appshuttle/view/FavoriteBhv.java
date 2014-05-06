package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import android.text.format.DateUtils;

public class FavoriteBhv extends ViewableUserBhv implements Comparable<FavoriteBhv> {
	private long setTime;
	private boolean isNotifiable;
	private int order;
	
	public FavoriteBhv(UserBhv uBhv, long _setTime, boolean _isNotifiable, int _order){
		super(uBhv);
		setTime = _setTime;
		isNotifiable = _isNotifiable;
		order = _order;
	}

	public long getSetTime() {
		return setTime;
	}
	
	public boolean isNotifiable() {
		return isNotifiable;
	}
	
	public int getOrder() {
		return order;
	}

	public void setNotifiable(boolean _isNotifiable) {
		isNotifiable = _isNotifiable;
	}
	
	public void setOrder(int _order) {
		order = _order;
	}
	
	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		
		msg.append(DateUtils.getRelativeTimeSpanString(setTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0
				));
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public Integer getNotibarContainerId() {
		if(isNotifiable())
			return R.id.noti_favorite_container;
		else
			return R.id.noti_present_container;
	}

	@Override
	public int compareTo(FavoriteBhv uBhv) {
		if(order > uBhv.order) return 1;
		else if(order < uBhv.order) return -1;
		
		if(!isNotifiable() && uBhv.isNotifiable()) return 1;
		else if(isNotifiable() && !uBhv.isNotifiable()) return -1;
			
		if(setTime > uBhv.setTime) return 1;
		else if(setTime == uBhv.setTime) return 0;
		else return -1;
	}
}