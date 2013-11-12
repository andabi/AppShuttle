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

public abstract class BaseMatcher implements Matcher {
	protected double _minLikelihood;
	protected double _minInverseEntropy;
	protected int _minNumCxt;
	protected long _duration;
	
	public BaseMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt) {
		_duration = duration;
		_minLikelihood = minLikelihood;
		_minInverseEntropy = minInverseEntropy;
		_minNumCxt = minNumCxt;
	}

	@Override
	public abstract MatcherType getMatcherType();
	
	@Override
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt/*, long noiseTimeTolerance*/){
		List<MatcherCountUnit> matcherCountUnitList = mergeCxtByCountUnit(getInvolvedDurationUserBhv(uBhv, currUCxt), currUCxt);
		
		if(matcherCountUnitList.isEmpty()){
			return null;
		}
		
		double inverseEntropy = computeInverseEntropy(matcherCountUnitList);
		if(inverseEntropy < _minInverseEntropy){
			return null;
		}
		
		int numTotalCxt = 0, numRelatedCxt = 0;
		Map<MatcherCountUnit, Double> relatedCxt = new HashMap<MatcherCountUnit, Double>();
		
		for(MatcherCountUnit mergedRfdCxt : matcherCountUnitList) {
			numTotalCxt++;
			double relatedness = computeRelatedness(mergedRfdCxt, currUCxt);
			if(relatedness > 0 ) {
				numRelatedCxt++;
				relatedCxt.put(mergedRfdCxt, relatedness);
			}
		}
		if(numRelatedCxt < _minNumCxt)
			return null;
		
		double likelihood = computeLikelihood(numRelatedCxt, relatedCxt, currUCxt);
		if(likelihood < _minLikelihood)
			return null;
		
		MatcherResult matchedCxt = new MatcherResult(currUCxt.getTimeDate(), currUCxt.getTimeZone(), currUCxt.getUserEnvs());
		matchedCxt.setUserBhv(uBhv);
		matchedCxt.setMatcherType(getMatcherType());
		matchedCxt.setNumTotalCxt(numTotalCxt);
		matchedCxt.setNumRelatedCxt(numRelatedCxt);
		matchedCxt.setRelatedCxt(relatedCxt);
		matchedCxt.setLikelihood(likelihood);
		matchedCxt.setInverseEntropy(inverseEntropy);
		
		double score = computeScore(matchedCxt);
		
		matchedCxt.setScore(score);
		
//		Log.d("matchedCxt: matcher type", matchedCxt.getMatcherType().toString());
		
		return matchedCxt;
	}
	
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		DurationUserBhvDao rfdUserCxtDao = DurationUserBhvDao.getInstance();

		Date toTime = currUCxt.getTimeDate();
		Date fromTime = new Date(toTime.getTime() - _duration);
		
		List<DurationUserBhv> rfdUCxtList = rfdUserCxtDao.retrieveByBhv(fromTime, toTime, uBhv);
		List<DurationUserBhv> pureRfdUCxtList = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
//			if(rfdUCxt.getEndTimeDate().getTime() - rfdUCxt.getTimeDate().getTime()	< noiseTimeTolerance)
//				continue;
			pureRfdUCxtList.add(rfdUCxt);
		}
		return pureRfdUCxtList;
	}

	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		likelihood /= numRelatedCxt;
		return likelihood;
	}
	
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		double inverseEntropy = Double.MIN_VALUE;
		return inverseEntropy;
	}
	
	protected abstract List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt);
	
	protected abstract double computeRelatedness(MatcherCountUnit rfdUCxt, SnapshotUserCxt uCxt);
	
	protected abstract double computeScore(MatcherResult matchedResult);
//			if(numRelatedCxt >= minNumCxt && likelihood >= minLikelihood)
//				matchedCxt.setMatched(true);
//			else
//				matchedCxt.setMatched(false);
	
//		Map<UserBhv, Integer> numTotalCxtByBhv = new HashMap<UserBhv, Integer>();
//		Map<UserBhv, Integer> numRelatedCxtByBhv = new HashMap<UserBhv, Integer>();
//		Map<UserBhv, SparseArray<Double>> relatedCxtByBhv = new HashMap<UserBhv, SparseArray<Double>>();

//		for(RfdUserCxt rfdUCxt : rfdUCxtList) {
//			int contextId = rfdUCxt.getContextId();
//			UserBhv userBhv = rfdUCxt.getBhv();
//
//			//numTotalCxtByBhv
//			if(!numTotalCxtByBhv.containsKey(userBhv)) numTotalCxtByBhv.put(userBhv, 1);
//			numTotalCxtByBhv.put(userBhv, numTotalCxtByBhv.get(userBhv) + 1);
//			
//			double relatedness = computeRelatedness(rfdUCxt, uEnv);			
//			if(relatedness > 0 ) {
//				//numRelatedCxtByBhv
//				if(!numRelatedCxtByBhv.containsKey(userBhv)) numRelatedCxtByBhv.put(userBhv, 0);
//				numRelatedCxtByBhv.put(userBhv, numRelatedCxtByBhv.get(userBhv) + 1);
//				
//				//relatedCxtByBhv
//				if(!relatedCxtByBhv.containsKey(userBhv)) relatedCxtByBhv.put(userBhv, new SparseArray<Double>());
//				SparseArray<Double> relatedCxtMap = relatedCxtByBhv.get(userBhv);
//				relatedCxtMap.put(contextId, relatedness);
//				relatedCxtByBhv.put(userBhv, relatedCxtMap);
//			}
//		}

//		for(UserBhv userBhv : relatedCxtByBhv.keySet()){
//			int numRelatedCxt = numRelatedCxtByBhv.get(userBhv);
//			
//			if(numRelatedCxt < minNumCxt) continue;
//			
//			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
//			matchedCxt.setUserBhv(userBhv);
//			matchedCxt.setCondition(conditionName());
//			matchedCxt.setNumTotalCxt(numTotalCxtByBhv.get(userBhv));
//			matchedCxt.setNumRelatedCxt(numRelatedCxtByBhv.get(userBhv));
//			matchedCxt.setRelatedCxt(relatedCxtByBhv.get(userBhv));
//			double likelihood = computeLikelihood(matchedCxt);
//
//			if(likelihood < minLikelihood) continue;
//
//			matchedCxt.setLikelihood(likelihood);
//			
//			res.add(matchedCxt);
//		}
//		return res;
}
