package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.model.MatchedCxt;
import lab.davidahn.appshuttle.model.RfdUserCxt;
import lab.davidahn.appshuttle.model.UserBhv;
import lab.davidahn.appshuttle.model.UserEnv;
import lab.davidahn.appshuttle.utils.Time;
import android.app.AlarmManager;
import android.content.Context;
import android.util.SparseArray;

public class TimeContextMatcher extends ContextMatcher {
	protected long period;
	protected long tolerance;
	
	public TimeContextMatcher(Context cxt, double minLikelihood, int minNumCxt, long period, long tolerance) {
		super(cxt, minLikelihood, minNumCxt);
		this.period = period;
		this.tolerance = tolerance;
	}

//	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
//		//TODO if tolerance is longer than 24h
//		long time = uEnv.getTime().getTime();
//		long validEndTime = time - period;
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(validEndTime - 3*AlarmManager.INTERVAL_DAY, validEndTime);
//		return res;
//	}
	
	@Override
	protected List<RfdUserCxt> refineCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
		Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();

		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			UserBhv uBhv = rfdUCxt.getBhv();
			if(ongoingBhvMap.isEmpty()) {
				ongoingBhvMap.put(uBhv, rfdUCxt);
			} else {
				if(ongoingBhvMap.containsKey(uBhv)){
					RfdUserCxt prevRfdUCxt = ongoingBhvMap.get(uBhv);
					if(rfdUCxt.getStartTime().getTime() - prevRfdUCxt.getEndTime().getTime()
							< settings.getLong("matcher.time.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)){
						RfdUserCxt mergedRfdUCxt = prevRfdUCxt;
						mergedRfdUCxt.setEndTime(rfdUCxt.getEndTime());
						mergedRfdUCxt.addLocFreq(rfdUCxt.getLocFreqList());
						mergedRfdUCxt.addPlaceFreq(rfdUCxt.getPlaceFreqList());
						ongoingBhvMap.put(uBhv, mergedRfdUCxt);
					} else {
						res.add(ongoingBhvMap.remove(uBhv));
						ongoingBhvMap.put(uBhv, rfdUCxt);
					}
				} else {
					ongoingBhvMap.put(uBhv, rfdUCxt);
				}
			}
		}
		for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
			RfdUserCxt restRfdUCxt = ongoingBhvMap.get(ongoingBhv);
			res.add(restRfdUCxt);
		}
		return res;
	}
	
	@Override
	protected double calcRelatedness(RfdUserCxt rfdUCxt, UserEnv uEnv) {
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
		int numTotalCxt = matchedCxt.getNumTotalCxt();
		int numRelatedCxt = matchedCxt.getNumRelatedCxt();
		SparseArray<Double> relatedCxtMap = matchedCxt.getRelatedCxt();
		
		double likelihood = 0;
		for(int i=0;i<numRelatedCxt;i++){
			likelihood+=relatedCxtMap.valueAt(i);
		}
		likelihood /= numTotalCxt;
		likelihood *= 100;
		return likelihood;
	}
	
	@Override
	protected String conditionName(){
		return "time";
	}
}