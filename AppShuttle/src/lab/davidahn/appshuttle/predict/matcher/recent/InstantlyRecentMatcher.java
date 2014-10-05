package lab.davidahn.appshuttle.predict.matcher.recent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class InstantlyRecentMatcher extends RecentMatcher {
	
	public InstantlyRecentMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.INSTANTALY_RECENT;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		if(relatedHistoryMap.isEmpty())
			return 0;

		double likelihood = 0;
		
		List<Long> durationUserBhvsEndTimeList = new ArrayList<Long>();
		for(MatcherCountUnit unit : relatedHistoryMap.keySet()){
			for(DurationUserBhv uBhv : unit.getDurationUserBhvList()){
				durationUserBhvsEndTimeList.add(uBhv.getEndTime());
			}
		}
		
		assert(!durationUserBhvsEndTimeList.isEmpty());
		
		likelihood = 1.0 * Collections.max(durationUserBhvsEndTimeList) / Long.MAX_VALUE;
		return likelihood;
	}
}