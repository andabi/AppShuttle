package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.bean.MatchedCxt;
import lab.davidahn.appshuttle.bean.MergedRfdUserCxt;
import lab.davidahn.appshuttle.bean.RfdUserCxt;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collector.ContextManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public abstract class ContextMatcher {
	protected ContextManager contextManager;
	protected UserBhvManager userBhvManager;
	protected SQLiteDatabase db;
	protected Context cxt;
	protected SharedPreferences settings;
	protected int numTotalCxt;
	protected double minLikelihood;
	protected int minNumCxt;
	protected String condition;

	public ContextMatcher(Context cxt, double minLikelihood, int minNumCxt) {
		this.cxt = cxt;
		contextManager = ContextManager.getInstance(cxt);
		userBhvManager = UserBhvManager.getInstance(cxt);
		db = DBHelper.getInstance(cxt.getApplicationContext()).getReadableDatabase();
		settings = cxt.getSharedPreferences("AppShuttle",Context.MODE_PRIVATE);

		numTotalCxt = 0;
		this.minLikelihood = minLikelihood;
		this.minNumCxt = minNumCxt;
	}

	public List<MatchedCxt> matchAndGetResult(UserEnv uEnv){
		List<MatchedCxt> res = new ArrayList<MatchedCxt>();
		
		long etime = uEnv.getTime().getTime();
		long sTime = etime - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);
		
		for(UserBhv uBhv : userBhvManager.retrieveBhv()){
			List<RfdUserCxt> rfdUCxtList = contextManager.retrieveRfdCxtByBhv(sTime, etime, uBhv);
			numTotalCxt+=rfdUCxtList.size();
		}
		
		for(UserBhv uBhv : userBhvManager.retrieveBhv()){
			List<RfdUserCxt> rfdUCxtList = contextManager.retrieveRfdCxtByBhv(sTime, etime, uBhv);
			List<MergedRfdUserCxt> mergedRfdCxtList = mergeCxtByCountUnit(rfdUCxtList);
			
			int numTotalCxt = 0;
			int numRelatedCxt = 0;
			Map<MergedRfdUserCxt, Double> relatedCxt = new HashMap<MergedRfdUserCxt, Double>();
			
			for(MergedRfdUserCxt mergedRfdCxt : mergedRfdCxtList) {
				numTotalCxt++;
				double relatedness = calcRelatedness(mergedRfdCxt, uEnv);			
				if(relatedness > 0 ) {
					numRelatedCxt++;
					relatedCxt.put(mergedRfdCxt, relatedness);
				}
			}
			
			if(numRelatedCxt < minNumCxt) continue;
			
			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
			matchedCxt.setUserBhv(uBhv);
			matchedCxt.setCondition(getCondition());
			matchedCxt.setNumTotalCxt(numTotalCxt);
			matchedCxt.setNumRelatedCxt(numRelatedCxt);
			matchedCxt.setRelatedCxt(relatedCxt);

			double likelihood = calcLikelihood(matchedCxt);
			if(likelihood < minLikelihood) continue;

			matchedCxt.setLikelihood(likelihood);
			
			res.add(matchedCxt);
		}
		return res;
		
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
	
	protected String getCondition(){
		return condition;
	}
	protected abstract List<MergedRfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList);
	protected abstract double calcRelatedness(MergedRfdUserCxt rfdUCxt, UserEnv uEnv);
	protected abstract double calcLikelihood(MatchedCxt matchedCxt);
}
