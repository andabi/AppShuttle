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
import android.app.AlarmManager;
import android.content.SharedPreferences;

public class Predictor {
	private SharedPreferences _preferenceSettings;
	
	private static Predictor predictor = new Predictor();
	private Predictor(){
//		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		_preferenceSettings = AppShuttleApplication.getContext().getPreferences();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	public void predict(){
		SnapshotUserCxt currUserCxt = AppShuttleApplication.currUserCxt;

		if(currUserCxt == null)
			return ;

		Map<UserBhv, PredictionInfo> predicted = new HashMap<UserBhv, PredictionInfo>();

		MatcherGroup freqMatcherGroup = new FreqMatcherGroup(MatcherGroupType.FREQUENCY, MatcherGroupType.FREQUENCY.priority);
		MatcherGroup timeMatcherGroup = new TimeMatcherGroup(MatcherGroupType.TIME, MatcherGroupType.TIME.priority);
		MatcherGroup locMatcherGroup = new LocMatcherGroup(MatcherGroupType.LOCATION, MatcherGroupType.LOCATION.priority);
		
		freqMatcherGroup.addMatcher(new FreqContextMatcher(
				currUserCxt.getTimeDate()
				, _preferenceSettings.getLong("matcher.freq.duration", AlarmManager.INTERVAL_DAY)
				, 0.0
				, 0.0
				, _preferenceSettings.getInt("matcher.freq.min_num_cxt", 3)
				, _preferenceSettings.getLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)
				));
		timeMatcherGroup.addMatcher(new TimeDailyContextMatcher(
				new Date(currUserCxt.getTimeDate().getTime() - _preferenceSettings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6))
				, _preferenceSettings.getLong("matcher.weak_time.duration", 5 * AlarmManager.INTERVAL_DAY)
				, _preferenceSettings.getFloat("matcher.weak_time.min_likelihood", 0.5f)
				, _preferenceSettings.getFloat("matcher.weak_time.min_inverse_entropy", 0.2f)
				, _preferenceSettings.getInt("matcher.weak_time.min_num_cxt", 3)
				, AlarmManager.INTERVAL_DAY
				, _preferenceSettings.getLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6)
				, _preferenceSettings.getLong("matcher.weak_time.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)
				));
		locMatcherGroup.addMatcher(new PlaceContextMatcher(
				currUserCxt.getTimeDate()
				, _preferenceSettings.getLong("matcher.place.duration", 6 * AlarmManager.INTERVAL_DAY)
				, _preferenceSettings.getFloat("matcher.place.min_likelihood", 0.7f)
				, _preferenceSettings.getFloat("matcher.place.min_inverse_entropy", 0.3f)
				, _preferenceSettings.getInt("matcher.place.min_num_cxt", 3)
				, _preferenceSettings.getInt("matcher.place.distance_tolerance", 2000)
				));
//			cxtMatcherList.add(new LocContextMatcher(
//					currUserCxt.getTimeDate()
//					, _preferenceSettings.getLong("matcher.loc.duration", AlarmManager.INTERVAL_HOUR / 6)
//					, _preferenceSettings.getFloat("matcher.loc.min_likelihood", 0.5f)
//					, _preferenceSettings.getFloat("matcher.loc.min_inverse_entropy", 0.2f)
//					, _preferenceSettings.getInt("matcher.loc.min_num_cxt", 5)
//					, _preferenceSettings.getInt("matcher.loc.distance_tolerance", 50)
//					));
		
		List<MatcherGroup> matcherGroupList = new ArrayList<MatcherGroup>();
		matcherGroupList.add(freqMatcherGroup);
		matcherGroupList.add(timeMatcherGroup);
		matcherGroupList.add(locMatcherGroup);
		
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
//		long noiseTimeTolerance = _preferenceSettings.getLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		for(UserBhv uBhv : userBhvManager.getTotalBhvSet()){
			EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupMap = new EnumMap<MatcherGroupType, MatcherGroupResult>(MatcherGroupType.class);
			for(MatcherGroup matcherGroup : matcherGroupList){
				MatcherGroupResult matcherGroupResult = matcherGroup.matchAndGetResult(uBhv, currUserCxt/*, noiseTimeTolerance*/);
				if(matcherGroupResult == null)
					continue;
				matcherGroupMap.put(matcherGroup.getMatcherGroupType(), matcherGroupResult);
			}

			if(matcherGroupMap.isEmpty()) {
				continue;
			}
			
			double score = computePredictionScore(matcherGroupMap);
			PredictionInfo predictedBhv = new PredictionInfo(currUserCxt.getTimeDate(), 
					currUserCxt.getTimeZone(), 
					currUserCxt.getUserEnvs(), 
					uBhv, matcherGroupMap, score);
			predicted.put(uBhv, predictedBhv);
		}
		
		if(_preferenceSettings.getBoolean("predictor.store", false))
			storeNewPredictedBhv(predicted);
		
		AppShuttleApplication.recentPredictionInfoMap = predicted;
//		AppShuttleApplication.getContext().setRecentPredictedBhvInfoList(predictedBhvInfoList);
	}
	
	public Map<UserBhv, PredictionInfo> getRecentPredictionInfoMap(){
		return AppShuttleApplication.recentPredictionInfoMap;
	}
	
//	public List<PredictionInfo> getRecentPredictedBhv(int topN){
//		Map<UserBhv, PredictionInfo> predictedBhvMap = AppShuttleApplication.recentPredictedBhvMap;
//		
//		if(predictedBhvMap == null)
//			return null;
//		
//		Log.d("test", predictedBhvMap.keySet().toString());
//		
//		List<PredictionInfo> predictedBhvList = new ArrayList<PredictionInfo>(predictedBhvMap.values());
//		Collections.sort(predictedBhvList);
//		
//		return predictedBhvList.subList(0, Math.min(predictedBhvList.size(), topN));
//	}
//	
	public PredictionInfo getPredictionInfo(UserBhv uBhv){
		Map<UserBhv, PredictionInfo> predictedBhvMap = AppShuttleApplication.recentPredictionInfoMap;
		
		if(predictedBhvMap == null)
			return null;
		
		return predictedBhvMap.get(uBhv);
	}
	
	private double computePredictionScore(EnumMap<MatcherGroupType, MatcherGroupResult> matchedResults){
		double PredictionScore = 0;
		
		for(MatcherGroupType matcherType : matchedResults.keySet()){
			double weight = Math.pow(10, matcherType.priority);
			double score = weight * matchedResults.get(matcherType).getScore();
			PredictionScore += score;
		}
		return PredictionScore;
	}
	

	public void storeNewPredictedBhv(Map<UserBhv, PredictionInfo> predicted) {
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance();

		for(UserBhv uBhv : predicted.keySet()) {
			if(AppShuttleApplication.recentPredictionInfoMap == null || !AppShuttleApplication.recentPredictionInfoMap.containsKey(uBhv)) {
				predictedBhvDao.storePredictedBhv(predicted.get(uBhv));
			}
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
