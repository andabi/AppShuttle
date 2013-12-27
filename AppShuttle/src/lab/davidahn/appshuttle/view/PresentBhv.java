package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.Predictor;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhv extends NormalBhv implements Comparable<PresentBhv>{

	private EnumMap<MatcherType, Long> initialPredictedTimeByMatcherType;
	
	public PresentBhv(UserBhv uBhv) {
		super(uBhv);
		initialPredictedTimeByMatcherType = new EnumMap<MatcherType, Long>(MatcherType.class);
	}
	
	public Long getInitialPredictedTime(MatcherType matcherType){
		return initialPredictedTimeByMatcherType.get(matcherType);
	}
	
	public void setInitialPredictedTime(MatcherType matcherType, long predictedTime){
		initialPredictedTimeByMatcherType.put(matcherType, predictedTime);
	}
	
	public long getRecentPredictedTime(){
		List<Long> predictedTimes = new ArrayList<Long>(initialPredictedTimeByMatcherType.values());
		return Collections.max(predictedTimes);
	}
	
	public static Map<UserBhv, PresentBhv> extractPresentBhvs(Map<UserBhv, PredictionInfo> currPredictionInfos) {
		if (currPredictionInfos == null || currPredictionInfos.isEmpty())
			return Collections.emptyMap();

		Map<UserBhv, PresentBhv> res = new HashMap<UserBhv, PresentBhv>();
		
		for(UserBhv uBhv : currPredictionInfos.keySet()){
			PredictionInfo predictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv recentPresentBhv = AppShuttleApplication.recentPresentBhvs.get(uBhv);
			if(recentPresentBhv == null){
				PresentBhv presentBhv = new PresentBhv(uBhv);
				for(MatcherType matcherType : predictionInfo.getMatcherResultMap().keySet())
					presentBhv.setInitialPredictedTime(matcherType, predictionInfo.getTimeDate().getTime());
				res.put(uBhv, presentBhv);
			} else {
				for(MatcherType matcherType : predictionInfo.getMatcherResultMap().keySet()){
					Long initialPredictedTime = recentPresentBhv.getInitialPredictedTime(matcherType);
					if(initialPredictedTime == null)
						recentPresentBhv.setInitialPredictedTime(matcherType, predictionInfo.getTimeDate().getTime());
//					else {
//						if(matcherType.isOverwritableForNewPrediction)
//							recentPresentBhv.setInitialPredictedTime(matcherType, predictionInfo.getTimeDate().getTime());
//					}
				}
				res.put(uBhv, recentPresentBhv);
			}
		}
		
		return res;
	}

	@Override
	public int compareTo(PresentBhv _uBhv) {
		long _recentPredictedTime = _uBhv.getRecentPredictedTime();
		long recentPredictedTime = getRecentPredictedTime();
		
		if(recentPredictedTime < _recentPredictedTime)
			return 1;
		else if(recentPredictedTime > _recentPredictedTime)
			return -1;
		else {
			Predictor predictor = Predictor.getInstance();
			double score = predictor.getCurrentPredictionInfo(uBhv).getScore();
			double _score = predictor.getCurrentPredictionInfo(_uBhv).getScore();
		
			if(score < _score)
				return 1;
			else if(score == _score)
				return 0;
			else 
				return -1;
		}
	}
}