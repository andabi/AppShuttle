package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;

public class FreqContextMatcher extends TemplateContextMatcher{
	long _acceptanceDelay;
	
	public FreqContextMatcher(Date time, long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long acceptanceDelay) {
		super(time, duration, minLikelihood, minInverseEntropy, minNumCxt);
		_matcherType = MatcherType.FREQUENCY;
		_acceptanceDelay = acceptanceDelay;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
			} else {
				if(rfdUCxt.getTimeDate().getTime() - prevRfdUCxt.getEndTimeDate().getTime()
						< _acceptanceDelay){
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
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
		return 1;
	}

	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		double likelihood = 0;
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
