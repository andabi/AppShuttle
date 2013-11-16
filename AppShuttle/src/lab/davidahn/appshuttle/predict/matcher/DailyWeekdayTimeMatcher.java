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


public class DailyWeekdayTimeMatcher extends TimeMatcher {

	public DailyWeekdayTimeMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY_WEEKDAY;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		
		if(!isWeekDay(currUCxt.getTimeDate()))
			return res;
		
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		Date toTime = new Date(currUCxt.getTimeDate().getTime() - conf.getTolerance());
		Date fromTime = new Date(toTime.getTime() - conf.getDuration());
		
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
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
