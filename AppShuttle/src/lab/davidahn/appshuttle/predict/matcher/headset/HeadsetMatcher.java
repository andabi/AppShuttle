package lab.davidahn.appshuttle.predict.matcher.headset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.HeadsetEnv;
import lab.davidahn.appshuttle.collect.env.HeadsetEnvSensor;
import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit.Builder;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class HeadsetMatcher extends Matcher<HeadsetMatcherConf>{
	
	public HeadsetMatcher(HeadsetMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.HEADSET;
	}
	
	@Override
	protected boolean isCurrCxtMetPreConditions(SnapshotUserCxt uCxt) {
		if(HeadsetEnvSensor.getInstance().isHeadsetPlugged()) return true;
		else return false;
	}
	
	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;

		DurationUserBhv prevDurationUserBhv = null;
		HeadsetEnv lastKnownHeadsetEnv = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTime()
					, durationUserBhv.getEndTime(), EnvType.HEADSET)){
				HeadsetEnv headsetEnv = ((HeadsetEnv)durationUserEnv.getUserEnv());
				if(prevDurationUserBhv == null) {
					matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, headsetEnv);
				} else {
					if(!headsetEnv.equals(lastKnownHeadsetEnv)){
						res.add(matcherCountUnitBuilder.build());
						matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, headsetEnv);
					} else {
						long time = durationUserBhv.getTime();
						long lastEndTime = prevDurationUserBhv.getEndTime();
						if(time - lastEndTime >= conf.getAcceptanceDelay()){
							res.add(matcherCountUnitBuilder.build());
							matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, headsetEnv);
						}
					}
				}
				prevDurationUserBhv = durationUserBhv;
				lastKnownHeadsetEnv = headsetEnv;
			}
		}

		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		
		return res;
	}

	private Builder makeMatcherCountUnitBuilder(DurationUserBhv durationUserBhv, HeadsetEnv headsetEnv) {
		return new MatcherCountUnit.Builder(durationUserBhv.getUserBhv())
		.setProperty("headset_plugged", headsetEnv.isPlugged());
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		return 0.5;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		if((Boolean)unit.getProperty("headset_plugged")) return 1;
		else return 0;
	}

	@Override
	protected double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt) {	
		if(numTotalHistory <= 0) return 0;
		
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
