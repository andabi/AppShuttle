package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;

//TODO make it two-level
public enum MatcherType {
	FREQUENCY(0, true, R.string.predict_freq_msg),
	WEAK_TIME(1, true, R.string.predict_time_msg),
	PLACE(1, true, R.string.predict_place_msg),
	STRICT_TIME(2, false, R.string.predict_time_msg),
	LOCATION(2, false, R.string.predict_place_msg);
//	WEEKLY_TIME
//	UNFAMILIER_PLACE
	
	public int priority;
	public boolean enabled;
	public String viewMsg;
	
	MatcherType(int _priority, boolean _enabled, int viewMsgId){
		priority = _priority;
		enabled = _enabled;
		viewMsg = AppShuttleApplication.getContext().getString(viewMsgId);
	}

//	public int getPriority() {
//		return priority;
//	}
//	
//	public boolean enabled() {
//		return enabled;
//	}
//	
//	public String getViewMsg(){
//		return viewMsg;
//	}

}