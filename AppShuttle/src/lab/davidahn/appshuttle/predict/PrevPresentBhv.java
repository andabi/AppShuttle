package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.bhv.UserBhv;
import android.text.format.DateUtils;

public class PrevPresentBhv extends PresentBhv {

	public PrevPresentBhv(UserBhv uBhv) {
		super(uBhv);
	}

	@Override
	public boolean isAlive(){
		return false;
	}

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		
		long recentPredictionTime = getRecentOfFirstPredictionInfo().getTimeDate().getTime();

		msg.append(DateUtils.getRelativeTimeSpanString(recentPredictionTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0
				));
		viewMsg = msg.toString();
		
		return viewMsg;
	}
}