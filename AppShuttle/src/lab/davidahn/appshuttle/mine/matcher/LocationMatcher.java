package lab.davidahn.appshuttle.mine.matcher;

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
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserLoc;

/**
 * K-NN based algorithm
 * @author andabi
 *
 */
public class LocationMatcher extends PositionMatcher {
	
	public LocationMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, int toleranceInMeter) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt, toleranceInMeter);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.LOCATION;
	}

	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		UserLoc lastKnownUserLoc = null;

		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(rfdUCxt.getTimeDate()
					, rfdUCxt.getEndTimeDate(), EnvType.LOCATION)){
				UserLoc userLoc = (UserLoc)durationUserEnv.getUserEnv();
				long duration = durationUserEnv.getDuration();
				if(lastKnownUserLoc == null) {
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
					mergedRfdUCxtBuilder.setProperty("loc", userLoc);
					mergedRfdUCxtBuilder.setProperty("duration", duration);
				} else {
					if(!userLoc.equals(lastKnownUserLoc)){
						res.add(mergedRfdUCxtBuilder.build());
						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
						mergedRfdUCxtBuilder.setProperty("loc", userLoc);
						mergedRfdUCxtBuilder.setProperty("duration", duration);
					}
				}
				lastKnownUserLoc = userLoc;
			}
		}
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		return 1;
	}
	
	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		long totalSpentTime = 0, validSpentTime = 0;
		
		for(MatcherCountUnit unit : relatedCxtMap.keySet()){
			UserLoc userLoc = ((UserLoc) unit.getProperty("loc"));
			long duration = ((Long) unit.getProperty("duration"));
			totalSpentTime += duration;
			try{
				if(userLoc.proximity((UserLoc)uCxt.getUserEnv(EnvType.LOCATION), _toleranceInMeter)){
					validSpentTime += duration;
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		
		double likelihood = 0;
		if(totalSpentTime > 0)
			likelihood = 1.0 * validSpentTime / totalSpentTime;
		return likelihood;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
		double inverseEntropy = 0;
		Set<UserLoc> uniqueLoc = new HashSet<UserLoc>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserLoc uLoc = ((UserLoc) unit.getProperty("loc"));
			Iterator<UserLoc> it = uniqueLoc.iterator();
			boolean unique = true;
			if(!uniqueLoc.isEmpty()){
				while(it.hasNext()){
					UserLoc uniqueLocElem = it.next();
					try {
						if(uLoc.proximity(uniqueLocElem, _toleranceInMeter)){
							unique = false;
							break;
						}
					} catch (InvalidUserEnvException e) {
						unique = false;
					}
				}
			}
			if(unique)
				uniqueLoc.add(uLoc);
		}
		int entropy = uniqueLoc.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
}
