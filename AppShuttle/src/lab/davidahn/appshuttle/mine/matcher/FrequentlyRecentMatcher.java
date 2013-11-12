package lab.davidahn.appshuttle.mine.matcher;

import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;

public class FrequentlyRecentMatcher extends RecentMatcher {
	
	public FrequentlyRecentMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt, acceptanceDelay);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.FREQUENTLY_RECENT;
	}

	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		likelihood = 1.0 * numRelatedCxt / Integer.MAX_VALUE;
		return likelihood;
	}
}