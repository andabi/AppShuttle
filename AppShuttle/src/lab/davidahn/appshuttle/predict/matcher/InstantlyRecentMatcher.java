package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.conf.RecentMatcherConf;

public class InstantlyRecentMatcher extends RecentMatcher {
	
	public InstantlyRecentMatcher(RecentMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.INSTANTALY_RECENT;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv,
			SnapshotUserCxt currUCxt) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		Date toTime = currUCxt.getTimeDate();
		Date fromTime = new Date(toTime.getTime() - conf.getDuration());

		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveOnEndTimeByBhv(
				fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for (DurationUserBhv durationUserBhv : durationUserBhvList) {
			// if(durationUserBhv.getEndTimeDate().getTime() -
			// durationUserBhv.getTimeDate().getTime() < noiseTimeTolerance)
			// continue;
			pureDurationUserBhvList.add(durationUserBhv);
		}
		return pureDurationUserBhvList;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		if(relatedHistoryMap.isEmpty())
			return 0;

		double likelihood = 0;
		
		List<Long> durationUserBhvsEndTimeList = new ArrayList<Long>();
		for(MatcherCountUnit unit : relatedHistoryMap.keySet()){
			for(DurationUserBhv uBhv : unit.getDurationUserBhvList()){
				durationUserBhvsEndTimeList.add(uBhv.getEndTimeDate().getTime());
			}
		}
		
		assert(!durationUserBhvsEndTimeList.isEmpty());
		
		likelihood = 1.0 * Collections.max(durationUserBhvsEndTimeList) / Long.MAX_VALUE;
		return likelihood;
	}
}