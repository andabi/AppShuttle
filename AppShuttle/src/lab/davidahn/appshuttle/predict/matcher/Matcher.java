package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public abstract class Matcher extends MatcherElem {
	protected MatcherConf conf;

	public Matcher(MatcherConf _conf) {
		conf = _conf;
	}

	public MatcherResultElem matchAndGetResult(UserBhv uBhv,
			SnapshotUserCxt currUCxt, List<DurationUserBhv> history) {
		if (!isCurrCxtMetPreConditions(currUCxt))
			return null;

		if (!isBhvMetPreConditions(uBhv))
			return null;

		history = rejectNotUsedHistory(history, currUCxt);

		List<MatcherCountUnit> matcherCountUnitList = makeMatcherCountUnit(
				history, currUCxt);

		if (matcherCountUnitList.isEmpty()) {
			return null;
		}

		double inverseEntropy = computeInverseEntropy(matcherCountUnitList);
		if (inverseEntropy < conf.getMinInverseEntropy()) {
			return null;
		}

		int numTotalHistory = 0, numRelatedHistory = 0;
		Map<MatcherCountUnit, Double> relatedHistory = new HashMap<MatcherCountUnit, Double>();

		for (MatcherCountUnit unit : matcherCountUnitList) {
			numTotalHistory++;
			double relatedness = computeRelatedness(unit, currUCxt);
			if (relatedness > 0) {
				numRelatedHistory++;
				relatedHistory.put(unit, relatedness);
			}
		}
		if (numRelatedHistory < conf.getMinNumRelatedHistory())
			return null;

		double likelihood = computeLikelihood(numTotalHistory, relatedHistory,
				currUCxt);
		if (likelihood < conf.getMinLikelihood())
			return null;

		MatcherResult matcherResult = new MatcherResult(currUCxt.getTime(),
				currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matcherResult.setUserBhv(uBhv);
		matcherResult.setMatcherType(getType());
		matcherResult.setNumTotalHistory(numTotalHistory);
		matcherResult.setNumRelatedHistory(numRelatedHistory);
		matcherResult.setRelatedHistory(relatedHistory);
		matcherResult.setLikelihood(likelihood);
		matcherResult.setInverseEntropy(inverseEntropy);
		matcherResult.setScore(computeScore(matcherResult));

		return matcherResult;
	}

	protected List<DurationUserBhv> rejectNotUsedHistory(List<DurationUserBhv> history, SnapshotUserCxt currUCxt) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : history){
			if(currUCxt.getTime() - durationUserBhv.getTime()  < conf.getDuration())
				res.add(durationUserBhv);
		}
		return res;
	}

	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt) {
		return true;
	}

	protected boolean isBhvMetPreConditions(UserBhv uBhv) {
		return true;
	}

	protected abstract List<MatcherCountUnit> makeMatcherCountUnit(
			List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt);

	protected abstract double computeInverseEntropy(
			List<MatcherCountUnit> matcherCountUnitList);

	protected abstract double computeRelatedness(
			MatcherCountUnit durationUserBhv, SnapshotUserCxt uCxt);

	protected abstract double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt);

	protected abstract double computeScore(MatcherResult matcherResult);

}