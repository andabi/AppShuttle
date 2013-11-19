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
	protected List<MatcherCountUnit> mergeHistoryByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedDurationUserBhvBuilder = null;
		UserSpeed.Level lastKnownUserSpeedLevel = null;
		
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.SPEED)){
				UserSpeed.Level userSpeedLevel = ((UserSpeed)durationUserEnv.getUserEnv()).getLevel();
				if(lastKnownUserSpeedLevel == null) {
					mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					mergedDurationUserBhvBuilder.setProperty("speed_level", userSpeedLevel);
				} else {
						if(!userSpeedLevel.equals(lastKnownUserSpeedLevel)){
							res.add(mergedDurationUserBhvBuilder.build());
							mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
							mergedDurationUserBhvBuilder.setProperty("speed_level", userSpeedLevel);
						}
				}
				lastKnownUserSpeedLevel = userSpeedLevel;
			}
		}
		if(mergedDurationUserBhvBuilder != null)
			res.add(mergedDurationUserBhvBuilder.build());
		
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
	protected double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt) {
		if(numTotalHistory <= 0)
			return 0;
		
		double likelihood = 0;
		for (double relatedness : relatedHistoryMap.values()) {
			likelihood += relatedness;
		}
		likelihood /= numTotalHistory;
		return likelihood;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		if((UserSpeed.Level) unit.getProperty("speed_level") == UserSpeed.Level.VEHICLE)
			return 1;
		
		return 0;
	}
}
