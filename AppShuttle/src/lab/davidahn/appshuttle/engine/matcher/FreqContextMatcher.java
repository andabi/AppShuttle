package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.model.MatchedCxt;
import lab.davidahn.appshuttle.model.RfdUserCxt;
import lab.davidahn.appshuttle.model.UserBhv;
import lab.davidahn.appshuttle.model.UserEnv;
import android.app.AlarmManager;
import android.content.Context;

public class FreqContextMatcher extends ContextMatcher{
	
	public FreqContextMatcher(Context cxt, double minLikelihood, int minNumCxt) {
		super(cxt, minLikelihood, minNumCxt);
	}
	
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
							< settings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
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
		return 1;
	}

	@Override
	protected double calcLikelihood(MatchedCxt matchedCxt){
		int numRelatedCxt = matchedCxt.getNumRelatedCxt();
		return numRelatedCxt * 10 / numTotalCxt;
	}
	
	@Override
	protected String conditionName(){
		return "frequency";
	}
}
