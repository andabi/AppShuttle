package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserSpeed;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class MoveMatcher extends PositionMatcher {

	public MoveMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.MOVE;
	}
	
	@Override
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt uCxt) {
		UserSpeed.Level currUserSpeedLevel = ((UserSpeed)uCxt.getUserEnv(EnvType.SPEED)).getLevel();
		if(currUserSpeedLevel == UserSpeed.Level.VEHICLE)
			return true;
		
		return false;
	}

	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit matcherCountUnit = null;

		DurationUserBhv prevDurationUserBhv = null;
		UserSpeed.Level lastKnownUserSpeedLevel = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTime()
					, durationUserBhv.getEndTime(), EnvType.SPEED)){
				UserSpeed.Level userSpeedLevel = ((UserSpeed)durationUserEnv.getUserEnv()).getLevel();
				if(prevDurationUserBhv == null) {
					matcherCountUnit = makeMatcherCountUnit(durationUserBhv, userSpeedLevel);
				} else {
					if(!userSpeedLevel.equals(lastKnownUserSpeedLevel)){
						res.add(matcherCountUnit);
						matcherCountUnit = makeMatcherCountUnit(durationUserBhv, userSpeedLevel);
					} else {
						long time = durationUserBhv.getTime();
						long lastEndTime = prevDurationUserBhv.getEndTime();
						if(time - lastEndTime >= conf.getAcceptanceDelay()){
							res.add(matcherCountUnit);
							matcherCountUnit = makeMatcherCountUnit(durationUserBhv, userSpeedLevel);
						}
					}
				}
				
				prevDurationUserBhv = durationUserBhv;
				lastKnownUserSpeedLevel = userSpeedLevel;
			}
		}

		if(matcherCountUnit != null)
			res.add(matcherCountUnit);
		
		return res;
	}

	private MatcherCountUnit makeMatcherCountUnit(DurationUserBhv durationUserBhv, UserSpeed.Level userSpeedLevel) {
		MatcherCountUnit matcherCountUnit = new MatcherCountUnit(durationUserBhv.getUserBhv());
		matcherCountUnit.setProperty("speed_level", userSpeedLevel);
		return matcherCountUnit;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		int numUserSpeedLevel = UserSpeed.Level.values().length;

		if(numUserSpeedLevel == 0)
			return 0;

		return 1.0 / numUserSpeedLevel;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		if((UserSpeed.Level) unit.getProperty("speed_level") == UserSpeed.Level.VEHICLE)
			return 1;
		
		return 0;
	}

	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double score = 1 + 0.5 * matcherResult.getLikelihood()
				+ 0.1 * (1.0 * matcherResult.getNumRelatedHistory() / Integer.MAX_VALUE);
		
		assert(1 <= score && score <=2);		
		
		return score;
	}
}
