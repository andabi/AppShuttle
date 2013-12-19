package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.view.OrdinaryUserBhv;

public class PredictedBhv extends OrdinaryUserBhv implements Comparable<PredictedBhv>{

	private EnumMap<MatcherType, Long> firstPredictedTimeByMatcherType;

	public PredictedBhv(UserBhv uBhv) {
		super(uBhv);
		firstPredictedTimeByMatcherType = new EnumMap<MatcherType, Long>(MatcherType.class);
	}
	
	public Long getFirstPredictedTime(MatcherType matcherType){
		return firstPredictedTimeByMatcherType.get(matcherType);
	}
	
	public void setFirstPredictedTime(MatcherType matcherType, long predictedTime){
		firstPredictedTimeByMatcherType.put(matcherType, predictedTime);
	}
	
	public long getMostRecentPredictedTime(){
		List<Long> predictedTimes = new ArrayList<Long>(firstPredictedTimeByMatcherType.values());
		return Collections.max(predictedTimes);
	}

	@Override
	public int compareTo(PredictedBhv _uBhv) {
		long _mostRecentPredictedTime = _uBhv.getMostRecentPredictedTime();
		long mostRecentPredictedTime = getMostRecentPredictedTime();
		
		if(mostRecentPredictedTime < _mostRecentPredictedTime)
			return 1;
		else if(mostRecentPredictedTime > _mostRecentPredictedTime)
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
