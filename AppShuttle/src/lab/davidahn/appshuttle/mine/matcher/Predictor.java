package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.utils.MapUtils;
import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.util.Log;

public class Predictor {
	private SharedPreferences _preferenceSettings;
	
	private static Predictor predictor = new Predictor();
	private Predictor(){
//		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		_preferenceSettings = AppShuttleApplication.getContext().getPreferenceSettings();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	public void predict(){
		SnapshotUserCxt currUserCxt = AppShuttleApplication.currUserCxt;

		if(currUserCxt == null)
			return ;
//			return Collections.emptyList();

		Map<UserBhv, PredictionInfo> predicted = new HashMap<UserBhv, PredictionInfo>();
//		PriorityQueue<PredictedBhvInfo> predicted = new PriorityQueue<PredictedBhvInfo>();

		List<TemplateContextMatcher> cxtMatcherList = new ArrayList<TemplateContextMatcher>();
		if(MatcherType.FREQUENCY.enabled){
			cxtMatcherList.add(new FreqContextMatcher(
					currUserCxt.getTimeDate()
					, _preferenceSettings.getLong("matcher.freq.duration", AlarmManager.INTERVAL_DAY)
					, 0.0
					, 0.0
					, _preferenceSettings.getInt("matcher.freq.min_num_cxt", 3)
					, _preferenceSettings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)
					));
		}
		if(MatcherType.WEAK_TIME.enabled){
			cxtMatcherList.add(new WeakTimeContextMatcher(
					new Date(currUserCxt.getTimeDate().getTime() - _preferenceSettings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6))
					, _preferenceSettings.getLong("matcher.weak_time.duration", 5 * AlarmManager.INTERVAL_DAY)
					, _preferenceSettings.getFloat("matcher.weak_time.min_likelihood", 0.5f)
					, _preferenceSettings.getFloat("matcher.weak_time.min_inverse_entropy", 0.2f)
					, _preferenceSettings.getInt("matcher.weak_time.min_num_cxt", 3)
					, AlarmManager.INTERVAL_DAY
					, _preferenceSettings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6)
					, _preferenceSettings.getLong("matcher.weak_time.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)
					));
		}
		if(MatcherType.STRICT_TIME.enabled){
			cxtMatcherList.add(new StrictTimeContextMatcher(
					new Date(currUserCxt.getTimeDate().getTime() - _preferenceSettings.getLong("matcher.strict_time.tolerance", AlarmManager.INTERVAL_HOUR / 6))
					, _preferenceSettings.getLong("matcher.strict_time.duration", 6 * AlarmManager.INTERVAL_DAY)
					, _preferenceSettings.getFloat("matcher.strict_time.min_likelihood", 0.3f)
					, _preferenceSettings.getFloat("matcher.strict_time.min_inverse_entropy", 0.2f)
					, _preferenceSettings.getInt("matcher.strict_time.min_num_cxt", 3)
					, AlarmManager.INTERVAL_DAY
					, _preferenceSettings.getLong("matcher.strict_time.tolerance", AlarmManager.INTERVAL_HOUR / 6)
					, _preferenceSettings.getLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3)
					));
		}
		if(MatcherType.PLACE.enabled){
			cxtMatcherList.add(new PlaceContextMatcher(
					currUserCxt.getTimeDate()
					, _preferenceSettings.getLong("matcher.place.duration", 6 * AlarmManager.INTERVAL_DAY)
					, _preferenceSettings.getFloat("matcher.place.min_likelihood", 0.7f)
					, _preferenceSettings.getFloat("matcher.place.min_inverse_entropy", 0.3f)
					, _preferenceSettings.getInt("matcher.place.min_num_cxt", 3)
					, _preferenceSettings.getInt("matcher.place.distance_tolerance", 2000)
					));
		}
		if(MatcherType.LOCATION.enabled){
			cxtMatcherList.add(new LocContextMatcher(
					currUserCxt.getTimeDate()
					, _preferenceSettings.getLong("matcher.loc.duration", AlarmManager.INTERVAL_HOUR / 6)
					, _preferenceSettings.getFloat("matcher.loc.min_likelihood", 0.5f)
					, _preferenceSettings.getFloat("matcher.loc.min_inverse_entropy", 0.2f)
					, _preferenceSettings.getInt("matcher.loc.min_num_cxt", 5)
					, _preferenceSettings.getInt("matcher.loc.distance_tolerance", 50)
					));
		}
		
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		long noiseTimeTolerance = _preferenceSettings.getLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		for(UserBhv uBhv : userBhvManager.getTotalBhvSet()){
			EnumMap<MatcherType, MatchedResult> matchedResultMap = new EnumMap<MatcherType, MatchedResult>(MatcherType.class);
			for(TemplateContextMatcher cxtMatcher : cxtMatcherList){
				MatchedResult matchedResult = cxtMatcher.matchAndGetResult(uBhv, currUserCxt, noiseTimeTolerance);
				if(matchedResult == null)
					continue;
				matchedResultMap.put(cxtMatcher.getMatcherType(), matchedResult);
			}

			if(matchedResultMap.isEmpty()) {
				continue;
			}
			
			double score = computePredictionScore(matchedResultMap);
			PredictionInfo predictedBhv = new PredictionInfo(currUserCxt.getTimeDate(), 
					currUserCxt.getTimeZone(), 
					currUserCxt.getUserEnvs(), 
					uBhv, matchedResultMap, score);
			predicted.put(uBhv, predictedBhv);
		}
		
		MapUtils.sortByValue(predicted);

		AppShuttleApplication.recentPredictedBhvMap = predicted;
