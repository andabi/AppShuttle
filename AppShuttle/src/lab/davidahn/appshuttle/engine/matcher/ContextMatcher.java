package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.DBHelper;
import lab.davidahn.appshuttle.bean.MatchedCxt;
import lab.davidahn.appshuttle.bean.RfdUserCxt;
import lab.davidahn.appshuttle.bean.UserBhv;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.collector.ContextManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

public abstract class ContextMatcher {
	protected ContextManager contextManager;
	protected SQLiteDatabase db;
	protected Context cxt;
	protected SharedPreferences settings;
	protected int numTotalCxt;
	protected double minLikelihood;
	protected int minNumCxt;

	public ContextMatcher(Context cxt, double minLikelihood, int minNumCxt) {
		this.cxt = cxt;
		contextManager = ContextManager.getInstance(cxt);
		db = DBHelper.getInstance(cxt.getApplicationContext()).getReadableDatabase();
		settings = cxt.getSharedPreferences("AppShuttle",Context.MODE_PRIVATE);

		numTotalCxt = 0;
		this.minLikelihood = minLikelihood;
		this.minNumCxt = minNumCxt;
	}

	public List<MatchedCxt> matchAndGetResult(UserEnv uEnv){
		List<MatchedCxt> res = new ArrayList<MatchedCxt>();
		
		List<RfdUserCxt> rfdUCxtList = retrieveCxt(uEnv);
		rfdUCxtList = mergeCxtByCountUnit(rfdUCxtList);
		rfdUCxtList = contextManager.getCxtRefiner().filter(cxt, rfdUCxtList);
		
		numTotalCxt = rfdUCxtList.size();
		Map<UserBhv, Integer> numTotalCxtByBhv = new HashMap<UserBhv, Integer>();
		Map<UserBhv, Integer> numRelatedCxtByBhv = new HashMap<UserBhv, Integer>();
		Map<UserBhv, SparseArray<Double>> relatedCxtByBhv = new HashMap<UserBhv, SparseArray<Double>>();

		for(RfdUserCxt rfdUCxt : rfdUCxtList) {
			int contextId = rfdUCxt.getContextId();
			UserBhv userBhv = rfdUCxt.getBhv();

			//numTotalCxtByBhv
			if(!numTotalCxtByBhv.containsKey(userBhv)) numTotalCxtByBhv.put(userBhv, 1);
			numTotalCxtByBhv.put(userBhv, numTotalCxtByBhv.get(userBhv) + 1);
			
			double relatedness = calcRelatedness(rfdUCxt, uEnv);			
			if(relatedness > 0 ) {
				//numRelatedCxtByBhv
				if(!numRelatedCxtByBhv.containsKey(userBhv)) numRelatedCxtByBhv.put(userBhv, 0);
				numRelatedCxtByBhv.put(userBhv, numRelatedCxtByBhv.get(userBhv) + 1);
				
				//relatedCxtByBhv
				if(!relatedCxtByBhv.containsKey(userBhv)) relatedCxtByBhv.put(userBhv, new SparseArray<Double>());
				SparseArray<Double> relatedCxtMap = relatedCxtByBhv.get(userBhv);
				relatedCxtMap.put(contextId, relatedness);
				relatedCxtByBhv.put(userBhv, relatedCxtMap);
			}
		}

		for(UserBhv userBhv : relatedCxtByBhv.keySet()){
			int numRelatedCxt = numRelatedCxtByBhv.get(userBhv);
			
			if(numRelatedCxt < minNumCxt) continue;
			
			MatchedCxt matchedCxt = new MatchedCxt(uEnv);
			matchedCxt.setUserBhv(userBhv);
			matchedCxt.setCondition(conditionName());
			matchedCxt.setNumTotalCxt(numTotalCxtByBhv.get(userBhv));
			matchedCxt.setNumRelatedCxt(numRelatedCxtByBhv.get(userBhv));
			matchedCxt.setRelatedCxt(relatedCxtByBhv.get(userBhv));
			double likelihood = calcLikelihood(matchedCxt);

			if(likelihood < minLikelihood) continue;

			matchedCxt.setLikelihood(likelihood);
			
			res.add(matchedCxt);
		}
		return res;
	}
	
	protected List<RfdUserCxt> retrieveCxt(UserEnv uEnv){
		long time = uEnv.getTime().getTime();
		List<RfdUserCxt> res = contextManager.retrieveRfdCxt(time - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY), time);
		return res;
	}
	
	protected abstract List<RfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList);
	protected abstract double calcRelatedness(RfdUserCxt rfdUCxt, UserEnv uEnv);
	protected abstract double calcLikelihood(MatchedCxt matchedCxt);
	protected abstract String conditionName();
}
