package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class PredictionInfo implements Comparable<PredictionInfo> {
	private final UserBhv _uBhv;
	private final Date _timeDate;
	private final TimeZone _timeZone;
	private final Map<EnvType, UserEnv> _uEnvs;
	private final EnumMap<MatcherGroupType, MatcherGroupResult> _matcherGroupResults;
	private final double _score;

	public PredictionInfo(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnvs, UserBhv uBhv, EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupResults, double score){
		_timeDate = time;
		_timeZone = timeZone;
		_uEnvs = userEnvs;
		_uBhv = uBhv;
		_matcherGroupResults = matcherGroupResults;
		_score = score;
	}

	public Date getTime() {
		return _timeDate;
	}

	public TimeZone getTimeZone() {
		return _timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnvMap() {
		return _uEnvs;
	}

	public UserEnv getUserEnv(EnvType envType) {
		return _uEnvs.get(envType);
	}
	
	public UserBhv getUserBhv() {
		return _uBhv;
	}

	public Map<MatcherGroupType, MatcherGroupResult> getMatcherGroupResultMap() {
		return _matcherGroupResults;
	}
	
	public MatcherGroupResult getMatcherGroupResult(MatcherGroupType matcherGroupType) {
		return _matcherGroupResults.get(matcherGroupType);
	}

	public double getScore() {
		return _score;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& _uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& _uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	@Override
	public int compareTo(PredictionInfo predictedBhv){
		if(_score < predictedBhv._score) 
			return 1;
		else if(_score == predictedBhv._score) 
			return 0;
		else 
			return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matched results: ").append(_matcherGroupResults.toString()).append(", ");
		msg.append("predicted bhv: ").append(_uBhv.toString()).append(", ");
		msg.append("score: ").append(_score);
		return msg.toString();
	}

}
