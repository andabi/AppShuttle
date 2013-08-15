package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.bean.MatchedCxt;
import lab.davidahn.appshuttle.bean.MergedRfdUserCxt;
import lab.davidahn.appshuttle.bean.RfdUserCxt;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.bean.UserLoc;
import android.content.Context;

public class LocContextMatcher extends ContextMatcher {
	double toleranceInMeter;

	public LocContextMatcher(Context cxt, double minLikelihood, int minNumCxt, double toleranceInMeter) {
		super(cxt, minLikelihood, minNumCxt);
		this.toleranceInMeter = toleranceInMeter;
		condition = "location";
	}
	
	@Override
	protected List<MergedRfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
		List<MergedRfdUserCxt> res = new ArrayList<MergedRfdUserCxt>();
		
		MergedRfdUserCxt.Builder mergedRfdUCxtBuilder = null;
		UserLoc lastKnownPlace = null;
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			for(UserLoc place : rfdUCxt.getPlaces().values()){
				if(lastKnownPlace == null) {
					mergedRfdUCxtBuilder = new MergedRfdUserCxt.Builder(rfdUCxt.getBhv());
					mergedRfdUCxtBuilder.setLocs(rfdUCxt.getLocs());
					mergedRfdUCxtBuilder.setPlaces(rfdUCxt.getPlaces());
				} else {
					//TODO move check
					if(place.equals(lastKnownPlace)) { // && !moved){
						;
					} else {
						res.add(mergedRfdUCxtBuilder.build());
						mergedRfdUCxtBuilder = new MergedRfdUserCxt.Builder(rfdUCxt.getBhv());
						mergedRfdUCxtBuilder.setLocs(rfdUCxt.getLocs());
						mergedRfdUCxtBuilder.setPlaces(rfdUCxt.getPlaces());
					}
				}
				lastKnownPlace = place;
			}
		}
		
		res.add(mergedRfdUCxtBuilder.build());
		return res;
		
//		Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();
//		
//		UserLoc curPlace = null;
////		RfdUserCxt prevRfdUCxt = null;
//		boolean isMoved = false;
//		for(RfdUserCxt rfdUCxt : rfdUCxtList){
//			if(rfdUCxt.getPlaces().isEmpty()) continue;
//
//			if(curPlace == null) curPlace = rfdUCxt.getPlaces().get(0).getULoc();
////			if(prevRfdUCxt == null) prevRfdUCxt = rfdUCxt;
//			
//			UserBhv uBhv = rfdUCxt.getBhv();
//			if(ongoingBhvMap.isEmpty() || !ongoingBhvMap.containsKey(uBhv)) {
//				ongoingBhvMap.put(uBhv, rfdUCxt);
//			}
//			else {
////				if()
//				for(LocFreq placeFreq : rfdUCxt.getPlaces()){
//					
//				}
//			}
////			prevRfdUCxt = rfdUCxt;
//			
//			if(isMoved){
//				for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//					res.add(ongoingBhvMap.get(ongoingBhv));
//				}
//				ongoingBhvMap.clear();
//				isMoved = false;
//			}
//		}
//		
//		for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//			RfdUserCxt restRfdUCxt = ongoingBhvMap.get(ongoingBhv);
//			res.add(restRfdUCxt);
//		}
	}
	
//	private boolean Proximity(RfdUserCxt prevRfdUCxt, RfdUserCxt curRfdUCxt){
//		if(curRfdUCxt.getPlaces().isEmpty()) return false;
//		if(curRfdUCxt.getPlaces().size() > 1) return true;
//		else if(!prevRfdUCxt.getPlaces().get(prevRfdUCxt.getPlaces().size()-1).
//		equals(curRfdUCxt.getPlaces().get(0)))
//			return true;
//		else
//			return false;
//	}

//	@Override
//	protected List<RfdUserCxt> retrieveCxtByBhv(UserEnv uEnv){
//		long time = uEnv.getTime().getTime();
//		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(time - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY), time);
//		return res;
//	}
//
	@Override
	protected double calcRelatedness(MergedRfdUserCxt rfdUCxt, UserEnv uEnv) {
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
		return 0;
	}
	
	@Override
	protected double calcLikelihood(MatchedCxt matchedCxt){
		int numTotalCxt = matchedCxt.getNumTotalCxt();
		Map<MergedRfdUserCxt, Double> relatedCxtMap = matchedCxt.getRelatedCxt();
		
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		likelihood /= numTotalCxt;
		likelihood *= 100;
		return likelihood;
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

}
