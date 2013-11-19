package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
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
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY_WEEKDAY;
	}
	
	@Override
	protected boolean preConditionForCurrUserCxt(SnapshotUserCxt currUCxt) {
		if(isWeekDay(currUCxt.getTimeDate()))
			return true;
		
		return false;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		Date toTime = new Date(currUCxt.getTimeDate().getTime() - conf.getTolerance());
		
		assert(conf.getDuration() % INTERVAL_WEEK == 0);
		
		int numWeekend = 2 * (int)(conf.getDuration() / INTERVAL_WEEK);
		Date fromTime = new Date(toTime.getTime() - conf.getDuration() - numWeekend * AlarmManager.INTERVAL_DAY);

		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
		
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(isWeekDay(durationUserBhv.getTimeDate()))
				res.add(durationUserBhv);
		}
		
		return res;
	}
	
	protected boolean isWeekDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return false;
		
		return true;
	}
}
