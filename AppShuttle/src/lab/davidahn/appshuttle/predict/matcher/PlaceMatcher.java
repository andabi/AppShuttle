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

public class PlaceMatcher extends PositionMatcher {

	public PlaceMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, int toleranceInMeter) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt, toleranceInMeter);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.PLACE;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		UserPlace lastKnownUserPlace = null;
		
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(rfdUCxt.getTimeDate()
					, rfdUCxt.getEndTimeDate(), EnvType.PLACE)){
				UserPlace userPlace = (UserPlace)durationUserEnv.getUserEnv();
				if(lastKnownUserPlace == null) {
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
					mergedRfdUCxtBuilder.setProperty("place", userPlace);
				} else {
						if(!userPlace.equals(lastKnownUserPlace)){
							res.add(mergedRfdUCxtBuilder.build());
							mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
							mergedRfdUCxtBuilder.setProperty("place", userPlace);
						}
				}
				lastKnownUserPlace = userPlace;
			}
		}
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		
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
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		if(numRelatedCxt > 0)
			likelihood /= numRelatedCxt;
		else
			likelihood = 0;
		return likelihood;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
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
