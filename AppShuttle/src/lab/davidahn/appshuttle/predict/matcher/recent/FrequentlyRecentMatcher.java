package lab.davidahn.appshuttle.predict.matcher.recent;

import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class FrequentlyRecentMatcher extends RecentMatcher {
	
	public FrequentlyRecentMatcher(RecentMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.FREQUENTLY_RECENT;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit durationUserBhv,
			SnapshotUserCxt uCxt) {
		long currTime = uCxt.getTimeDate().getTime();
		long time = durationUserBhv.getDurationUserBhvList().get(0).getTimeDate().getTime();
		double relatedness = 1 - (currTime - time) * 1.0 / conf.getDuration();

		assert(0 <= relatedness && relatedness <= 1);
		
		return relatedness;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		SummaryStatistics relatedHistoryStat = new SummaryStatistics();
		for(double relatedness : relatedHistoryMap.values())
			relatedHistoryStat.addValue(relatedness);
		
		double likelihood = 0;
		likelihood = 10 * relatedHistoryMap.size() + relatedHistoryStat.getMean();
		
		double normalizedLikelihood = likelihood / Integer.MAX_VALUE;
		assert(0 <= normalizedLikelihood && normalizedLikelihood <= 1);

		return normalizedLikelihood;
	}

}