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
import lab.davidahn.appshuttle.predict.matcher.DailyTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.DailyWeekdayTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.DailyWeekendTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.FrequentlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.InstantlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.MovePositionMatcher;
import lab.davidahn.appshuttle.predict.matcher.PlacePositionMatcher;
import lab.davidahn.appshuttle.predict.matcher.conf.PositionMatcherConf;
import lab.davidahn.appshuttle.predict.matcher.conf.RecentMatcherConf;
import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.PositionMatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.RecentMatcherGroup;
import lab.davidahn.appshuttle.predict.matchergroup.TimeMatcherGroup;
import android.app.AlarmManager;
import android.content.SharedPreferences;

public class Predictor {
	private List<MatcherGroup> matcherGroupList;

	private static Predictor predictor = new Predictor();
	private Predictor(){
		matcherGroupList = new ArrayList<MatcherGroup>();
		registerMatcherGroup();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	private void registerMatcherGroup() {
		registerRecentMatcherGroup();
		registerTimeMatcherGroup();
		registerPositionMatcherGroup();
	}
	
	private void registerRecentMatcherGroup() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup recentMatcherGroup = new RecentMatcherGroup();
		
		recentMatcherGroup.registerMatcher(new FrequentlyRecentMatcher(
				new RecentMatcherConf.Builder()
					.setDuration(preferenceSettings.getLong("matcher.recent.frequently.duration", AlarmManager.INTERVAL_DAY))
					.setMinLikelihood(0.0)
					.setMinInverseEntropy(0.0)
					.setMinNumHistory(preferenceSettings.getInt("matcher.recent.frequently.min_num_history", 3))
					.setAcceptanceDelay(preferenceSettings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6))
					.build()
				));
		recentMatcherGroup.registerMatcher(new InstantlyRecentMatcher(
			new RecentMatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.recent.instantly.duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3 * 2))
				.setMinLikelihood(0.0)
				.setMinInverseEntropy(0.0)
				.setMinNumHistory(preferenceSettings.getInt("matcher.recent.instantly.min_num_history", 1))
				.setAcceptanceDelay(preferenceSettings.getLong("matcher.recent.instantly.acceptance_delay", 0))
				.build()
			));


		registerMatcherGroup(recentMatcherGroup);
	}
	
	private void registerTimeMatcherGroup() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup timeMatcherGroup = new TimeMatcherGroup();
		
		timeMatcherGroup.registerMatcher(new DailyTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HOUR))
			.setDuration(preferenceSettings.getLong("matcher.time.daily.duration", 5 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily.min_num_history", 3))
			.build()
		));

		timeMatcherGroup.registerMatcher(new DailyWeekdayTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily_weekday.tolerance", AlarmManager.INTERVAL_HOUR))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekday.duration", 7 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily_weekday.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily_weekday.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily_weekday.min_num_history", 3))
			.build()
		));
		
		timeMatcherGroup.registerMatcher(new DailyWeekendTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily_weekend.tolerance", 2 * AlarmManager.INTERVAL_HOUR))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekend.duration", 21 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily_weekend.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily_weekend.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily_weekend.min_num_history", 2))
			.build()
		));
	
		registerMatcherGroup(timeMatcherGroup);
//		long noiseTimeTolerance = _preferenceSettings.getLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		/*, noiseTimeTolerance*/
	}
	
	private void registerPositionMatcherGroup() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup locMatcherGroup = new PositionMatcherGroup();
		
		locMatcherGroup.registerMatcher(new PlacePositionMatcher(
			new PositionMatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.position.place.duration", 14 * AlarmManager.INTERVAL_DAY))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.place.min_likelihood", 0.7f))
				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.place.min_inverse_entropy", Float.MIN_VALUE))
				.setMinNumHistory(preferenceSettings.getInt("matcher.position.place.min_num_history", 3))
				.setToleranceInMeter(preferenceSettings.getInt("matcher.position.place.tolerance_in_meter", 2000))
				.build()
			)
		);
		locMatcherGroup.registerMatcher(new MovePositionMatcher(
				new PositionMatcherConf.Builder()
					.setDuration(preferenceSettings.getLong("matcher.position.move.duration", 14 * AlarmManager.INTERVAL_DAY))
					.setMinLikelihood(preferenceSettings.getFloat("matcher.position.move.min_likelihood", 0.3f))
					.setMinInverseEntropy(Float.MIN_VALUE)
					.setMinNumHistory(preferenceSettings.getInt("matcher.position.move.min_num_history", 3))
					.setToleranceInMeter(0)
					.build()
				)
			);
		
