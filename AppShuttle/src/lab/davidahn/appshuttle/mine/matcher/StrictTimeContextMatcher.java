package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.commons.Time;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;

public class StrictTimeContextMatcher extends TemplateContextMatcher {
	protected long _period;
	protected long _tolerance;
	protected long _acceptanceDelay;
	
	public StrictTimeContextMatcher(Date time, long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		super(time, duration, minLikelihood, minInverseEntropy, minNumCxt);
		_period = period;
		_tolerance = tolerance;
		_acceptanceDelay = acceptanceDelay;
		_matcherType = MatcherType.STRICT_TIME;
	}

//	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
//		//TODO if tolerance is longer than 24h
//		long time = uEnv.getTime().getTime();
//		long validEndTime = time - period;
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(validEndTime - 3*AlarmManager.INTERVAL_DAY, validEndTime);
//		return res;
//	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
				mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTimeDate());
				mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTimeDate());
				mergedRfdUCxtBuilder.setProperty("timeZone", rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getTimeDate().getTime() - prevRfdUCxt.getEndTimeDate().getTime()
						< _acceptanceDelay){
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTimeDate());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
					mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTimeDate());
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTimeDate());
					mergedRfdUCxtBuilder.setProperty("timeZone", rfdUCxt.getTimeZone());
				}
			}
			prevRfdUCxt = rfdUCxt;
		}
		
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit rfdUCxt, SnapshotUserCxt uCxt) {
		double relatedness = 0;
		
		long currTime = uCxt.getTimeDate().getTime();
		long currTimePeriodic = currTime % _period;
		long targetTime = ((Date) rfdUCxt.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % _period;

		if(Time.isBetween((currTimePeriodic - _tolerance) % _period, targetTimePeriodic, (currTimePeriodic + _tolerance) % _period)){
			relatedness = 1;
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
		double inverseEntropy = 0;
		Set<Long> uniqueTime = new HashSet<Long>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			long time = ((Date) unit.getProperty("time")).getTime();
			long timePeriodic = time % _period;
			Iterator<Long> it = uniqueTime.iterator();
			boolean unique = true;
			if(!uniqueTime.isEmpty()){
				while(it.hasNext()){
					Long uniqueTimeElem = it.next();
					if(Time.isBetween((uniqueTimeElem - _tolerance) % _period, timePeriodic, (uniqueTimeElem + _tolerance) % _period)){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniqueTime.add(timePeriodic);
		}
		int entropy = uniqueTime.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
}
