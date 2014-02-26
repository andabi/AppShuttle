package lab.davidahn.appshuttle.predict;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PredictedBhvInfo implements Comparable<PredictedBhvInfo> {
	private final UserBhv uBhv;
	private final Date timeDate;
	private final TimeZone timeZone;
	private final Map<EnvType, UserEnv> uEnvs;
	private final EnumMap<MatcherType, MatcherResultElem> matcherResults;
	private final double score;

	public PredictedBhvInfo(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnvs, UserBhv _uBhv, EnumMap<MatcherType, MatcherResultElem> _matcherResults, double _score){
		timeDate = _time;
		timeZone = _timeZone;
		uEnvs = _userEnvs;
		uBhv = _uBhv;
		matcherResults = _matcherResults;
		score = _score;
	}

	public Date getTimeDate() {
		return timeDate;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public Map<EnvType, UserEnv> getUserEnvMap() {
		return uEnvs;
	}
	public UserEnv getUserEnv(EnvType envType) {
		return uEnvs.get(envType);
	}
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public Map<MatcherType, MatcherResultElem> getMatcherResultMap() {
		return matcherResults;
	}
	
	public MatcherResultElem getMatcherResult(MatcherType matcherType) {
		return matcherResults.get(matcherType);
	}

	public double getScore() {
		return score;
	}
	
	public EnumMap<MatcherType, MatcherResultElem> getAllChildMatcherResultMap(){
		EnumMap<MatcherType, MatcherResultElem> matcherResultMap = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
		for(MatcherType matcherType : matcherResults.keySet())
			matcherResultMap.putAll(matcherResults.get(matcherType).getChildMatcherResultMap());
		return matcherResultMap;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return uBhv.hashCode();
	}
	
	@Override
	public int compareTo(PredictedBhvInfo predictedBhvInfo){
		return timeDate.compareTo(predictedBhvInfo.timeDate);
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("bhv: ").append(uBhv.toString()).append(", ");
		msg.append("matcherResults: ").append(matcherResults.toString()).append(", ");
		msg.append("score: ").append(score);
		return msg.toString();
	}
}
