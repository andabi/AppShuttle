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
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.predict.matcher.conf.PositionMatcherConf;

/**
 * K-NN based algorithm
 * @author andabi
 *
 */
public class LocationPositionMatcher extends PositionMatcher {
	
	public LocationPositionMatcher(PositionMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.LOCATION;
	}

	@Override
	protected List<MatcherCountUnit> mergeHistoryByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedDurationUserBhvBuilder = null;
		UserLoc lastKnownUserLoc = null;

		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.LOCATION)){
				UserLoc userLoc = (UserLoc)durationUserEnv.getUserEnv();
				long duration = durationUserEnv.getDuration();
				if(lastKnownUserLoc == null) {
					mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					mergedDurationUserBhvBuilder.setProperty("loc", userLoc);
					mergedDurationUserBhvBuilder.setProperty("duration", duration);
				} else {
					if(!userLoc.equals(lastKnownUserLoc)){
						res.add(mergedDurationUserBhvBuilder.build());
						mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
						mergedDurationUserBhvBuilder.setProperty("loc", userLoc);
						mergedDurationUserBhvBuilder.setProperty("duration", duration);
					}
				}
				lastKnownUserLoc = userLoc;
			}
		}
		if(mergedDurationUserBhvBuilder != null)
			res.add(mergedDurationUserBhvBuilder.build());
		
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		return 1;
	}
	
	@Override
	protected double computeLikelihood(int numRelatedHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		long totalSpentTime = 0, validSpentTime = 0;
		
		for(MatcherCountUnit unit : relatedHistoryMap.keySet()){
			UserLoc userLoc = ((UserLoc) unit.getProperty("loc"));
			long duration = ((Long) unit.getProperty("duration"));
			totalSpentTime += duration;
			try{
				if(userLoc.proximity((UserLoc)uCxt.getUserEnv(EnvType.LOCATION), ((PositionMatcherConf)conf).getToleranceInMeter())){
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
		assert(matcherCountUnitList.size() >= conf.getMinNumHistory());
		
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
						if(uLoc.proximity(uniqueLocElem, conf.getToleranceInMeter())){
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
