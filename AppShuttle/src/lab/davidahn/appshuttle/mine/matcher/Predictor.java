package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.PriorityQueue;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

public class Predictor {
	private Context cxt;
	private SharedPreferences preferenceSettings;
	
	public Predictor(Context cxt){
		this.cxt = cxt;		
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
	}
	
	public List<PredictedBhv> predict(int topN){
		List<PredictedBhv> res = new ArrayList<PredictedBhv>();
		SnapshotUserCxt currUserCxt = ((AppShuttleApplication)cxt.getApplicationContext()).getCurrUserCxt();
		if(((AppShuttleApplication) cxt.getApplicationContext()).getCurrUserCxt() == null) 
			return res;
		
		PriorityQueue<PredictedBhv> predicted = new PriorityQueue<PredictedBhv>();

		List<ContextMatcher> cxtMatcherList = new ArrayList<ContextMatcher>();
		cxtMatcherList.add(new WeakTimeContextMatcher(cxt
				, preferenceSettings.getLong("matcher.weak_time.duration", 5 * AlarmManager.INTERVAL_DAY)
				, preferenceSettings.getFloat("matcher.weak_time.min_likelihood", 0.7f)
				, preferenceSettings.getInt("matcher.weak_time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, preferenceSettings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6)
				, preferenceSettings.getLong("matcher.weak_time.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)
				));
		cxtMatcherList.add(new StrictTimeContextMatcher(cxt
				, preferenceSettings.getLong("matcher.strict_time.duration", 5 * AlarmManager.INTERVAL_DAY)
				, preferenceSettings.getFloat("matcher.strict_time.min_likelihood", 0.3f)
				, preferenceSettings.getInt("matcher.strict_time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, preferenceSettings.getLong("matcher.strict_time.tolerance", AlarmManager.INTERVAL_HOUR / 6)
				, preferenceSettings.getLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3)
				));
		cxtMatcherList.add(new PlaceContextMatcher(cxt
				, preferenceSettings.getLong("matcher.place.duration", 5 * AlarmManager.INTERVAL_DAY)
				, preferenceSettings.getFloat("matcher.place.min_likelihood", 0.7f)
				, preferenceSettings.getInt("matcher.place.min_num_cxt", 3)
				, preferenceSettings.getInt("matcher.place.distance_tolerance", 2000)
				));
		cxtMatcherList.add(new FreqContextMatcher(cxt
				, preferenceSettings.getLong("matcher.freq.duration", 5 * AlarmManager.INTERVAL_DAY)
				, Double.MIN_VALUE
				, preferenceSettings.getInt("matcher.freq.min_num_cxt", 3)
				, preferenceSettings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)
				));
//		cxtMatcherList.add(new LocContextMatcher(cxt
//				, preferenceSettings.getLong("matcher.loc.duration", AlarmManager.INTERVAL_HOUR / 6)
//				, preferenceSettings.getFloat("matcher.loc.min_likelihood", 0.5f)
//				, preferenceSettings.getInt("matcher.loc.min_num_cxt", 5)
//				, preferenceSettings.getInt("matcher.loc.distance_tolerance", 50)


		UserBhvManager userBhvManager = UserBhvManager.getInstance(cxt);
		for(UserBhv uBhv : userBhvManager.getBhvList()){
			EnumMap<MatcherType, MatchedResult> matchedResults = new EnumMap<MatcherType, MatchedResult>(MatcherType.class);

			for(ContextMatcher cxtMatcher : cxtMatcherList){
				MatchedResult matchedResult = cxtMatcher.matchAndGetResult(uBhv, currUserCxt);
				if(matchedResult != null)
					matchedResults.put(cxtMatcher.getMatcherType(), matchedResult);
			}

			if(matchedResults.size() > 0){
				double score = calcScore(matchedResults);
				PredictedBhv predictedBhv = new PredictedBhv(currUserCxt.getTime(), 
						currUserCxt.getTimeZone(), 
						currUserCxt.getUserEnvs(), 
						uBhv, matchedResults, score);
				predicted.add(predictedBhv);
				
			} else {
				userBhvManager.unregisterBhv(uBhv);
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
			int priority = matcherType.getPriority();
			double likelihood = matchedResults.get(matcherType).getLikelihood();
			double inverseEntropy = matchedResults.get(matcherType).getInverseEntropy();
			score += Math.pow(10, priority) * (1 + inverseEntropy + 0.1 * (1 + likelihood));
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
//, settings.getInt("matcher.loc.distance_tolerance", 2000));
//ContextMatcher FreqCxtMatcher = new FreqContextMatcher(cxt
//, Double.MIN_VALUE
//, settings.getInt("matcher.freq.min_num_cxt", 3));


//List<MatchedCxt> locMatchhedCxtList;
//if(GlobalState.recentLocMatchedCxtList == null) 
//	GlobalState.recentLocMatchedCxtList = new ArrayList<MatchedCxt>();
//if(GlobalState.moved == true){
//	ContextMatcher locCxtMatcher = new LocContextMatcher(cxt, 0, settings.getInt("location.distance_tolerance", 2000));
//	locMatchedCxtList = locCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);
//	GlobalState.recentLocMatchedCxtList = locMatchedCxtList;
//	Log.i("location", "moved");
//} else {
//	locMatchedCxtList = GlobalState.recentLocMatchedCxtList;
//}

//Collections.sort(res);
//res = res.subList(0, Math.min(res.size(), topN));