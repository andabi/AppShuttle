package lab.davidahn.appshuttle.predict;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.ViewableUserBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhv extends ViewableUserBhv implements Comparable<PresentBhv> {
	private EnumMap<MatcherType, PredictedBhvInfo> firstPredictionInfoByMatcherType;

	public PresentBhv(UserBhv uBhv) {
		super(uBhv);
		firstPredictionInfoByMatcherType = new EnumMap<MatcherType, PredictedBhvInfo>(
				MatcherType.class);
	}

	public EnumMap<MatcherType, PredictedBhvInfo> getFirstPredictionInfo() {
		return firstPredictionInfoByMatcherType;
	}

	public void setFirstPredictionInfo(EnumMap<MatcherType, PredictedBhvInfo> _firstPredictionInfoByMatcherType) {
		firstPredictionInfoByMatcherType = _firstPredictionInfoByMatcherType;
	}

	public PredictedBhvInfo getFirstPredictionInfoByMatcherType(
			MatcherType matcherType) {
		return firstPredictionInfoByMatcherType.get(matcherType);
	}

	public void setFirstPredictionInfoByMatcherType(MatcherType matcherType,
			PredictedBhvInfo info) {
		firstPredictionInfoByMatcherType.put(matcherType, info);
	}

	public PredictedBhvInfo getRecentOfFirstPredictionInfo() {
		if(firstPredictionInfoByMatcherType.isEmpty())
			return null;
			
		return Collections.max(firstPredictionInfoByMatcherType.values());
	}
	
	public boolean isAlive(){
		return true;
	}

	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_present_container;
	}

	@Override
	public int compareTo(PresentBhv _uBhv) {
		if (isAlive() && !_uBhv.isAlive())	return 1;
		else if(!isAlive() && _uBhv.isAlive())	return -1;
		
		Date recentFirstPredictionInfoTime = getRecentOfFirstPredictionInfo().getTimeDate();
		Date _recentFirstPredictionInfoTime = _uBhv.getRecentOfFirstPredictionInfo()
				.getTimeDate();
		int comp = recentFirstPredictionInfoTime.compareTo(_recentFirstPredictionInfoTime);
		if (comp != 0) return comp;
		else {
			double score = getRecentOfFirstPredictionInfo().getScore();
			double _score = _uBhv.getRecentOfFirstPredictionInfo().getScore();
			if (score > _score)	return 1;
			else if (score == _score) return 0;
			else return -1;
		}
	}

	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer();
		msg.append("firstPredictionInfoByMatcherType: ").append(firstPredictionInfoByMatcherType.toString());
		return msg.toString();
	}
}