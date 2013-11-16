package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
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
	protected double computeLikelihood(int numRelatedHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		if(numRelatedHistory <= 0 || relatedHistoryMap.isEmpty())
			return 0;

		double likelihood = 0;
		
		List<Long> durationUserBhvsEndTimeList = new ArrayList<Long>();
		for(MatcherCountUnit unit : relatedHistoryMap.keySet()){
			for(DurationUserBhv uBhv : unit.getDurationUserBhvList()){
				durationUserBhvsEndTimeList.add(uBhv.getEndTimeDate().getTime());
			}
		}
		
		assert(!durationUserBhvsEndTimeList.isEmpty());
		
		Collections.sort(durationUserBhvsEndTimeList);
		long recentEndTime = durationUserBhvsEndTimeList.get(durationUserBhvsEndTimeList.size()-1);

		likelihood = 1.0 * recentEndTime / Long.MAX_VALUE;
		return likelihood;
	}
}