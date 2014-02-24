package lab.davidahn.appshuttle.predict;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.ViewableUserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhv extends ViewableUserBhv implements Comparable<PresentBhv> {

	private EnumMap<MatcherType, PredictedBhvInfo> startPredictionInfoByMatcherType;

	public PresentBhv(UserBhv uBhv) {
		super(uBhv);
		startPredictionInfoByMatcherType = new EnumMap<MatcherType, PredictedBhvInfo>(
				MatcherType.class);
	}

	public PredictedBhvInfo getRecentPredictionInfo() {
		return Collections.max(startPredictionInfoByMatcherType.values());
	}

	public PredictedBhvInfo getStartPredictionInfoByMatcherType(
			MatcherType matcherType) {
		return startPredictionInfoByMatcherType.get(matcherType);
	}

	public void setStartPredictionInfoByMatcherType(MatcherType matcherType,
			PredictedBhvInfo info) {
		startPredictionInfoByMatcherType.put(matcherType, info);
	}

	@Override
	public int compareTo(PresentBhv _uBhv) {
		Date recentPredictionTime = getRecentPredictionInfo().getTimeDate();
		Date _recentPredictionTime = _uBhv.getRecentPredictionInfo()
				.getTimeDate();

		int comp = recentPredictionTime.compareTo(_recentPredictionTime);
		if (comp != 0)
			return comp;
		else {
			double score = getRecentPredictionInfo().getScore();
			double _score = _uBhv.getRecentPredictionInfo().getScore();
			if (score > _score)
				return 1;
			else if (score == _score)
				return 0;
			else
				return -1;
		}
	}
	
	@Override
	public String toString(){
		return startPredictionInfoByMatcherType.toString();
	}
}