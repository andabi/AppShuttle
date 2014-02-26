package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;
import android.app.AlarmManager;

public class DailyWeekdayTimeMatcher extends TimeMatcher {
	private static final long INTERVAL_WEEK = 7 * AlarmManager.INTERVAL_DAY;

	public DailyWeekdayTimeMatcher(TimeMatcherConf conf){
		super(conf);
		
		if(conf.getDuration() % INTERVAL_WEEK != 0)
			throw new IllegalArgumentException("duration must be times of a week");
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.TIME_DAILY_WEEKDAY;
	}
	
	@Override
	public int getPriority() {
		return MatcherType.TIME_DAILY_WEEKDAY.priority;
	}
	
	@Override
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt) {
		if(isWeekDay(currUCxt.getTimeDate()))
			return true;
		
		return false;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		Date toTime = new Date(currUCxt.getTimeDate().getTime() - conf.getTolerance());
		
		assert(conf.getDuration() % INTERVAL_WEEK == 0);
		
		Date fromTime = new Date(toTime.getTime() - conf.getDuration());

		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
		
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(isWeekDay(durationUserBhv.getTimeDate()))
				res.add(durationUserBhv);
		}
		
		return res;
	}
}