//		locMatcherGroup.registerMatcher(new LocationMatcher(
//			new PositionMatcherConf.Builder()
//				.setDuration(preferenceSettings.getLong("matcher.position.loc.duration", AlarmManager.INTERVAL_HOUR / 6))
//				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.loc.min_likelihood", 0.5f))
//				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.loc.min_inverse_entropy", 0.2f))
//				.setMinNumHistory(preferenceSettings.getInt("matcher.position.loc.min_num_history", 5))
//				.setToleranceInMeter(preferenceSettings.getInt("matcher.position.loc.tolerance_in_meter", 50))
//				.build()
//			)
//		);
		
		registerMatcherGroup(locMatcherGroup);
	}
	
	private void registerMatcherGroup(MatcherGroup matcherGroup){
		matcherGroupList.add(matcherGroup);
	}
	
	public void predict(SnapshotUserCxt currUserCxt){
		if(currUserCxt == null)
			return ;

		Map<UserBhv, PredictionInfo> predicted = new HashMap<UserBhv, PredictionInfo>();
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(UserBhv uBhv : userBhvManager.getTotalBhvSet()){
			EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupMap = new EnumMap<MatcherGroupType, MatcherGroupResult>(MatcherGroupType.class);
			for(MatcherGroup matcherGroup : matcherGroupList){
				MatcherGroupResult matcherGroupResult = matcherGroup.matchAndGetResult(uBhv, currUserCxt);
				if(matcherGroupResult == null)
					continue;
				matcherGroupMap.put(matcherGroup.getMatcherGroupType(), matcherGroupResult);
			}

			if(matcherGroupMap.isEmpty()) {
				continue;
			}
			
			PredictionInfo predictionInfo = new PredictionInfo(currUserCxt.getTimeDate(), 
					currUserCxt.getTimeZone(), 
					currUserCxt.getUserEnvs(), 
					uBhv, matcherGroupMap, computePredictionScore(matcherGroupMap));
			
			predicted.put(uBhv, predictionInfo);
		}
		
		storeNewPredictionInfo(predicted);
		
		AppShuttleApplication.recentPredictionInfoMap = predicted;
	}
	
	public Map<UserBhv, PredictionInfo> getRecentPredictionInfoMap(){
		return AppShuttleApplication.recentPredictionInfoMap;
	}

	public PredictionInfo getPredictionInfo(UserBhv uBhv){
		Map<UserBhv, PredictionInfo> predictionInfoMap = AppShuttleApplication.recentPredictionInfoMap;
		
		if(predictionInfoMap == null)
			return null;
		
		return predictionInfoMap.get(uBhv);
	}
	
	private double computePredictionScore(EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupResults){
		double PredictionScore = 0;
		
		for(MatcherGroupType matcherGroupType : matcherGroupResults.keySet()){
			double weight = Math.pow(10, matcherGroupType.priority);
			double score = weight * matcherGroupResults.get(matcherGroupType).getScore();
			PredictionScore += score;
		}
		return PredictionScore;
	}
	

	public void storeNewPredictionInfo(Map<UserBhv, PredictionInfo> predictionInfoMap) {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		
		if(!preferenceSettings.getBoolean("predictor.store", false))
			return;
		
		PredictionInfoDao predictionInfoDao = PredictionInfoDao.getInstance();
		for(UserBhv uBhv : predictionInfoMap.keySet()) {
			if(AppShuttleApplication.recentPredictionInfoMap == null || !AppShuttleApplication.recentPredictionInfoMap.containsKey(uBhv)) {
				predictionInfoDao.storePredictionInfo(predictionInfoMap.get(uBhv));
			}
		}
	}
}

//ContextMatcher timeCxtMatcher = new TimeContextMatcher(cxt
//, settings.getFloat("matcher.time.min_likelihood", 0.7f)
//, settings.getInt("matcher.time.min_num_history", 3)
//, AlarmManager.INTERVAL_DAY
//, settings.getLong("matcher.time.tolerance", AlarmManager.INTERVAL_HOUR / 6));
//ContextMatcher locCxtMatcher = new LocContextMatcher(cxt
//, settings.getFloat("matcher.loc.min_likelihood", 0.7f)
//, settings.getInt("matcher.loc.min_num_history", 3)
//, settings.getInt("matcher.loc.tolerance_in_meter", 2000));
//ContextMatcher FreqCxtMatcher = new FreqContextMatcher(cxt
//, Double.MIN_VALUE
//, settings.getInt("matcher.recent.frequently.min_num_history", 3));


//List<MatchedCxt> locMatchhedCxtList;
//if(GlobalState.recentLocMatchedCxtList == null) 
//	GlobalState.recentLocMatchedCxtList = new ArrayList<MatchedCxt>();
//if(GlobalState.moved == true){
//	ContextMatcher locCxtMatcher = new LocContextMatcher(cxt, 0, settings.getInt("location.tolerance_in_meter", 2000));
//	locMatchedCxtList = locCxtMatcher.matchAndGetResult(GlobalState.currentUEnv);
//	GlobalState.recentLocMatchedCxtList = locMatchedCxtList;
//	Log.i("location", "moved");
//} else {
//	locMatchedCxtList = GlobalState.recentLocMatchedCxtList;
//}

//Collections.sort(res);
//res = res.subList(0, Math.min(res.size(), topN));
