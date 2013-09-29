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

import org.apache.commons.math3.distribution.NormalDistribution;

import android.content.Context;

public class WeakTimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	protected long acceptanceDelay;

	public WeakTimeContextMatcher(Context cxt, Date time, long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		//TODO if tolerance is longer than 24h
		super(cxt, time, duration, minLikelihood, minInverseEntropy, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
		this.acceptanceDelay = acceptanceDelay;
		_matcherType = MatcherType.WEAK_TIME;
	}

	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
				mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTimeDate());
				mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				mergedRfdUCxtBuilder.setProperty("timeZone", rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getTimeDate().getTime() - prevRfdUCxt.getEndTime().getTime()
						< acceptanceDelay){
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setProperty("time", rfdUCxt.getTimeDate());
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
		
		long currTime = uCxt.getTimeDate().getTime();
		long currTimePeriodic = currTime % period;
		long targetTime = ((Date) rfdUCxt.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % period;
		
		long mean = currTimePeriodic;
		long std = tolerance / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		if(Time.isBetween((currTimePeriodic - tolerance) % period, targetTimePeriodic, (currTimePeriodic + tolerance) % period)){
			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double calcInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
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
