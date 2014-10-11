package lab.davidahn.appshuttle.predict.matcher.recent;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;

public abstract class RecentMatcher extends Matcher {
	
	public RecentMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	protected boolean isBhvMetPreConditions(UserBhv uBhv){
		if(uBhv.getBhvType() == UserBhvType.SENSOR_ON && uBhv.getBhvName().equals(SensorType.WIFI.name()))
			return false;
		else
			return true;
	}
		
	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevDurationUserBhv = null;
		MatcherCountUnit matcherCountUnit = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevDurationUserBhv == null){
				matcherCountUnit = new MatcherCountUnit(durationUserBhv.getUserBhv());
			} else {
				long time = durationUserBhv.getTime();
				long prevEndTime = prevDurationUserBhv.getEndTime();
				if(time - prevEndTime >= conf.getAcceptanceDelay()){
					res.add(matcherCountUnit);
					matcherCountUnit = new MatcherCountUnit(durationUserBhv.getUserBhv());
				}
			}
			matcherCountUnit.addRelatedDurationUserBhv(durationUserBhv);
			prevDurationUserBhv = durationUserBhv;
		}
		
		if(matcherCountUnit != null)
			res.add(matcherCountUnit);
		
		return res;
	}

	@Override
	protected double computeInverseEntropy(
			List<MatcherCountUnit> matcherCountUnitList) {
		return 1;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit durationUserBhv,
			SnapshotUserCxt uCxt) {
		return 1;
	}

	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		
		double score = 1 + likelihood;
		
		assert(1 <= score && score <=2);
		return score;
	}
}
