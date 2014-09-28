package lab.davidahn.appshuttle.predict.matcher.position;

import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;

public abstract class PositionMatcher extends Matcher {

	public PositionMatcher(MatcherConf conf){
		super(conf);
	}
	
	//TODO
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		double inverseEntropy = matcherResult.getInverseEntropy();
		int numRelatedHistory = matcherResult.getNumRelatedHistory();
//		int bhvEffectivenessInEnv;
		
		double score = 1;
		score += 0.5 * inverseEntropy + 0.3 * (numRelatedHistory / Integer.MAX_VALUE);
//		score += 0.5 * bhvEffectivenessInEnv + 0.3 * likelihood + 0.1 * inverseEntropy;
		
		assert(1 <= score && score <=2);
		
		return score;
	}
}
