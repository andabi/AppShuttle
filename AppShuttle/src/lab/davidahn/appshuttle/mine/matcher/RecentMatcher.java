package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;

public abstract class RecentMatcher extends BaseMatcher{
	long _acceptanceDelay;
	
	public RecentMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt);
		_acceptanceDelay = acceptanceDelay;
	}
	
	@Override
	public abstract MatcherType getMatcherType();
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevRfdUCxt = null;
		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevRfdUCxt == null){
				mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
			} else {
				if(durationUserBhv.getTimeDate().getTime() - prevRfdUCxt.getEndTimeDate().getTime()
						< _acceptanceDelay){
				} else {
					res.add(mergedRfdUCxtBuilder.build());
					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
				}
			}
			mergedRfdUCxtBuilder.addRelatedDurationUserBhv(durationUserBhv);
			prevRfdUCxt = durationUserBhv;
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
	protected abstract double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt);
	
	@Override
	protected double computeScore(MatcherResult matchedResult) {
		double likelihood = matchedResult.getLikelihood();
		
		double score = 1 + likelihood;
		
		assert(1 <= score && score <=2);
		return score;
	}
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
//							< settings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
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
