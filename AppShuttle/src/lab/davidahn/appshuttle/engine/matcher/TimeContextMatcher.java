package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.bean.cxt.MatchedCxt;
import lab.davidahn.appshuttle.bean.cxt.MergedRfdUserCxt;
import lab.davidahn.appshuttle.bean.cxt.RfdUserCxt;
import lab.davidahn.appshuttle.bean.env.UserEnv;
import lab.davidahn.appshuttle.utils.Time;
import android.app.AlarmManager;
import android.content.Context;

public class TimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	
	public TimeContextMatcher(Context cxt, double minLikelihood, int minNumCxt, long period, long tolerance) {
		super(cxt, minLikelihood, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
		condition = "time";
	}

//	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
//		//TODO if tolerance is longer than 24h
//		long time = uEnv.getTime().getTime();
//		long validEndTime = time - period;
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(validEndTime - 3*AlarmManager.INTERVAL_DAY, validEndTime);
//		return res;
//	}
	
	@Override
	protected List<MergedRfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
		List<MergedRfdUserCxt> res = new ArrayList<MergedRfdUserCxt>();

		RfdUserCxt prevRfdUCxt = null;
		MergedRfdUserCxt.Builder mergedRfdUCxtBuilder = null;
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MergedRfdUserCxt.Builder(rfdUCxt.getBhv());
				mergedRfdUCxtBuilder.setStartTime(rfdUCxt.getStartTime());
				mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
				mergedRfdUCxtBuilder.setTimeZone(rfdUCxt.getTimeZone());
			} else {
				if(rfdUCxt.getStartTime().getTime() - prevRfdUCxt.getEndTime().getTime()
						< settings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
					mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MergedRfdUserCxt.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setStartTime(rfdUCxt.getStartTime());
					mergedRfdUCxtBuilder.setEndTime(rfdUCxt.getEndTime());
					mergedRfdUCxtBuilder.setTimeZone(rfdUCxt.getTimeZone());
				}
			}
			prevRfdUCxt = rfdUCxt;
		}
		
		res.add(mergedRfdUCxtBuilder.build());
		return res;
	}
	
	@Override
	protected double calcRelatedness(MergedRfdUserCxt rfdUCxt, UserEnv uEnv) {
		long startTime = rfdUCxt.getStartTime().getTime();
		long endTime = rfdUCxt.getEndTime().getTime();
		long time = uEnv.getTime().getTime();
		
		long startTimePeriodic = startTime % period;
		long endTimePeriodic = endTime % period;
		long timePeriodic = time % period;

		if(Time.isBetween(startTimePeriodic - tolerance, timePeriodic, endTimePeriodic + tolerance)){
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	protected double calcLikelihood(MatchedCxt matchedCxt){
		double likelihood = 0;
		int numTotalCxt = matchedCxt.getNumTotalCxt();
		
		Map<MergedRfdUserCxt, Double> relatedCxtMap = matchedCxt.getRelatedCxt();
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		likelihood /= numTotalCxt;
		likelihood *= 100;
		return likelihood;
	}
}