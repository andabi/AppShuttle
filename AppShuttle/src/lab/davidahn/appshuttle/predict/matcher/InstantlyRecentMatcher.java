package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;

public class InstantlyRecentMatcher extends RecentMatcher {
	
	public InstantlyRecentMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt, acceptanceDelay);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.INSTANTALY_RECENT;
	}
	
	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		if(numRelatedCxt <= 0 || relatedCxtMap.isEmpty())
			return 0;

		double likelihood = 0;
		
		List<Long> durationUserBhvsEndTimeList = new ArrayList<Long>();
		for(MatcherCountUnit unit : relatedCxtMap.keySet()){
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