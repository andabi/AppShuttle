package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.context.RfdUserCxt;
import lab.davidahn.appshuttle.context.UserCxt;
import lab.davidahn.appshuttle.context.env.Time;
import android.app.AlarmManager;
import android.content.Context;

public class WeakTimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	
	public WeakTimeContextMatcher(Context cxt, double minLikelihood, int minNumCxt, long period, long tolerance) {
		super(cxt, minLikelihood, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
		matcherType = MatcherType.WEAK_TIME;
	}

//	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
//		//TODO if tolerance is longer than 24h
//		long time = uEnv.getTime().getTime();
//		long validEndTime = time - period;
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(validEndTime - 3*AlarmManager.INTERVAL_DAY, validEndTime);
//		return res;
//	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		RfdUserCxt prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
				mergedRfdUCxtBuilder.setStartTime(rfdUCxt.getTime());
				mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
				mergedRfdUCxtBuilder.setTimeZone(rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getTime().getTime() - prevRfdUCxt.getEndTime().getTime()
						< settings.getLong("matcher.weak_time.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)){
					mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setStartTime(rfdUCxt.getTime());
					mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
					mergedRfdUCxtBuilder.setTimeZone(rfdUCxt.getTimeZone());
				}
			}
			prevRfdUCxt = rfdUCxt;
		}
		
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		return res;
	}
	
	@Override
	protected double calcRelatedness(MatcherCountUnit rfdUCxt, UserCxt uCxt) {
		long startTime = rfdUCxt.getStartTime().getTime();
		long endTime = rfdUCxt.getEndTime().getTime();
		long time = uCxt.getTime().getTime();
		
		long startTimePeriodic = startTime % period;
		long endTimePeriodic = endTime % period;
		long timePeriodic = time % period;

		if(Time.isBetween(startTimePeriodic - tolerance, timePeriodic, endTimePeriodic + tolerance)){
			return 1;
//			return (endTime - startTime) / Long.MAX_VALUE;
		} else {
			return 0;
		}
	}
}