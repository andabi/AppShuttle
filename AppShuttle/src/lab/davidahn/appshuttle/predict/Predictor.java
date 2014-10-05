package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.predict.matcher.headset.HeadsetMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.LocationMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.LocationTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.MoveMatcher;
import lab.davidahn.appshuttle.predict.matcher.position.PositionMatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.recent.FrequentlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.recent.InstantlyRecentMatcher;
import lab.davidahn.appshuttle.predict.matcher.recent.RecentMatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.time.DailyTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.DailyWeekdayTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.DailyWeekendTimeMatcher;
import lab.davidahn.appshuttle.predict.matcher.time.TimeMatcherGroup;
import android.app.AlarmManager;
import android.content.SharedPreferences;

public class Predictor {
	private List<MatcherElem> matcherList;
	private long maxDuration;
	
	private static Predictor predictor = new Predictor();
	private Predictor(){
		matcherList = new ArrayList<MatcherElem>();
		maxDuration = AppShuttleApplication.getContext().getPreferences().getLong("predictor.max_duration", 21 * AlarmManager.INTERVAL_DAY);
		registerMatcher();
	}
	public static Predictor getInstance() {
		return predictor;
	}
	
	private void registerMatcher() {
		registerRecentMatcher();
		registerTimeMatcher();
		registerPositionMatcher();
		registerHeadsetMatcher();
	}
	
	private void registerRecentMatcher() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherGroup recentMatcherGroup = new RecentMatcherGroup();
		
