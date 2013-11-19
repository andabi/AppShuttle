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
	protected double computeLikelihood(int numTotalHistory, Map<MatcherCountUnit, Double> relatedHistoryMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
		for(double relatedness : relatedHistoryMap.values()){
			likelihood+=relatedness;
		}
		if(relatedHistoryMap.size() > 0)
			likelihood /= relatedHistoryMap.size();
		else
			likelihood = 0;
		return likelihood;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		if((UserSpeed.Level) unit.getProperty("speed_level") == UserSpeed.Level.VEHICLE)
			return 1;
		
		return 0;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= conf.getMinNumHistory());
		
		double inverseEntropy = 0;
		Set<UserSpeed.Level> uniqueUserSpeedlevel = new HashSet<UserSpeed.Level>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserSpeed.Level uSpeedLevel = ((UserSpeed.Level) unit.getProperty("speed_level"));
			Iterator<UserSpeed.Level> it = uniqueUserSpeedlevel.iterator();
			boolean unique = true;
			if(!uniqueUserSpeedlevel.isEmpty()){
				while(it.hasNext()){
					UserSpeed.Level uniqueUserSpeedLevelElem = it.next();
					if(uSpeedLevel.equals(uniqueUserSpeedLevelElem)){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniqueUserSpeedlevel.add(uSpeedLevel);
		}
		int entropy = uniqueUserSpeedlevel.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
}
