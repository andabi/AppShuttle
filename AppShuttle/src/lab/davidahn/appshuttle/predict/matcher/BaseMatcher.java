package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.predict.matcher.conf.BaseMatcherConf;

public abstract class BaseMatcher<C extends BaseMatcherConf> extends BaseMatcherElem {
	protected C conf;
	
	public BaseMatcher(C _conf) {
		conf = _conf;
	}

	@Override
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		if(!isCurrCxtMetPreConditions(currUCxt))
			return null;

		if(!isBhvMetPreConditions(uBhv))
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
		matcherResult.setMatcherType(getType());
		matcherResult.setNumTotalHistory(numTotalHistory);
		matcherResult.setNumRelatedHistory(numRelatedHistory);
		matcherResult.setRelatedHistory(relatedHistory);
		matcherResult.setLikelihood(likelihood);
		matcherResult.setInverseEntropy(inverseEntropy);
		matcherResult.setScore(computeScore(matcherResult));

		return matcherResult;
	}
	
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt){
		return true;
	}

	protected boolean isBhvMetPreConditions(UserBhv uBhv){
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

}
