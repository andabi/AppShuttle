package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public abstract class TemplateContextMatcher {
	protected MatcherType _matcherType;
	protected double _minLikelihood;
	protected double _minInverseEntropy;
	protected int _minNumCxt;
	protected Date _time;
	protected long _duration;
	
	protected MatcherType getMatcherType(){
		return _matcherType;
	}

	public TemplateContextMatcher(Date time, long duration, double minLikelihood, double minInverseEntropy, int minNumCxt) {
		_time = time;
		_duration = duration;
		_minLikelihood = minLikelihood;
		_minInverseEntropy = minInverseEntropy;
		_minNumCxt = minNumCxt;
	}

	public MatchedResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt, long noiseTimeTolerance){
		DurationUserBhvDao rfdUserCxtDao = DurationUserBhvDao.getInstance();

		Map<EnvType, UserEnv> uEnvs = currUCxt.getUserEnvs();
		
		Date toTime = _time;
		Date fromTime = new Date(toTime.getTime() - _duration);
		
		List<DurationUserBhv> rfdUCxtList = rfdUserCxtDao.retrieveByBhv(fromTime, toTime, uBhv);
		List<DurationUserBhv> pureRfdUCxtList = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(rfdUCxt.getEndTime().getTime() - rfdUCxt.getTimeDate().getTime()	< noiseTimeTolerance)
				continue;
			pureRfdUCxtList.add(rfdUCxt);
		}
		List<MatcherCountUnit> mergedRfdCxtList = mergeCxtByCountUnit(pureRfdUCxtList, currUCxt);
		
		if(mergedRfdCxtList.isEmpty()){
			return null;
		}
		
		double inverseEntropy = calcInverseEntropy(mergedRfdCxtList);
		if(inverseEntropy < _minInverseEntropy){
			return null;
		}
		
		int numTotalCxt = 0;
		int numRelatedCxt = 0;
		Map<MatcherCountUnit, Double> relatedCxt = new HashMap<MatcherCountUnit, Double>();
		
		for(MatcherCountUnit mergedRfdCxt : mergedRfdCxtList) {
			numTotalCxt++;
			double relatedness = calcRelatedness(mergedRfdCxt, currUCxt);
			if(relatedness > 0 ) {
				numRelatedCxt++;
				relatedCxt.put(mergedRfdCxt, relatedness);
			}
		}
		if(numRelatedCxt < _minNumCxt)
			return null;
		
		double likelihood = calcLikelihood(numRelatedCxt, relatedCxt, currUCxt);
		if(likelihood < _minLikelihood)
			return null;
		
		MatchedResult matchedCxt = new MatchedResult(currUCxt.getTimeDate(), currUCxt.getTimeZone(), uEnvs);
		matchedCxt.setUserBhv(uBhv);
		matchedCxt.setMatcherType(getMatcherType());
		matchedCxt.setNumTotalCxt(numTotalCxt);
		matchedCxt.setNumRelatedCxt(numRelatedCxt);
		matchedCxt.setRelatedCxt(relatedCxt);
		matchedCxt.setLikelihood(likelihood);
		matchedCxt.setInverseEntropy(inverseEntropy);
		return matchedCxt;
	}
	
	protected double calcLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		likelihood /= numRelatedCxt;
		return likelihood;
	}
	
	protected double calcInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		double inverseEntropy = Double.MIN_VALUE;
		return inverseEntropy;
	}

	protected abstract List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt);
	protected abstract double calcRelatedness(MatcherCountUnit rfdUCxt, SnapshotUserCxt uCxt);
	
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
//			double relatedness = calcRelatedness(rfdUCxt, uEnv);			
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
//			double likelihood = calcLikelihood(matchedCxt);
//
//			if(likelihood < minLikelihood) continue;
//
//			matchedCxt.setLikelihood(likelihood);
//			
//			res.add(matchedCxt);
//		}
//		return res;
}
