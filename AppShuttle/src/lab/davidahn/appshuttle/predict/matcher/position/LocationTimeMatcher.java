package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.ArrayList;
import java.util.List;

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
 */
public class LocationTimeMatcher extends LocationMatcher {
	
	public LocationTimeMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.LOCATION_TIME;
	}

	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvs, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;

		for(DurationUserBhv durationUserBhv : durationUserBhvs){
			UserLoc lastKnownUserLoc = null;
			long accumulativeDuration = 0;
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTime()
					, durationUserBhv.getEndTime(), EnvType.LOCATION)){
				UserLoc userLoc = (UserLoc)durationUserEnv.getUserEnv();
				long time = durationUserEnv.getTime();
				long duration = durationUserEnv.getDuration();
				if(lastKnownUserLoc == null) {
					matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					matcherCountUnitBuilder.setProperty("time", time);
					matcherCountUnitBuilder.setProperty("location", userLoc);
					matcherCountUnitBuilder.setProperty("duration", duration);
				} else {
					if(!userLoc.equals(lastKnownUserLoc)){
						res.add(matcherCountUnitBuilder.build());
						matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
						matcherCountUnitBuilder.setProperty("time", time);
						matcherCountUnitBuilder.setProperty("location", userLoc);
						matcherCountUnitBuilder.setProperty("duration", accumulativeDuration );
					}
				}
				accumulativeDuration += duration;
				lastKnownUserLoc = userLoc;
			}
			
			if(matcherCountUnitBuilder != null)
				res.add(matcherCountUnitBuilder.build());
		}
		
		return res;
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
		
//		double relatedness = 0;
//		
//		long currTime = uCxt.getTime();
//		long currTimePeriodic = currTime % conf.getPeriod();
//		long targetTime = (Long) unit.getProperty("time");
//		long targetTimePeriodic = targetTime % conf.getPeriod();
//		
//		long mean = currTimePeriodic;
//		long std = conf.getTolerance() / 2;
//		NormalDistribution nd = new NormalDistribution(mean, std);
//
//		long from = (currTimePeriodic - conf.getTolerance() + conf.getPeriod()) % conf.getPeriod();
//		long to = (currTimePeriodic + conf.getTolerance()) % conf.getPeriod();
//				
//		if(Time.isIncludedIn(from, targetTimePeriodic, to, conf.getPeriod())){
//			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
//		} else {
//			relatedness = 0;
//		}
//		return relatedness;
	}
}
