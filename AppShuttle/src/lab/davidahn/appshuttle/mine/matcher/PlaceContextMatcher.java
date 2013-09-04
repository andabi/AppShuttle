package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import lab.davidahn.appshuttle.context.env.UserPlace;
import android.content.Context;

public class PlaceContextMatcher extends ContextMatcher {
	int toleranceInMeter;

	public PlaceContextMatcher(Context cxt, double minLikelihood, int minNumCxt, int toleranceInMeter) {
		super(cxt, minLikelihood, minNumCxt);
		this.toleranceInMeter = toleranceInMeter;
		matcherType = MatcherType.PLACE;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(cxt);

		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		UserPlace lastKnownUserPlace = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			for(DurationUserEnv durationUserEnv : durationUserEnvDao.retrieveDurationUserEnv(rfdUCxt.getTime()
					, rfdUCxt.getEndTime(), EnvType.PLACE)){
				try {
					UserPlace userPlace = ((PlaceUserEnv)durationUserEnv.getUserEnv()).getPlace();
					if(lastKnownUserPlace == null) {
						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
						mergedRfdUCxtBuilder.setProperty("place", userPlace);
					} else {
							if(!userPlace.isSame(lastKnownUserPlace)){
								res.add(mergedRfdUCxtBuilder.build());
								mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
								mergedRfdUCxtBuilder.setProperty("place", userPlace);
							}
					}
					lastKnownUserPlace = userPlace;
				} catch (InvalidUserEnvException e) {
					;
				}
			}
		}
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
		return res;
	}
	
//	@Override
//	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
//		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//		
//		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
//		Entry<Date, UserLoc> lastKnownTimeAndPlace = null;
//		for(RfdUserCxt rfdUCxt : rfdUCxtList){
//			for(Entry<Date, UserLoc> timeAndPlace : rfdUCxt.getPlaces().entrySet()){
//				if(lastKnownTimeAndPlace == null) {
//					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//					mergedRfdUCxtBuilder.setPlace(timeAndPlace.getValue());
//				} else {
//					if(timeAndPlace.getValue().equals(lastKnownTimeAndPlace.getValue())
//							&& !moved(lastKnownTimeAndPlace, timeAndPlace)){
//						;
//					} else {
//						res.add(mergedRfdUCxtBuilder.build());
//						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//						mergedRfdUCxtBuilder.setPlace(timeAndPlace.getValue());
//					}
//				}
//				lastKnownTimeAndPlace = timeAndPlace;
//			}
//		}
//		if(mergedRfdUCxtBuilder != null)
//			res.add(mergedRfdUCxtBuilder.build());
//		return res;
//	}

	@Override
	protected double calcRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		try{
			UserPlace uPlace = ((PlaceUserEnv) uCxt.getUserEnv(EnvType.PLACE)).getPlace();
//			UserLoc uLoc = ((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
			if(uPlace.isSame((UserPlace) unit.getProperty("place")))
				return 1;
			else
				return 0;
		} catch (InvalidUserEnvException e) {
			return 0;
		}
	}
//		if(rfdUCxt.getPlace().equals(((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)).getPlace()))
//			return 1;
//		else
//			return 0;
//		Map<Date, UserLoc> locs = rfdUCxt.getLocs();
//		for(UserLoc uLoc : locs.values()){
//			try {
//				if(Utils.Proximity(uLoc, uEnv.getLoc(), toleranceInMeter)){
//					return 1;
//				}
//			} catch (InvalidLocationException e) {
//				return 0;
//			}
//		}
	
	@Override
	protected double calcLikelihood(int numTotalCxt, int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
//		int numTotalCxt = matchedCxt.getNumTotalCxt();
//		Map<MatcherCountUnit, Double> relatedCxtMap = matchedCxt.getRelatedCxt();
		
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		if(numTotalCxt > 0)
			likelihood /= numTotalCxt;
		else
			likelihood = 0;
		return likelihood;
	}
	
//	private boolean moved(Entry<Date, UserLoc> start, Entry<Date, UserLoc> end){
//		ChangeUserEnvDao changedUserEnvDao = ChangeUserEnvDao.getInstance(cxt);
//		
//		Date sTime = start.getKey();
//		Date eTime = end.getKey();
//		if(changedUserEnvDao.retrieveChangedUserEnv(sTime, eTime, EnvType.PLACE).size() > 0){
//			Log.d("PlaceContextMatcher", "moved");
//			return true;
//		} else
//			return false;
//	}
}
	
