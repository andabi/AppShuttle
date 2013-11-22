package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.conf.BaseMatcherConf;

public abstract class BaseMatcher<C extends BaseMatcherConf> implements Matcher {
	protected C conf;
	
	public BaseMatcher(C _conf) {
		conf = _conf;
	}

	@Override
	public abstract MatcherType getMatcherType();

	@Override
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		if(!preConditionForCurrUserCxt(currUCxt))
			return null;
		
		List<MatcherCountUnit> matcherCountUnitList = makeMatcherCountUnit(
				getInvolvedDurationUserBhv(uBhv, currUCxt), currUCxt);

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
		if (numRelatedHistory < conf.getMinNumHistory())
			return null;

		double likelihood = computeLikelihood(numTotalHistory,
				relatedHistory, currUCxt);
		if (likelihood < conf.getMinLikelihood())
			return null;

		MatcherResult matcherResult = new MatcherResult(currUCxt.getTimeDate(),
				currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matcherResult.setUserBhv(uBhv);
		matcherResult.setMatcherType(getMatcherType());
		matcherResult.setNumTotalHistory(numTotalHistory);
		matcherResult.setNumRelatedHistory(numRelatedHistory);
		matcherResult.setRelatedHistory(relatedHistory);
		matcherResult.setLikelihood(likelihood);
		matcherResult.setInverseEntropy(inverseEntropy);
		matcherResult.setScore(computeScore(matcherResult));

		// Log.d("matchedCxt: matcher type",
		// matchedCxt.getMatcherType().toString());

		return matcherResult;
	}
	
	protected boolean preConditionForCurrUserCxt(SnapshotUserCxt currUCxt) {
		return true;
	}

	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv,
			SnapshotUserCxt currUCxt) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		Date toTime = currUCxt.getTimeDate();
		Date fromTime = new Date(toTime.getTime() - conf.getDuration());

		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(
				fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for (DurationUserBhv durationUserBhv : durationUserBhvList) {
			// if(durationUserBhv.getEndTimeDate().getTime() -
			// durationUserBhv.getTimeDate().getTime() < noiseTimeTolerance)
			// continue;
			pureDurationUserBhvList.add(durationUserBhv);
		}
		return pureDurationUserBhvList;
	}

	protected abstract List<MatcherCountUnit> makeMatcherCountUnit(
				List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt);

	protected abstract double computeInverseEntropy(
			List<MatcherCountUnit> matcherCountUnitList);
	
	protected abstract double computeRelatedness(MatcherCountUnit durationUserBhv,
	SnapshotUserCxt uCxt);

	protected abstract double computeLikelihood(int numTotalHistory,
				Map<MatcherCountUnit, Double> relatedHistoryMap,
				SnapshotUserCxt uCxt);

	protected abstract double computeScore(MatcherResult matcherResult);

	// if(numRelatedHistory >= minNumHistory && likelihood >= minLikelihood)
	// matchedCxt.setMatched(true);
	// else
	// matchedCxt.setMatched(false);

	// Map<UserBhv, Integer> numTotalHistoryByBhv = new HashMap<UserBhv,
	// Integer>();
	// Map<UserBhv, Integer> numRelatedHistoryByBhv = new HashMap<UserBhv,
	// Integer>();
	// Map<UserBhv, SparseArray<Double>> relatedHistoryByBhv = new
	// HashMap<UserBhv, SparseArray<Double>>();

	// for(RfdUserCxt durationUserBhv : durationUserBhvList) {
	// int contextId = durationUserBhv.getContextId();
	// UserBhv userBhv = durationUserBhv.getBhv();
	//
	// //numTotalHistoryByBhv
	// if(!numTotalHistoryByBhv.containsKey(userBhv))
	// numTotalHistoryByBhv.put(userBhv, 1);
	// numTotalHistoryByBhv.put(userBhv, numTotalHistoryByBhv.get(userBhv) + 1);
	//
	// double relatedness = computeRelatedness(durationUserBhv, uEnv);
	// if(relatedness > 0 ) {
	// //numRelatedHistoryByBhv
	// if(!numRelatedHistoryByBhv.containsKey(userBhv))
	// numRelatedHistoryByBhv.put(userBhv, 0);
	// numRelatedHistoryByBhv.put(userBhv, numRelatedHistoryByBhv.get(userBhv) +
	// 1);
	//
	// //relatedHistoryByBhv
	// if(!relatedHistoryByBhv.containsKey(userBhv))
	// relatedHistoryByBhv.put(userBhv, new SparseArray<Double>());
	// SparseArray<Double> relatedHistoryMap = relatedHistoryByBhv.get(userBhv);
	// relatedHistoryMap.put(contextId, relatedness);
	// relatedHistoryByBhv.put(userBhv, relatedHistoryMap);
	// }
	// }

	// for(UserBhv userBhv : relatedHistoryByBhv.keySet()){
	// int numRelatedHistory = numRelatedHistoryByBhv.get(userBhv);
	//
	// if(numRelatedHistory < minNumHistory) continue;
	//
	// MatchedCxt matchedCxt = new MatchedCxt(uEnv);
	// matchedCxt.setUserBhv(userBhv);
	// matchedCxt.setCondition(conditionName());
	// matchedCxt.setNumTotalCxt(numTotalHistoryByBhv.get(userBhv));
	// matchedCxt.setNumRelatedCxt(numRelatedHistoryByBhv.get(userBhv));
	// matchedCxt.setRelatedCxt(relatedHistoryByBhv.get(userBhv));
	// double likelihood = computeLikelihood(matchedCxt);
	//
	// if(likelihood < minLikelihood) continue;
	//
	// matchedCxt.setLikelihood(likelihood);
	//
	// res.add(matchedCxt);
	// }
	// return res;
}
