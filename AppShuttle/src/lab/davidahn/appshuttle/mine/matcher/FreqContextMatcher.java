package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.RfdUserCxt;
import lab.davidahn.appshuttle.context.UserCxt;
import android.app.AlarmManager;
import android.content.Context;

public class FreqContextMatcher extends ContextMatcher{
	public FreqContextMatcher(Context cxt, double minLikelihood, int minNumCxt) {
		super(cxt, minLikelihood, minNumCxt);
		matcherType = MatcherType.FREQUENCY;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		RfdUserCxt prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
			} else {
				if(rfdUCxt.getTime().getTime() - prevRfdUCxt.getEndTime().getTime()
						< settings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
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
		return 1;
	}

	@Override
	protected double calcLikelihood(int numTotalCxt, int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, UserCxt uCxt){
		double likelihood = 0;
//		int numRelatedCxt = matchedCxt.getNumRelatedCxt();
		likelihood = 1.0 * numRelatedCxt / Integer.MAX_VALUE;
		return likelihood;
	}

//	private int getNumTotalCxt(UserCxt uCxt){
//		int numTotalCxt = 0;
//		long etime = uCxt.getTime().getTime();
//		long sTime = etime - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);
//
//		for(UserBhv uBhv : userBhvManager.retrieveBhv()){
//			List<RfdUserCxt> rfdUCxtList = contextManager.retrieveRfdCxtByBhv(sTime, etime, uBhv);
//			numTotalCxt+=rfdUCxtList.size();
//		}
//		return numTotalCxt;
//	}
}
	
//	@Override
//	protected List<MergedRfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
//		List<MergedRfdUserCxt> res = new ArrayList<MergedRfdUserCxt>();
//		Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();
//
//		for(RfdUserCxt rfdUCxt : rfdUCxtList){
//			UserBhv uBhv = rfdUCxt.getBhv();
//			if(ongoingBhvMap.isEmpty()) {
//				ongoingBhvMap.put(uBhv, rfdUCxt);
//			} else {
//				if(ongoingBhvMap.containsKey(uBhv)){
//					RfdUserCxt prevRfdUCxt = ongoingBhvMap.get(uBhv);
//					if(rfdUCxt.getStartTime().getTime() - prevRfdUCxt.getEndTime().getTime()
//							< settings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
//						MergedRfdUserCxt mergedRfdUCxt = prevRfdUCxt;
//						mergedRfdUCxt.setEndTime(rfdUCxt.getEndTime());
//						mergedRfdUCxt.setLocs(rfdUCxt.getLocs());
//						mergedRfdUCxt.setPlaces(rfdUCxt.getPlaces());
//						ongoingBhvMap.put(uBhv, mergedRfdUCxt);
//					} else {
//						res.add(ongoingBhvMap.remove(uBhv));
//						ongoingBhvMap.put(uBhv, rfdUCxt);
//					}
//				} else {
//					ongoingBhvMap.put(uBhv, rfdUCxt);
//				}
//			}
//		}
//		for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//			RfdUserCxt restRfdUCxt = ongoingBhvMap.get(ongoingBhv);
//			res.add(restRfdUCxt);
//		}
//		return res;
//	}
