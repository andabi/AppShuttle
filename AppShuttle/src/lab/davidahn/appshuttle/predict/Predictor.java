package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.matcher.FrequentlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.InstantlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.PlaceMatcher;
import lab.davidahn.appshuttle.predict.matcher.TimeDailyMatcher;
import lab.davidahn.appshuttle.predict.matcher.TimeDailyWeekdayMatcher;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.PositionMatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.RecentMatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.TimeMatcherGroup;
import android.app.AlarmManager;
import android.content.SharedPreferences;

public class Predictor {
	private SharedPreferences preferenceSettings;

	private List<MatcherGroup> matcherGroupList;

	private static Predictor predictor = new Predictor();
	private Predictor(){
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		
		matcherGroupList = new ArrayList<MatcherGroup>();

		registerMatcherGroup();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	private void registerMatcherGroup() {
		registerFreqMatcherGroup();
		registerTimeMatcherGroup();
		registerLocationMatcherGroup();
	}
	
	private void registerFreqMatcherGroup() {
		MatcherGroup recentMatcherGroup = new RecentMatcherGroup();
		
		recentMatcherGroup.registerMatcher(new FrequentlyRecentMatcher(
				preferenceSettings.getLong("matcher.recent.frequently.duration", AlarmManager.INTERVAL_DAY),
				0.0,
				0.0,
				preferenceSettings.getInt("matcher.recent.frequently.min_num_cxt", 3),
				preferenceSettings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)
				));
		recentMatcherGroup.registerMatcher(new InstantlyRecentMatcher(
				preferenceSettings.getLong("matcher.recent.instantly.duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3 * 2),
				0.0,
				0.0,
				preferenceSettings.getInt("matcher.recent.instantly.min_num_cxt", 1),
				preferenceSettings.getLong("matcher.recent.instantly.acceptance_delay", 0)
				));

		registerMatcherGroup(recentMatcherGroup);
	}
	
	private void registerTimeMatcherGroup() {
		MatcherGroup timeMatcherGroup = new TimeMatcherGroup();
		
		timeMatcherGroup.registerMatcher(new TimeDailyMatcher(
				preferenceSettings.getLong("matcher.time.daily.duration", 5 * AlarmManager.INTERVAL_DAY),
				preferenceSettings.getFloat("matcher.time.daily.min_likelihood", 0.5f),
				preferenceSettings.getFloat("matcher.time.daily.min_inverse_entropy", 0.2f),
				preferenceSettings.getInt("matcher.time.daily.min_num_cxt", 3),
				AlarmManager.INTERVAL_DAY,
				preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6),
				preferenceSettings.getLong("matcher.time.daily.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)
				));
		timeMatcherGroup.registerMatcher(new TimeDailyWeekdayMatcher(
				preferenceSettings.getLong("matcher.time.daily.duration", 5 * AlarmManager.INTERVAL_DAY),
				preferenceSettings.getFloat("matcher.time.daily.min_likelihood", 0.5f),
				preferenceSettings.getFloat("matcher.time.daily.min_inverse_entropy", 0.2f),
				preferenceSettings.getInt("matcher.time.daily.min_num_cxt", 3),
				AlarmManager.INTERVAL_DAY,
				preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HALF_HOUR / 6),
				preferenceSettings.getLong("matcher.time.daily.acceptance_delay", AlarmManager.INTERVAL_HOUR / 2)
				));
		
		registerMatcherGroup(timeMatcherGroup);
	}
	
	private void registerLocationMatcherGroup() {
		MatcherGroup locMatcherGroup = new PositionMatcherGroup();
		
		locMatcherGroup.registerMatcher(new PlaceMatcher(
				preferenceSettings.getLong("matcher.position.place.duration", 6 * AlarmManager.INTERVAL_DAY),
				preferenceSettings.getFloat("matcher.position.place.min_likelihood", 0.7f),
				preferenceSettings.getFloat("matcher.position.place.min_inverse_entropy", 0.3f),
				preferenceSettings.getInt("matcher.position.place.min_num_cxt", 3),
				preferenceSettings.getInt("matcher.position.place.distance_tolerance", 2000)
				));
//		locMatcherGroup.registerMatcher(new LocationMatcher(
//				preferenceSettings.getLong("matcher.position.loc.duration", AlarmManager.INTERVAL_HOUR / 6),
//				preferenceSettings.getFloat("matcher.position.loc.min_likelihood", 0.5f),
//				preferenceSettings.getFloat("matcher.position.loc.min_inverse_entropy", 0.2f),
//				preferenceSettings.getInt("matcher.position.loc.min_num_cxt", 5),
//				preferenceSettings.getInt("matcher.position.loc.distance_tolerance", 50)
//				));
		
		registerMatcherGroup(locMatcherGroup);
	}
	
	private void registerMatcherGroup(MatcherGroup matcherGroup){
		matcherGroupList.add(matcherGroup);
	}
	
	public void predict(){
		SnapshotUserCxt currUserCxt = AppShuttleApplication.currUserCxt;
		
		if(currUserCxt == null)
			return ;

		Map<UserBhv, PredictionInfo> predicted = new HashMap<UserBhv, PredictionInfo>();
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
		
		if(preferenceSettings.getBoolean("predictor.store", false))
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
//, settings.getInt("matcher.recent.frequently.min_num_cxt", 3));


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
