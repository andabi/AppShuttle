package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.commons.Time;
import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import android.content.Context;

public class StrictTimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	protected long acceptanceDelay;
	
	public StrictTimeContextMatcher(Context cxt, long duration, double minLikelihood, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		super(cxt, duration, minLikelihood, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
		this.acceptanceDelay = acceptanceDelay;
		matcherType = MatcherType.STRICT_TIME;
	}

//	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
//		//TODO if tolerance is longer than 24h
//		long time = uEnv.getTime().getTime();
//		long validEndTime = time - period;
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(validEndTime - 3*AlarmManager.INTERVAL_DAY, validEndTime);
//		return res;
//	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
				mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTime());
				mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				mergedRfdUCxtBuilder.setProperty("timeZone", rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getTime().getTime() - prevRfdUCxt.getEndTime().getTime()
						< acceptanceDelay){
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTime());
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
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
	protected double calcRelatedness(MatcherCountUnit rfdUCxt, SnapshotUserCxt uCxt) {
		double relatedness = 0;
		
		long currTime = uCxt.getTime().getTime();
		long currTimePeriodic = currTime % period;
		long targetTime = ((Date) rfdUCxt.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % period;

		if(Time.isBetween((currTimePeriodic - tolerance) % period, targetTimePeriodic, (currTimePeriodic + tolerance) % period)){
			relatedness = 1;
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double calcInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= minNumCxt);
		
		double inverseEntropy = 0;
		Set<Long> uniqueTime = new HashSet<Long>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			long time = ((Date) unit.getProperty("time")).getTime();
			long timePeriodic = time % period;
			Iterator<Long> it = uniqueTime.iterator();
			boolean unique = true;
			if(!uniqueTime.isEmpty()){
				while(it.hasNext()){
					Long uniqueTimeElem = it.next();
					if(Time.isBetween((uniqueTimeElem - tolerance) % period, timePeriodic, (uniqueTimeElem + tolerance) % period)){
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