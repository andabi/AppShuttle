package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.collect.env.UserLoc;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

/**
 * K-NN based algorithm
 * @author andabi
 *
 */
public class LocationMatcher extends PositionMatcher {
	
	public LocationMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.LOCATION;
	}

	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;

		UserLoc lastKnownUserLoc = null;
		long accumulativeDuration = 0;
		
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTime()
					, durationUserBhv.getEndTime(), EnvType.LOCATION)){
				UserLoc userLoc = (UserLoc)durationUserEnv.getUserEnv();
				long duration = durationUserEnv.getDuration();
				if(lastKnownUserLoc == null) {
					matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					matcherCountUnitBuilder.setProperty("location", userLoc);
					matcherCountUnitBuilder.setProperty("duration", duration);
				} else {
					if(!userLoc.equals(lastKnownUserLoc)){
						res.add(matcherCountUnitBuilder.build());
						matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
						matcherCountUnitBuilder.setProperty("location", userLoc);
						matcherCountUnitBuilder.setProperty("duration", accumulativeDuration );
					}
				}
				accumulativeDuration += duration;
				lastKnownUserLoc = userLoc;
			}
		}
		
		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());

		return res;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {

		double inverseEntropy = 0;
		Set<UserLoc> uniqueLoc = new HashSet<UserLoc>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserLoc uLoc = ((UserLoc) unit.getProperty("location"));
			Iterator<UserLoc> it = uniqueLoc.iterator();
			boolean unique = true;
			if(!uniqueLoc.isEmpty()){
				while(it.hasNext()){
					UserLoc uniqueLocElem = it.next();
					try {
						if(uLoc.proximity(uniqueLocElem, conf.getPositionToleranceInMeter())){
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

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		UserLoc userLoc = ((UserLoc) unit.getProperty("location"));
		try{
			UserLoc currLoc = (UserLoc)uCxt.getUserEnv(EnvType.LOCATION);
			int toleranceInMeter = conf.getPositionToleranceInMeter();
			if(userLoc.proximity(currLoc, toleranceInMeter))
				return userLoc.distanceTo(currLoc) / toleranceInMeter;
			else
				return 0;
		} catch (InvalidUserEnvException e) {
			return 0;
		}
	}
}
