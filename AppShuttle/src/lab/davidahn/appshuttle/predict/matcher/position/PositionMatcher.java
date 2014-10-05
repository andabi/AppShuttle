package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public abstract class PositionMatcher extends Matcher {

	public PositionMatcher(MatcherConf conf) {
		super(conf);
	}

	@Override
	protected double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt) {

		if (numTotalHistory <= 0)
			return 0;

		SummaryStatistics relatednessStat = new SummaryStatistics();
		for (double relatedness : relatedHistoryMap.values())
			relatednessStat.addValue(relatedness);
		
		double likelihood = 0;
		likelihood = 10 * relatedHistoryMap.size() + relatednessStat.getMean();
		
		double normalizedLikelihood = likelihood / Integer.MAX_VALUE;
		assert(0 <= normalizedLikelihood && normalizedLikelihood <= 1);

		return normalizedLikelihood;

	}

	// TODO bhvEffectivenessInEnv
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		double inverseEntropy = matcherResult.getInverseEntropy();

		double score = 1;
		score += 0.5 * inverseEntropy + 0.3 * (likelihood / Integer.MAX_VALUE);

		assert (1 <= score && score <= 2);

		return score;
	}
}