//		AppShuttleApplication.getContext().setRecentPredictedBhvInfoList(predictedBhvInfoList);
	}
	
	public List<PredictionInfo> getRecentPredictedBhv(int topN){
		Map<UserBhv, PredictionInfo> predictedBhvMap = AppShuttleApplication.recentPredictedBhvMap;
		
		if(predictedBhvMap == null)
			return null;
		
		Log.d("test", predictedBhvMap.keySet().toString());
		
		List<PredictionInfo> predictedBhvList = new ArrayList<PredictionInfo>(predictedBhvMap.values());
		
		return predictedBhvList.subList(0, Math.min(predictedBhvList.size(), topN));
	}
	
	public PredictionInfo getPredictedBhv(UserBhv uBhv){
		Map<UserBhv, PredictionInfo> predictedBhvMap = AppShuttleApplication.recentPredictedBhvMap;
		
		if(predictedBhvMap == null)
			return null;
		
		return predictedBhvMap.get(uBhv);
	}
	
	private double computePredictionScore(EnumMap<MatcherType, MatchedResult> matchedResults){
		double PredictionScore = 0;
		
		for(MatcherType matcherType : matchedResults.keySet()){
			double weight = Math.pow(10, matcherType.priority);
			double score = weight * matchedResults.get(matcherType).getScore();;
			PredictionScore += score;
		}
		return PredictionScore;
	}
	

	public void storeNewPredictedBhv(List<PredictionInfo> predictedBhvInfoList) {
//		Set<BaseUserBhv> lastPredictedBhvSet = recentPredictedBhvSet;
		
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance();

//		Set<BaseUserBhv> currPredictedBhvSet = new HashSet<BaseUserBhv>();
		for(PredictionInfo predictedBhv : predictedBhvInfoList) {
//			BaseUserBhv predictedBhv = predictedBhvInfo.getUserBhv();
//			BaseUserBhv predictedBhv = predictedBhvInfo.getUserBhv();
//			if(lastPredictedBhvSet == null || !lastPredictedBhvSet.contains(predictedBhv)) {
//				predictedBhvDao.storePredictedBhv(predictedBhvInfo);
//			}
			if(AppShuttleApplication.recentPredictedBhvMap == null || !AppShuttleApplication.recentPredictedBhvMap.containsValue(predictedBhv)) {
//				Log.d("test", AppShuttleApplication.recentPredictedBhvList.toString());
				predictedBhvDao.storePredictedBhv(predictedBhv);
			}
//			currPredictedBhvSet.add(predictedBhv);
		}

//		recentPredictedBhvSet = currPredictedBhvSet;
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
