package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;

public enum MatcherType {
	//frequency
	FREQUENCY_RECENT(0, R.string.predict_freq_recent_msg),
	RECENT(1, R.string.predict_recent_msg),

	//time
	TIME_DAILY(0, R.string.predict_time_daily_msg),
	TIME_DAILY_WEEKDAY(1, R.string.predict_time_daily_weekday_msg),
	
	//location
	PLACE(0, R.string.predict_place_msg),
	LOCATION(1, R.string.predict_location_msg);
//	UNFAMILIER_PLACE

	//move
//	SLOWLY_MOVE
//	FASTLY_MOVE
	
	public int priority;
	public String viewMsg;
	
	MatcherType(int _priority, int viewMsgId){
		priority = _priority;
		viewMsg = AppShuttleApplication.getContext().getString(viewMsgId);
	}
}