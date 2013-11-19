package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;

public enum MatcherType {
	//recent
	FREQUENTLY_RECENT(0, R.string.predict_frequently_recent_msg),
	INSTANTALY_RECENT(1, R.string.predict_instantly_recent_msg),

	//time
	TIME_DAILY_WEEKDAY(0, R.string.predict_time_daily_weekday_msg),
	TIME_DAILY(1, R.string.predict_time_daily_msg),
	
	//location
	PLACE(0, R.string.predict_place_msg),
	LOCATION(1, R.string.predict_gps_msg),
	MOVE(2, R.string.predict_move_msg);
//	UNFAMILIER_PLACE

	
	public int priority;
	public String viewMsg;
	
	MatcherType(int _priority, int viewMsgId){
		priority = _priority;
		viewMsg = AppShuttleApplication.getContext().getString(viewMsgId);
	}
}