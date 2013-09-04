package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.PriorityQueue;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

public class Predictor {
	private Context cxt;
	private SharedPreferences settings;
	
	public Predictor(Context cxt){
		this.cxt = cxt;
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}
	
	public List<PredictedBhv> predict(int topN){
		List<PredictedBhv> res = new ArrayList<PredictedBhv>();
		if(GlobalState.currUserCxt == null) 
			return res;
		
		PriorityQueue<PredictedBhv> predicted = new PriorityQueue<PredictedBhv>();

		List<ContextMatcher> cxtMatcherList = new ArrayList<ContextMatcher>();
		cxtMatcherList.add(new WeakTimeContextMatcher(cxt
				, settings.getFloat("matcher.weak_time.min_likelihood", 0.7f)
				, settings.getInt("matcher.weak_time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, settings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6)));
		cxtMatcherList.add(new StrictTimeContextMatcher(cxt
				, settings.getFloat("matcher.strict_time.min_likelihood", 0.3f)
				, settings.getInt("matcher.strict_time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, settings.getLong("matcher.strict_time.tolerance", AlarmManager.INTERVAL_HOUR / 6)));
		cxtMatcherList.add(new PlaceContextMatcher(cxt
				, settings.getFloat("matcher.place.min_likelihood", 0.7f)
				, settings.getInt("matcher.place.min_num_cxt", 3)
				, settings.getInt("matcher.place.min_distance", 2000)));
		cxtMatcherList.add(new LocContextMatcher(cxt
				, settings.getFloat("matcher.loc.min_likelihood", 0.5f)
				, settings.getInt("matcher.loc.min_num_cxt", 5)
				, settings.getInt("matcher.loc.min_distance", 50)));
		cxtMatcherList.add(new FreqContextMatcher(cxt
				, Double.MIN_VALUE
				, settings.getInt("matcher.freq.min_num_cxt", 3)));

		UserBhvManager userBhvManager = UserBhvManager.getInstance(cxt);
		for(UserBhv uBhv : userBhvManager.getBhvList()){
			EnumMap<MatcherType, MatchedResult> matchedResults = new EnumMap<MatcherType, MatchedResult>(MatcherType.class);

			for(ContextMatcher cxtMatcher : cxtMatcherList){
				MatchedResult matchedResult = cxtMatcher.matchAndGetResult(uBhv, GlobalState.currUserCxt);
				if(matchedResult != null)
					matchedResults.put(cxtMatcher.getMatcherType(), matchedResult);
			}

			if(matchedResults.size() > 0){
				double score = calcScore(matchedResults);
				PredictedBhv predictedBhv = new PredictedBhv(GlobalState.currUserCxt.getTime(), 
						GlobalState.currUserCxt.getTimeZone(), 
						GlobalState.currUserCxt.getUserEnvs(), 
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
			score += Math.pow(10, matcherType.getPriority()) + matchedResults.get(matcherType).getLikelihood();
		}
		return score;
	}

	public void storePredictedBhv(PredictedBhv predictedBhv){
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance(cxt);
		predictedBhvDao.storePredictedBhv(predictedBhv);
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