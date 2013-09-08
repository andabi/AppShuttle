package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.commons.Time;
import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;

import org.apache.commons.math3.distribution.NormalDistribution;

import android.content.Context;

public class WeakTimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	protected long acceptanceDelay;

	public WeakTimeContextMatcher(Context cxt, long duration, double minLikelihood, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		//TODO if tolerance is longer than 24h
		super(cxt, duration, minLikelihood, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
		this.acceptanceDelay = acceptanceDelay;
		matcherType = MatcherType.WEAK_TIME;
	}

	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
				mergedRfdUCxtBuilder.setProperty("startTime", rfdUCxt.getTime());
				mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				mergedRfdUCxtBuilder.setProperty("timeZone", rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getTime().getTime() - prevRfdUCxt.getEndTime().getTime()
						< acceptanceDelay){
					mergedRfdUCxtBuilder.setProperty("endTime", rfdUCxt.getEndTime());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setProperty("startTime", rfdUCxt.getTime());
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
		long currTime = uCxt.getTime().getTime();
		long currTimePeriodic = currTime % period;
		long targetTime = ((Date) rfdUCxt.getProperty("startTime")).getTime();
		long targetTimePeriodic = targetTime % period;
		
		long mean = currTimePeriodic;
		long std = tolerance / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		if(Time.isBetween((currTimePeriodic - tolerance) % period, targetTimePeriodic, (currTimePeriodic + tolerance) % period)){
			return nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			return 0;
		}
	}
}