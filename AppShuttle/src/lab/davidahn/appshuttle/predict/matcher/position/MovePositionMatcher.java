package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserSpeed;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit.Builder;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class MovePositionMatcher extends PositionMatcher {

	public MovePositionMatcher(PositionMatcherConf conf){
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

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;

		DurationUserBhv prevDurationUserBhv = null;
		UserSpeed.Level lastKnownUserSpeedLevel = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.SPEED)){
				UserSpeed.Level userSpeedLevel = ((UserSpeed)durationUserEnv.getUserEnv()).getLevel();
				if(prevDurationUserBhv == null) {
					matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userSpeedLevel);
				} else {
					if(!userSpeedLevel.equals(lastKnownUserSpeedLevel)){
						res.add(matcherCountUnitBuilder.build());
						matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userSpeedLevel);
					} else {
						long time = durationUserBhv.getTimeDate().getTime();
						long lastEndTime = prevDurationUserBhv.getEndTimeDate().getTime();
						if(time - lastEndTime >= conf.getAcceptanceDelay()){
							res.add(matcherCountUnitBuilder.build());
							matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userSpeedLevel);
						}
					}
				}
				
				prevDurationUserBhv = durationUserBhv;
				lastKnownUserSpeedLevel = userSpeedLevel;
			}
		}

		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		
		return res;
	}

	private Builder makeMatcherCountUnitBuilder(DurationUserBhv durationUserBhv, UserSpeed.Level userSpeedLevel) {
		return new MatcherCountUnit.Builder(durationUserBhv.getUserBhv())
		.setProperty("speed_level", userSpeedLevel);
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
//		return (double)relatedHistoryMap.size() / Integer.MAX_VALUE;
		if(numTotalHistory <= 0)
			return 0;
		
		return relatedHistoryMap.size() / numTotalHistory;
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double score = 1 + 0.5 * matcherResult.getLikelihood()
				+ 0.1 * (1.0 * matcherResult.getNumRelatedHistory() / Integer.MAX_VALUE);
		
		assert(1 <= score && score <=2);		
		
		return score;
	}
}