		recentMatcherGroup.registerMatcher(new FrequentlyRecentMatcher(
			new MatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.recent.frequently.duration", AlarmManager.INTERVAL_DAY))
				.setAcceptanceDelay(preferenceSettings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6))
				.setMinLikelihood(0.0)
				.setMinInverseEntropy(0.0)
				.setMinNumHistory(preferenceSettings.getInt("matcher.recent.frequently.min_num_related_history", 3))
				.build()
			));
		recentMatcherGroup.registerMatcher(new InstantlyRecentMatcher(
			new MatcherConf.Builder()
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
			new MatcherConf.Builder()
			.setTimePeriod(AlarmManager.INTERVAL_DAY)
			.setTimeTolerance(preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setDuration(preferenceSettings.getLong("matcher.time.daily.duration", 5 * AlarmManager.INTERVAL_DAY))
			.setAcceptanceDelay(2 * preferenceSettings.getLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily.min_num_related_history", 3))
			.build()
		));

		timeMatcherGroup.registerMatcher(new DailyWeekdayTimeMatcher(
			new MatcherConf.Builder()
			.setTimePeriod(AlarmManager.INTERVAL_DAY)
			.setTimeTolerance(preferenceSettings.getLong("matcher.time.daily_weekday.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekday.duration", 7 * AlarmManager.INTERVAL_DAY))
			.setAcceptanceDelay(2 * preferenceSettings.getLong("matcher.time.daily_weekday.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
			.setMinLikelihood(preferenceSettings.getFloat("matcher.time.daily_weekday.min_likelihood", 0.5f))
			.setMinInverseEntropy(preferenceSettings.getFloat("matcher.time.daily_weekday.min_inverse_entropy", 0.2f))
			.setMinNumHistory(preferenceSettings.getInt("matcher.time.daily_weekday.min_num_related_history", 3))
			.build()
		));
		
		timeMatcherGroup.registerMatcher(new DailyWeekendTimeMatcher(
			new MatcherConf.Builder()
			.setTimePeriod(AlarmManager.INTERVAL_DAY)
			.setTimeTolerance(preferenceSettings.getLong("matcher.time.daily_weekend.tolerance", 2 * AlarmManager.INTERVAL_HOUR))
			.setDuration(preferenceSettings.getLong("matcher.time.daily_weekend.duration", 21 * AlarmManager.INTERVAL_DAY))
			.setAcceptanceDelay(2 * preferenceSettings.getLong("matcher.time.daily_weekend.tolerance", AlarmManager.INTERVAL_HALF_HOUR * 3))
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

		locMatcherGroup.registerMatcher(new MoveMatcher(
			new MatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.position.move.duration", 7 * AlarmManager.INTERVAL_DAY))
				.setAcceptanceDelay(preferenceSettings.getLong("matcher.position.move.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.move.min_likelihood", 0.3f))
				.setMinInverseEntropy(Float.MIN_VALUE)
				.setMinNumHistory(preferenceSettings.getInt("matcher.position.move.min_num_related_history", 3))
				.setPositionToleranceInMeter(0)
				.build()
			)
		);
		locMatcherGroup.registerMatcher(new LocationMatcher(
			new MatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.position.loc.duration", 3 * AlarmManager.INTERVAL_DAY))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.loc.min_likelihood", 0.5f))
				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.loc.min_inverse_entropy", 0.3f))
				.setMinNumHistory(preferenceSettings.getInt("matcher.position.loc.min_num_history", 3))
				.setPositionToleranceInMeter(preferenceSettings.getInt("matcher.position.loc.tolerance", 50))
				.build()
			)
		);
		locMatcherGroup.registerMatcher(new LocationTimeMatcher(
			new MatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.position.loc_time.duration", 3 * AlarmManager.INTERVAL_DAY))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.position.loc_time.min_likelihood", 0.5f))
				.setMinInverseEntropy(preferenceSettings.getFloat("matcher.position.loc_time.min_inverse_entropy", 0.3f))
				.setMinNumHistory(preferenceSettings.getInt("matcher.position.loc_time.min_num_history", 3))
				.setPositionToleranceInMeter(preferenceSettings.getInt("matcher.position.loc_time.tolerance_time_in_meter", 50))
				.setTimeTolerance(preferenceSettings.getLong("matcher.position.loc_time.tolerance_time", AlarmManager.INTERVAL_HALF_HOUR * 3))
				.setAcceptanceDelay(2 * preferenceSettings.getLong("matcher.position.loc_time.tolerance_time", AlarmManager.INTERVAL_HALF_HOUR * 3))
				.build()
			)
		);
//		locMatcherGroup.registerMatcher(new PlacePositionMatcher(
//			new MatcherConf.Builder()
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
	
	private void registerHeadsetMatcher() {
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		MatcherElem headsetMatcherGroup = new HeadsetMatcher(
			new MatcherConf.Builder()
				.setDuration(preferenceSettings.getLong("matcher.headset.duration", 7 * AlarmManager.INTERVAL_DAY))
				.setAcceptanceDelay(preferenceSettings.getLong("matcher.headset.acceptance_delay", AlarmManager.INTERVAL_HOUR))
				.setMinLikelihood(preferenceSettings.getFloat("matcher.headset.min_likelihood", 0.5f))
				.setMinNumHistory(preferenceSettings.getInt("matcher.headset.min_num_related_history", 2))
				.build()
			);
		matcherList.add(headsetMatcherGroup);
	}
	
	public void predict(SnapshotUserCxt currUserCxt){
		if(currUserCxt.getUserEnvs().isEmpty())
			return ;
		
		List<PredictedBhv> predictedBhvList = new ArrayList<PredictedBhv>();
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		long toTime = currUserCxt.getTime();
		long fromTime = toTime - maxDuration;
		for(UserBhv uBhv : userBhvManager.getRegisteredBhvSet()){
			List<DurationUserBhv> history = getInvolvedDurationUserBhv(uBhv, fromTime, toTime);

			EnumMap<MatcherType, MatcherResultElem> matcherMap = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
			for(MatcherElem matcher : matcherList){
				MatcherResultElem matcherResult = matcher.matchAndGetResult(uBhv, currUserCxt, history);
				if(matcherResult == null)
					continue;
				matcherMap.put(matcher.getType(), matcherResult);
			}

			if(matcherMap.isEmpty())
				continue;
			
			PredictedBhv predictedBhv = new PredictedBhv(currUserCxt.getTime(), 
					currUserCxt.getTimeZone(), 
					currUserCxt.getUserEnvs(), 
					uBhv, matcherMap, computePredictionScore(matcherMap));
			
			predictedBhvList.add(predictedBhv);
		}
		PredictedBhv.updatePredictedBhvList(predictedBhvList);
	}
	
	private List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, long fromTime, long toTime) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(
				fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for (DurationUserBhv durationUserBhv : durationUserBhvList) {
			// if(durationUserBhv.getEndTime().getTime() -
			// durationUserBhv.getTime().getTime() < noiseTimeTolerance)
			// continue;
			pureDurationUserBhvList.add(durationUserBhv);
		}
		return pureDurationUserBhvList;
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
