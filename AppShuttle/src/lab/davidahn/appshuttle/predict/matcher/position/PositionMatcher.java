package lab.davidahn.appshuttle.predict.matcher.position;

import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;

public abstract class PositionMatcher extends Matcher<PositionMatcherConf> {

	public PositionMatcher(PositionMatcherConf conf){
		super(conf);
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		double inverseEntropy = matcherResult.getInverseEntropy();
		
		double score = (1 + 0.5 * inverseEntropy + 0.1 * likelihood);
		
		assert(1 <= score && score <=2);
		
		return score;
	}

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
//		List<RfdUserCxt> durationUserBhvList = retrieveCxt(uEnv);
//		totalCxtSize = durationUserBhvList.size();
//		Map<UserBhv, SparseArray<Double>> relatednessSparseArrayMap = new HashMap<UserBhv, SparseArray<Double>>();
//
//		for(RfdUserCxt durationUserBhv : durationUserBhvList) {
//			int contextId = durationUserBhv.getContextId();
//			UserBhv userBhv = durationUserBhv.getBhv();
//			
//			if(!relatednessSparseArrayMap.containsKey(userBhv)) relatednessSparseArrayMap.put(userBhv, new SparseArray<Double>());
//			SparseArray<Double> relatedHistorySparseArray = relatednessSparseArrayMap.get(userBhv);
//			double relatedness = computeRelatedness(durationUserBhv, uEnv);
//			relatedHistorySparseArray.put(contextId, relatedness);
//			relatednessSparseArrayMap.put(userBhv, relatedHistorySparseArray);
//		}
//
//		for(UserBhv userBhv : relatednessSparseArrayMap.keySet()){
//			SparseArray<Double> relatedHistoryMap = relatednessSparseArrayMap.get(userBhv);
//			double likelihood = computeLikelihood(relatedHistoryMap);
//			if(likelihood <= threshold) continue;
//
//			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
//			matchedCxt.setUserBhv(userBhv);
//			matchedCxt.setNumTotalCxt(relatedHistoryMap.size());
//			matchedCxt.setLikelihood(likelihood);
//			matchedCxt.setRelatedCxt(relatedHistoryMap);
//			matchedCxt.setCondition(conditionName());
//			res.add(matchedCxt);
//		}
//		return res;
//	}
	
//	Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();
//	
//	UserLoc curPlace = null;
////	RfdUserCxt prevDurationUserBhv = null;
//	boolean isMoved = false;
//	for(RfdUserCxt durationUserBhv : durationUserBhvList){
//		if(durationUserBhv.getPlaces().isEmpty()) continue;
//
//		if(curPlace == null) curPlace = durationUserBhv.getPlaces().get(0).getULoc();
////		if(prevDurationUserBhv == null) prevDurationUserBhv = durationUserBhv;
//		
//		UserBhv uBhv = durationUserBhv.getBhv();
//		if(ongoingBhvMap.isEmpty() || !ongoingBhvMap.containsKey(uBhv)) {
//			ongoingBhvMap.put(uBhv, durationUserBhv);
//		}
//		else {
////			if()
//			for(LocFreq placeFreq : durationUserBhv.getPlaces()){
//				
//			}
//		}
////		prevDurationUserBhv = durationUserBhv;
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
//		RfdUserCxt restDurationUserBhv = ongoingBhvMap.get(ongoingBhv);
//		res.add(restDurationUserBhv);
//	}

//private boolean Proximity(RfdUserCxt prevDurationUserBhv, RfdUserCxt curDurationUserBhv){
//	if(curDurationUserBhv.getPlaces().isEmpty()) return false;
//	if(curDurationUserBhv.getPlaces().size() > 1) return true;
//	else if(!prevDurationUserBhv.getPlaces().get(prevDurationUserBhv.getPlaces().size()-1).
//	equals(curDurationUserBhv.getPlaces().get(0)))
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
