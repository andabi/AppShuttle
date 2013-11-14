package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserPlace;
import lab.davidahn.appshuttle.predict.matcher.conf.PositionMatcherConf;

public class PlaceMatcher extends PositionMatcher {

//	public PlaceMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumHistory, int toleranceInMeter) {
//		super(duration, minLikelihood, minInverseEntropy, minNumHistory, toleranceInMeter);
//	}

	public PlaceMatcher(PositionMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.PLACE;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeHistoryByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedDurationUserBhvBuilder = null;
		UserPlace lastKnownUserPlace = null;
		
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.PLACE)){
				UserPlace userPlace = (UserPlace)durationUserEnv.getUserEnv();
				if(lastKnownUserPlace == null) {
					mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					mergedDurationUserBhvBuilder.setProperty("place", userPlace);
				} else {
						if(!userPlace.equals(lastKnownUserPlace)){
							res.add(mergedDurationUserBhvBuilder.build());
							mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
							mergedDurationUserBhvBuilder.setProperty("place", userPlace);
						}
				}
				lastKnownUserPlace = userPlace;
			}
		}
		if(mergedDurationUserBhvBuilder != null)
			res.add(mergedDurationUserBhvBuilder.build());
		
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		UserPlace uPlace = (UserPlace) uCxt.getUserEnv(EnvType.PLACE);
		if(uPlace.equals((UserPlace) unit.getProperty("place")))
			return 1;
		else
			return 0;
	}
	
	@Override
	protected double computeLikelihood(int numRelatedHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		for(double relatedness : relatedHistoryMap.values()){
			likelihood+=relatedness;
		}
		if(numRelatedHistory > 0)
			likelihood /= numRelatedHistory;
		else
			likelihood = 0;
		return likelihood;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= conf.getMinNumHistory());
		
		double inverseEntropy = 0;
		Set<UserPlace> uniquePlace = new HashSet<UserPlace>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserPlace uPlace = ((UserPlace) unit.getProperty("place"));
			Iterator<UserPlace> it = uniquePlace.iterator();
			boolean unique = true;
			if(!uniquePlace.isEmpty()){
				while(it.hasNext()){
					UserPlace uniquePlaceElem = it.next();
					if(uPlace.equals(uniquePlaceElem)){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniquePlace.add(uPlace);
		}
		int entropy = uniquePlace.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
}
