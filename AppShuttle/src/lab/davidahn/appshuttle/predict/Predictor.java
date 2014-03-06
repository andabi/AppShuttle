package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.matcher.MatcherElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.predict.matcher.position.LocationPositionMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.MovePositionMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.PositionMatcherConf;
import lab.davidahn.appshuttle.predict.matcher.position.PositionMatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.recent.FrequentlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.recent.InstantlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.recent.RecentMatcherConf;
import lab.davidahn.appshuttle.predict.matcher.recent.RecentMatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.time.DailyTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.DailyWeekdayTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.DailyWeekendTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.TimeMatcherConf;
import lab.davidahn.appshuttle.predict.matcher.time.TimeMatcherGroup;
import android.app.AlarmManager;
import android.content.SharedPreferences;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class Predictor {
	private List<MatcherElem> matcherList;
	
	private static Predictor predictor = new Predictor();
	private Predictor(){
		matcherList = new ArrayList<MatcherElem>();
		registerMatcher();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	private void registerMatcher() {
		registerRecentMatcher();
		registerTimeMatcher();
		registerPositionMatcher();
	}
	
	private void registerRecentMatcher() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup recentMatcherGroup = new RecentMatcherGroup();
		
		recentMatcherGroup.registerMatcher(new FrequentlyRecentMatcher(
				new RecentMatcherConf.Builder()
					.setDuration(preferenceSettings.getLong("matcher.recent.frequently.duration", AlarmManager.INTERVAL_DAY))
					.setAcceptanceDelay(preferenceSettings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6))
					.setMinLikelihood(0.0)
					.setMinInverseEntropy(0.0)
					.setMinNumHistory(preferenceSettings.getInt("matcher.recent.frequently.min_num_related_history", 3))
					.build()
				));
		recentMatcherGroup.registerMatcher(new InstantlyRecentMatcher(
			new RecentMatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.recent.instantly.duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3 * 2))
				.setAcceptanceDelay(preferenceSettings.getLong("matcher.recent.instantly.acceptance_delay", 0))
				.setMinLikelihood(0.0)
				.setMinInverseEntropy(0.0)
				.setMinNumHistory(preferenceSettings.getInt("matcher.recent.instantly.min_num_related_history", 1))
				.build()
			));

		matcherList.add(recentMatcherGroup);
	}
	
	private void registerTimeMatcher() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup timeMatcherGroup = new TimeMatcherGroup();
		
		timeMatcherGroup.registerMatcher(new DailyTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setDuration(preferenceSettings.getLong("matcher.time.daily.duration", 5 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily.min_num_related_history", 3))
			.build()
		));

		timeMatcherGroup.registerMatcher(new DailyWeekdayTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily_weekday.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekday.duration", 7 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily_weekday.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily_weekday.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily_weekday.min_num_related_history", 3))
			.build()
		));
		
		timeMatcherGroup.registerMatcher(new DailyWeekendTimeMatcher(
			new TimeMatcherConf.Builder()
			.setPeriod(AlarmManager.INTERVAL_DAY)
			.setTolerance(preferenceSettings.getLong("matcher.time.daily_weekend.tolerance", 2 * AlarmManager.INTERVAL_HOUR))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekend.duration", 21 * AlarmManager.INTERVAL_DAY))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily_weekend.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily_weekend.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily_weekend.min_num_related_history", 3))
			.build()
		));
	
		matcherList.add(timeMatcherGroup);
//		long noiseTimeTolerance = _preferenceSettings.getLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		/*, noiseTimeTolerance*/
	}
	
	private void registerPositionMatcher() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup locMatcherGroup = new PositionMatcherGroup();

		locMatcherGroup.registerMatcher(new MovePositionMatcher(
				new PositionMatcherConf.Builder()
					.setDuration(preferenceSettings.getLong("matcher.position.move.duration", 7 * AlarmManager.INTERVAL_DAY))
					.setAcceptanceDelay(preferenceSettings.getLong("matcher.position.move.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3))
					.setMinLikelihood(preferenceSettings.getFloat("matcher.position.move.min_likelihood", 0.3f))
					.setMinInverseEntropy(Float.MIN_VALUE)
					.setMinNumHistory(preferenceSettings.getInt("matcher.position.move.min_num_related_history", 3))
					.setToleranceInMeter(0)
					.build()
				)
			);
		locMatcherGroup.registerMatcher(new LocationPositionMatcher(
			new PositionMatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.position.loc.duration", 3 * AlarmManager.INTERVAL_DAY))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.loc.min_likelihood", 0.5f))
				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.loc.min_inverse_entropy", 0.3f))
				.setMinNumHistory(preferenceSettings.getInt("matcher.position.loc.min_num_history", 3))
				.setToleranceInMeter(preferenceSettings.getInt("matcher.position.loc.tolerance_in_meter", 50))
				.build()
			)
		);
//		locMatcherGroup.registerMatcher(new PlacePositionMatcher(
//			new PositionMatcherConf.Builder()
//				.setDuration(preferenceSettings.getLong("matcher.position.place.duration", 7 * AlarmManager.INTERVAL_DAY))
//				.setAcceptanceDelay(preferenceSettings.getLong("matcher.position.place.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3))
//				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.place.min_likelihood", 0.7f))
//				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.place.min_inverse_entropy", Float.MIN_VALUE))
//				.setMinNumHistory(preferenceSettings.getInt("matcher.position.place.min_num_history", 3))
//				.setToleranceInMeter(0)
//				.build()
//			)
//		);
		matcherList.add(locMatcherGroup);
	}
	
	public void predict(SnapshotUserCxt currUserCxt){
		if(currUserCxt == null)
			return ;
		
		Date time_1 = new Date();	// Start time
				
		List<PredictedBhv> predictedBhvList = new ArrayList<PredictedBhv>();
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(UserBhv uBhv : userBhvManager.getRegisteredBhvSet()){
			EnumMap<MatcherType, MatcherResultElem> matcherMap = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
			for(MatcherElem matcher : matcherList){
				MatcherResultElem matcherResult = matcher.matchAndGetResult(uBhv, currUserCxt);
				if(matcherResult == null)
					continue;
				matcherMap.put(matcher.getType(), matcherResult);
			}

			if(matcherMap.isEmpty())
				continue;
			
			PredictedBhv predictedBhv = new PredictedBhv(currUserCxt.getTimeDate(), 
					currUserCxt.getTimeZone(), 
					currUserCxt.getUserEnvs(), 
					uBhv, matcherMap, computePredictionScore(matcherMap));
			
			predictedBhvList.add(predictedBhv);
		}
		PredictedBhv.updatePredictedBhvList(predictedBhvList);
		
		
		// Statistics
		// Report (end time - start time)
		Tracker easyTracker = EasyTracker.getInstance(AppShuttleApplication.getContext());
		easyTracker.send(MapBuilder
				.createTiming("algorithm",    // Timing category (required)
							(new Date()).getTime() - time_1.getTime(),       // Timing interval in milliseconds (required)
							"overall_prediction_cost",  // Timing name
							null)           // Timing label
				.build()
			);
	}
	private double computePredictionScore(EnumMap<MatcherType, MatcherResultElem> matcherResults){
		double PredictionScore = 0;
		
		for(MatcherType matcher : matcherResults.keySet()){
			double weight = Math.pow(10, matcher.priority);
			double score = weight * matcherResults.get(matcher).getScore();
			PredictionScore += score;
		}
		return PredictionScore;
	}
}
