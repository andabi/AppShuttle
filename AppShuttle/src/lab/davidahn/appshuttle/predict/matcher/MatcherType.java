package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;

public enum MatcherType {
	//recent
	FREQUENTLY_RECENT(0, R.string.predict_frequently_recent_msg, false),
	INSTANTALY_RECENT(1, R.string.predict_instantly_recent_msg, true),

	//time
	TIME_DAILY_WEEKDAY(0, R.string.predict_time_daily_weekday_msg, false),
	TIME_DAILY_WEEKEND(0, R.string.predict_time_daily_weekend_msg, false),
	TIME_DAILY(1, R.string.predict_time_daily_msg, false),
	
	//location
	PLACE(0, R.string.predict_place_msg, false),
	LOCATION(1, R.string.predict_gps_msg, false),
	MOVE(2, R.string.predict_move_msg, false),
	;
//	UNFAMILIER_PLACE

	public int priority;
	public String viewMsg;
	public boolean isOverwritableForNewPrediction;
	
	MatcherType(int _priority, int viewMsgId, boolean _isOverwritableForNewPrediction){
		priority = _priority;
		viewMsg = AppShuttleApplication.getContext().getString(viewMsgId);
		isOverwritableForNewPrediction = _isOverwritableForNewPrediction;
	}
}