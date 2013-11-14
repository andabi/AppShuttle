package lab.davidahn.appshuttle.predict.matcher;

import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.conf.RecentMatcherConf;

public class FrequentlyRecentMatcher extends RecentMatcher {
	
//	public FrequentlyRecentMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumHistory, long acceptanceDelay) {
//		super(duration, minLikelihood, minInverseEntropy, minNumHistory, acceptanceDelay);
//	}
	
	public FrequentlyRecentMatcher(RecentMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.FREQUENTLY_RECENT;
	}

	@Override
	protected double computeLikelihood(int numRelatedHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		likelihood = 1.0 * numRelatedHistory / Integer.MAX_VALUE;
		return likelihood;
	}
}