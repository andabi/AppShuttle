package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;

public class InstantlyRecentMatcher extends BaseMatcher{
	long _acceptanceDelay;
	
	public InstantlyRecentMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt);
		_acceptanceDelay = acceptanceDelay;
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.INSTANTALY_RECENT;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
			} else {
				if(durationUserBhv.getTimeDate().getTime() - prevRfdUCxt.getEndTimeDate().getTime()
						< _acceptanceDelay){
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
				}
			}
			mergedRfdUCxtBuilder.addRelatedDurationUserBhv(durationUserBhv);
			prevRfdUCxt = durationUserBhv;
		}
		
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit rfdUCxt, SnapshotUserCxt uCxt) {
		return 1;
	}

	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;

		if(numRelatedCxt <= 0 || relatedCxtMap.isEmpty())
			return likelihood;
		
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
	
	@Override
	protected double computeScore(MatcherResult matchedResult) {
		double likelihood = matchedResult.getLikelihood();
		
		double score = 1 + likelihood;
		
		assert(1 <= score && score <=2);
		return score;
	}
}