package lab.davidahn.appshuttle.predict.matcher.time;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import android.app.AlarmManager;

/* TODO: 리팩토링 제안
 * - Weekday와 Weekend 매쳐는 따로 있을 필요가 없음. (요일 조건별 매쳐로 통합 가능)
 */
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
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt) {
		if(isWeekDay(currUCxt.getTime()))
			return true;
		
		return false;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		long toTime = currUCxt.getTime() - conf.getTolerance();
		
		assert(conf.getDuration() % INTERVAL_WEEK == 0);
		
		long fromTime = toTime - conf.getDuration();

		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
		
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(isWeekDay(durationUserBhv.getTime()))
				res.add(durationUserBhv);
		}
		
		return res;
	}
}
