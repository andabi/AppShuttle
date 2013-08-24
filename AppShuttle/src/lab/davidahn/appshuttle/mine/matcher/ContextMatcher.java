package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.context.RfdUserCxt;
import lab.davidahn.appshuttle.context.RfdUserCxtDao;
import lab.davidahn.appshuttle.context.UserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public abstract class ContextMatcher {
	protected MatcherType matcherType;
	protected SQLiteDatabase db;
	protected Context cxt;
	protected SharedPreferences settings;
	protected double minLikelihood;
	protected int minNumCxt;

	protected MatcherType getMatcherType(){
		return matcherType;
	}

	public ContextMatcher(Context cxt, double minLikelihood, int minNumCxt) {
		this.cxt = cxt;
		db = DBHelper.getInstance(cxt.getApplicationContext()).getReadableDatabase();
		settings = cxt.getSharedPreferences("AppShuttle",Context.MODE_PRIVATE);

		this.minLikelihood = minLikelihood;
		this.minNumCxt = minNumCxt;
	}

	public MatchedResult matchAndGetResult(UserBhv uBhv, UserCxt uCxt){
		RfdUserCxtDao rfdUserCxtDao = RfdUserCxtDao.getInstance(cxt);

		Map<EnvType, UserEnv> uEnvs = uCxt.getUserEnvs();
		
		long etime = uCxt.getTime().getTime();
		long sTime = etime - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);
		
		List<RfdUserCxt> rfdUCxtList = rfdUserCxtDao.retrieveRfdCxtByBhv(sTime, etime, uBhv);
		List<RfdUserCxt> pureRfdUCxtList = new ArrayList<RfdUserCxt>();
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			if(rfdUCxt.getEndTime().getTime() - rfdUCxt.getStartTime().getTime() 
					< settings.getLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15 / 6 / 2))   //noise
				continue;
			pureRfdUCxtList.add(rfdUCxt);
		}
		List<MatcherCountUnit> mergedRfdCxtList = mergeCxtByCountUnit(pureRfdUCxtList);
		
		int numTotalCxt = 0;
		int numRelatedCxt = 0;
		Map<MatcherCountUnit, Double> relatedCxt = new HashMap<MatcherCountUnit, Double>();
		
		for(MatcherCountUnit mergedRfdCxt : mergedRfdCxtList) {
			numTotalCxt++;
			double relatedness = calcRelatedness(mergedRfdCxt, uCxt);
			if(relatedness > 0 ) {
				numRelatedCxt++;
				relatedCxt.put(mergedRfdCxt, relatedness);
			}
		}

		double likelihood = calcLikelihood(numTotalCxt, numRelatedCxt, relatedCxt);
		if(numRelatedCxt < minNumCxt || likelihood < minLikelihood)
			return null;
		else {
			MatchedResult matchedCxt = new MatchedResult(uCxt.getTime(), uCxt.getTimeZone(), uEnvs);
			matchedCxt.setUserBhv(uBhv);
			matchedCxt.setMatcherType(getMatcherType());
			matchedCxt.setNumTotalCxt(numTotalCxt);
			matchedCxt.setNumRelatedCxt(numRelatedCxt);
			matchedCxt.setRelatedCxt(relatedCxt);
			matchedCxt.setLikelihood(likelihood);
			return matchedCxt;
		}
	}
	
	protected double calcLikelihood(int numTotalCxt, int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap){
		double likelihood = 0;
		for(double relatedness : relatedCxtMap.values()){
			likelihood+=relatedness;
		}
		likelihood /= numTotalCxt;
		return likelihood;
	}	

	protected abstract List<MatcherCountUnit> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList);
	protected abstract double calcRelatedness(MatcherCountUnit rfdUCxt, UserCxt uCxt);
	
//			if(numRelatedCxt >= minNumCxt && likelihood >= minLikelihood)
//				matchedCxt.setMatched(true);
//			else
//				matchedCxt.setMatched(false);
	
//		Map<UserBhv, Integer> numTotalCxtByBhv = new HashMap<UserBhv, Integer>();
//		Map<UserBhv, Integer> numRelatedCxtByBhv = new HashMap<UserBhv, Integer>();
//		Map<UserBhv, SparseArray<Double>> relatedCxtByBhv = new HashMap<UserBhv, SparseArray<Double>>();

//		for(RfdUserCxt rfdUCxt : rfdUCxtList) {
//			int contextId = rfdUCxt.getContextId();
//			UserBhv userBhv = rfdUCxt.getBhv();
//
//			//numTotalCxtByBhv
//			if(!numTotalCxtByBhv.containsKey(userBhv)) numTotalCxtByBhv.put(userBhv, 1);
//			numTotalCxtByBhv.put(userBhv, numTotalCxtByBhv.get(userBhv) + 1);
//			
//			double relatedness = calcRelatedness(rfdUCxt, uEnv);			
//			if(relatedness > 0 ) {
//				//numRelatedCxtByBhv
//				if(!numRelatedCxtByBhv.containsKey(userBhv)) numRelatedCxtByBhv.put(userBhv, 0);
//				numRelatedCxtByBhv.put(userBhv, numRelatedCxtByBhv.get(userBhv) + 1);
//				
//				//relatedCxtByBhv
//				if(!relatedCxtByBhv.containsKey(userBhv)) relatedCxtByBhv.put(userBhv, new SparseArray<Double>());
//				SparseArray<Double> relatedCxtMap = relatedCxtByBhv.get(userBhv);
//				relatedCxtMap.put(contextId, relatedness);
//				relatedCxtByBhv.put(userBhv, relatedCxtMap);
//			}
//		}

//		for(UserBhv userBhv : relatedCxtByBhv.keySet()){
//			int numRelatedCxt = numRelatedCxtByBhv.get(userBhv);
//			
//			if(numRelatedCxt < minNumCxt) continue;
//			
//			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
//			matchedCxt.setUserBhv(userBhv);
//			matchedCxt.setCondition(conditionName());
//			matchedCxt.setNumTotalCxt(numTotalCxtByBhv.get(userBhv));
//			matchedCxt.setNumRelatedCxt(numRelatedCxtByBhv.get(userBhv));
//			matchedCxt.setRelatedCxt(relatedCxtByBhv.get(userBhv));
//			double likelihood = calcLikelihood(matchedCxt);
//
//			if(likelihood < minLikelihood) continue;
//
//			matchedCxt.setLikelihood(likelihood);
//			
//			res.add(matchedCxt);
//		}
//		return res;
}