//	public List<MatchedCxt> matchAndGetResult(UserEnv uEnv){
//		Map<String, Integer> totalNumMap = new HashMap<String, Integer>();
//		Map<String, Integer> matchedNumMap = new HashMap<String, Integer>();
//
//		Cursor cur = db.rawQuery("SELECT context_id, location_list, bhv_name FROM refined_context;", null);
//		Gson gson = new Gson();
//		while (cur.moveToNext()) {
//			int contextId = cur.getInt(0);
//			String loc = cur.getString(1).toString();
//			String bhvName= cur.getString(2);
//			Type listType = new TypeToken<ArrayList<LocFreq>>(){}.getType();
//			List<LocFreq> locFreqList = gson.fromJson(loc, listType);
//			
//			
//			if(totalNumMap.containsKey(bhvName)) totalNumMap.put(bhvName, totalNumMap.get(bhvName) + 1);
//			else totalNumMap.put(bhvName, 1);
//			
//			if(!matchedNumMap.containsKey(bhvName)) matchedNumMap.put(bhvName, 0);
//			for(LocFreq locFreq : locFreqList){
//				if(Proximity(locFreq.getULoc(), uEnv.getLoc(), toleranceInMeter)){
//					matchedNumMap.put(bhvName, matchedNumMap.get(bhvName) + 1);
//					break;
//				}
//			}
//		}
//		cur.close();
//
//		Map<String, Double> res = new HashMap<String, Double>();
//		for(String bhvName : totalNumMap.keySet()){
//			if(totalNumMap.get(bhvName) < 2 || matchedNumMap.get(bhvName) == 0) continue;
//			double likelihood = matchedNumMap.get(bhvName)*1.0 / totalNumMap.get(bhvName) * 100;
//			if(likelihood >= threshold) res.put(bhvName, likelihood);
//		}
//		res = MapUtil.sortByValue(res);
//		return res;
//		return null;
//	}
	
	
//	@Override
//	public List<MatchedCxt> matchAndGetResult(UserEnv uEnv){
//		List<MatchedCxt> res = new ArrayList<MatchedCxt>();
//		
//		List<RfdUserCxt> rfdUCxtList = retrieveCxt(uEnv);
//		totalCxtSize = rfdUCxtList.size();
//		Map<UserBhv, SparseArray<Double>> relatednessSparseArrayMap = new HashMap<UserBhv, SparseArray<Double>>();
//
//		for(RfdUserCxt rfdUCxt : rfdUCxtList) {
//			int contextId = rfdUCxt.getContextId();
//			UserBhv userBhv = rfdUCxt.getBhv();
//			
//			if(!relatednessSparseArrayMap.containsKey(userBhv)) relatednessSparseArrayMap.put(userBhv, new SparseArray<Double>());
//			SparseArray<Double> relatedCxtSparseArray = relatednessSparseArrayMap.get(userBhv);
//			double relatedness = calcRelatedness(rfdUCxt, uEnv);
//			relatedCxtSparseArray.put(contextId, relatedness);
//			relatednessSparseArrayMap.put(userBhv, relatedCxtSparseArray);
//		}
//
//		for(UserBhv userBhv : relatednessSparseArrayMap.keySet()){
//			SparseArray<Double> relatedCxtMap = relatednessSparseArrayMap.get(userBhv);
//			double likelihood = calcLikelihood(relatedCxtMap);
//			if(likelihood <= threshold) continue;
//
//			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
//			matchedCxt.setUserBhv(userBhv);
//			matchedCxt.setNumTotalCxt(relatedCxtMap.size());
//			matchedCxt.setLikelihood(likelihood);
//			matchedCxt.setRelatedCxt(relatedCxtMap);
//			matchedCxt.setCondition(conditionName());
//			res.add(matchedCxt);
//		}
//		return res;
//	}
	
//	Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();
//	
//	UserLoc curPlace = null;
////	RfdUserCxt prevRfdUCxt = null;
//	boolean isMoved = false;
//	for(RfdUserCxt rfdUCxt : rfdUCxtList){
//		if(rfdUCxt.getPlaces().isEmpty()) continue;
//
//		if(curPlace == null) curPlace = rfdUCxt.getPlaces().get(0).getULoc();
////		if(prevRfdUCxt == null) prevRfdUCxt = rfdUCxt;
//		
//		UserBhv uBhv = rfdUCxt.getBhv();
//		if(ongoingBhvMap.isEmpty() || !ongoingBhvMap.containsKey(uBhv)) {
//			ongoingBhvMap.put(uBhv, rfdUCxt);
//		}
//		else {
////			if()
//			for(LocFreq placeFreq : rfdUCxt.getPlaces()){
//				
//			}
//		}
////		prevRfdUCxt = rfdUCxt;
//		
//		if(isMoved){
//			for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//				res.add(ongoingBhvMap.get(ongoingBhv));
//			}
//			ongoingBhvMap.clear();
//			isMoved = false;
//		}
//	}
//	
//	for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//		RfdUserCxt restRfdUCxt = ongoingBhvMap.get(ongoingBhv);
//		res.add(restRfdUCxt);
//	}

//private boolean Proximity(RfdUserCxt prevRfdUCxt, RfdUserCxt curRfdUCxt){
//	if(curRfdUCxt.getPlaces().isEmpty()) return false;
//	if(curRfdUCxt.getPlaces().size() > 1) return true;
//	else if(!prevRfdUCxt.getPlaces().get(prevRfdUCxt.getPlaces().size()-1).
//	equals(curRfdUCxt.getPlaces().get(0)))
//		return true;
//	else
//		return false;
//}

//@Override
//protected List<RfdUserCxt> retrieveCxtByBhv(UserEnv uEnv){
//	long time = uEnv.getTime().getTime();
//	List<RfdUserCxt> res = contextManager.retrieveRfdCxt(time - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY), time);
//	return res;
//}
//
