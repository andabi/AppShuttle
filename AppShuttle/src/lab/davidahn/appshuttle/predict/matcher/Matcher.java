package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public abstract class Matcher<C extends BaseMatcherConf> extends MatcherElem {
	protected C conf;
	
	public Matcher(C _conf) {
		conf = _conf;
	}
	
	public MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt) {
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
		if (numRelatedHistory < conf.getMinNumRelatedHistory())
			return null;

		double likelihood = computeLikelihood(numTotalHistory,
				relatedHistory, currUCxt);
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

	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt currUCxt){
		return true;
	}

	protected boolean isBhvMetPreConditions(UserBhv uBhv){
		return true;
	}
	
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv,
			SnapshotUserCxt currUCxt) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		long toTime = currUCxt.getTime();
		long fromTime = toTime - conf.getDuration();

		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(
				fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for (DurationUserBhv durationUserBhv : durationUserBhvList) {
			// if(durationUserBhv.getEndTime().getTime() -
			// durationUserBhv.getTime().getTime() < noiseTimeTolerance)
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

//protected List<MatcherCountUnit> mergeMatcherCountUnit(List<MatcherCountUnit> matcherCountUnitList) {
//	List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//
//	MatcherCountUnit lastUnit = null;
//	for(MatcherCountUnit unit : matcherCountUnitList){
//		if(lastUnit == null){
//			continue;
//		}
//		
//		long time = unit.getDurationUserBhvList().get(0).getTime();
//		long lastTime = lastUnit.getDurationUserBhvList().get(0).getTime;
//		if(time - lastTime	>= conf.getAcceptanceDelay()){
//			res.add(lastUnit);
//		}
//		
//		lastUnit = unit;
//	}
//	res.add(lastUnit);
//	
//	return res;
//}
