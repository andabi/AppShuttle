package lab.davidahn.appshuttle.engine.matcher;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.PriorityQueue;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.bean.MatcherType;
import lab.davidahn.appshuttle.bean.cxt.MatchedResult;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collector.ContextManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

public class Predictor {
	private ContextManager contextManager;
	private UserBhvManager userBhvManager;
	private Context cxt;
	private SharedPreferences settings;
	
	public Predictor(Context cxt){
		this.cxt = cxt;
		contextManager = ContextManager.getInstance(cxt);
		userBhvManager = UserBhvManager.getInstance(cxt);
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}
	
	public List<PredictedBhv> predict(int topN){
		List<PredictedBhv> res = new ArrayList<PredictedBhv>();
		PriorityQueue<PredictedBhv> predicted = new PriorityQueue<PredictedBhv>();

		List<ContextMatcher> cxtMatcherList = new ArrayList<ContextMatcher>();
		cxtMatcherList.add(new TimeContextMatcher(cxt
				, settings.getFloat("matcher.time.min_likelihood", 0.7f)
				, settings.getInt("matcher.time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, settings.getLong("matcher.time.tolerance", AlarmManager.INTERVAL_HOUR / 6)));
		cxtMatcherList.add(new LocContextMatcher(cxt
				, settings.getFloat("matcher.loc.min_likelihood", 0.7f)
				, settings.getInt("matcher.loc.min_num_cxt", 3)
				, settings.getInt("matcher.loc.min_distance", 2000)));
		cxtMatcherList.add(new FreqContextMatcher(cxt
				, Double.MIN_VALUE
				, settings.getInt("matcher.freq.min_num_cxt", 3)));

		for(UserBhv uBhv : userBhvManager.retrieveBhv()){
			EnumMap<MatcherType, MatchedResult> matchedResults = new EnumMap<MatcherType, MatchedResult>(MatcherType.class);

			for(ContextMatcher cxtMatcher : cxtMatcherList){
				MatchedResult matchedResult = cxtMatcher.matchAndGetResult(uBhv, GlobalState.currentUCxt);
				if(matchedResult != null)
					matchedResults.put(cxtMatcher.getMatcherType(), matchedResult);
			}

			if(matchedResults.size() > 0){
				double score = calcScore(matchedResults);
				PredictedBhv predictedBhv = new PredictedBhv(GlobalState.currentUCxt.getTime(), 
						GlobalState.currentUCxt.getTimeZone(), 
						GlobalState.currentUCxt.getUserEnvs(), 
						uBhv, matchedResults, score);
				predicted.add(predictedBhv);
			}
		}

		for(int i=0;i<topN;i++){
			if(predicted.isEmpty()) break;
			res.add(predicted.remove());
		}
		return res;
	}
	
	private double calcScore(EnumMap<MatcherType, MatchedResult> matchedResults){
		double score = 0;
		for(MatcherType matcherType : matchedResults.keySet()){
//			if(matchedResults.get(matcherType).isMatched())
			score += Math.pow(10, matcherType.getPriority()) * matchedResults.get(matcherType).getLikelihood();
		}
		return score;
	}

	public void storePredictedBhv(PredictedBhv predictedBhv){
		contextManager.storePredictedBhv(predictedBhv);
		for(MatchedResult matchedRes : predictedBhv.getMatchedResults().values()) {
			contextManager.storeMatchedCxt(matchedRes);
		}
	}
}
//ContextMatcher timeCxtMatcher = new TimeContextMatcher(cxt
//, settings.getFloat("matcher.time.min_likelihood", 0.7f)
//, settings.getInt("matcher.time.min_num_cxt", 3)
//, AlarmManager.INTERVAL_DAY
//, settings.getLong("matcher.time.tolerance", AlarmManager.INTERVAL_HOUR / 6));
//ContextMatcher locCxtMatcher = new LocContextMatcher(cxt
//, settings.getFloat("matcher.loc.min_likelihood", 0.7f)
//, settings.getInt("matcher.loc.min_num_cxt", 3)
//, settings.getInt("matcher.loc.min_distance", 2000));
//ContextMatcher FreqCxtMatcher = new FreqContextMatcher(cxt
//, Double.MIN_VALUE
//, settings.getInt("matcher.freq.min_num_cxt", 3));


//List<MatchedCxt> locMatchhedCxtList;
//if(GlobalState.recentLocMatchedCxtList == null) 
//	GlobalState.recentLocMatchedCxtList = new ArrayList<MatchedCxt>();
//if(GlobalState.moved == true){
//	ContextMatcher locCxtMatcher = new LocContextMatcher(cxt, 0, settings.getInt("location.min_distance", 2000));
//	locMatchedCxtList = locCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);
//	GlobalState.recentLocMatchedCxtList = locMatchedCxtList;
//	Log.i("location", "moved");
//} else {
//	locMatchedCxtList = GlobalState.recentLocMatchedCxtList;
//}

//Collections.sort(res);
//res = res.subList(0, Math.min(res.size(), topN));