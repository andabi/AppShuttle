package lab.davidahn.appshuttle.predict.matcher.time;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import android.app.AlarmManager;

public class DailyWeekdayTimeMatcher extends TimeMatcher {
	private static final long INTERVAL_WEEK = 7 * AlarmManager.INTERVAL_DAY;

	public DailyWeekdayTimeMatcher(MatcherConf conf){
		super(conf);
		
		if(conf.getDuration() % INTERVAL_WEEK != 0)
			throw new IllegalArgumentException("duration must be times of a week");
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.TIME_DAILY_WEEKDAY;
	}
	
	@Override
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt) {
		if(isWeekDay(currUCxt.getTime()))
			return true;
		
		return false;
	}
	
	@Override
	protected List<DurationUserBhv> rejectNotUsedHistory(List<DurationUserBhv> durationUserBhvs, SnapshotUserCxt currUCxt) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvs){
			long timePast = currUCxt.getTime() - durationUserBhv.getTime();
			if(timePast < conf.getDuration() && timePast > conf.getTimeTolerance() 
					&& isWeekDay(durationUserBhv.getTime()))
				res.add(durationUserBhv);
		}
		return res;
	}
}
