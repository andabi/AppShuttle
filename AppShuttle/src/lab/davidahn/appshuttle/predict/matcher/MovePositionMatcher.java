package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserSpeed;
import lab.davidahn.appshuttle.predict.matcher.conf.PositionMatcherConf;

public class MovePositionMatcher extends PositionMatcher {

	public MovePositionMatcher(PositionMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.MOVE;
	}
	
	@Override
	protected boolean preConditionForCurrUserCxt(SnapshotUserCxt uCxt) {
		UserSpeed.Level currUserSpeedLevel = ((UserSpeed)uCxt.getUserEnv(EnvType.SPEED)).getLevel();
		if(currUserSpeedLevel == UserSpeed.Level.VEHICLE)
			return true;
		
		return false;
	}

	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;
		
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			UserSpeed.Level lastKnownUserSpeedLevel = null;
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.SPEED)){
				UserSpeed.Level userSpeedLevel = ((UserSpeed)durationUserEnv.getUserEnv()).getLevel();
				if(lastKnownUserSpeedLevel == null) {
					matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					matcherCountUnitBuilder.setProperty("speed_level", userSpeedLevel);
				} else {
						if(!userSpeedLevel.equals(lastKnownUserSpeedLevel)){
							res.add(matcherCountUnitBuilder.build());
							matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
							matcherCountUnitBuilder.setProperty("speed_level", userSpeedLevel);
						}
				}
				lastKnownUserSpeedLevel = userSpeedLevel;
			}

			if(matcherCountUnitBuilder != null)
				res.add(matcherCountUnitBuilder.build());
		}
		
		return res;
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
	protected double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt) {	
		return (double)relatedHistoryMap.size() / Integer.MAX_VALUE;
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double score = (1 + matcherResult.getLikelihood());
		
		assert(1 <= score && score <=2);		
		
		return score;
	}
}
