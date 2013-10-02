package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class PredictedBhv implements Comparable<PredictedBhv> {
	private final Date _timeDate;
	private final TimeZone _timeZone;
	private final Map<EnvType, UserEnv> _uEnvs;
	private final UserBhv _uBhv;
	private final EnumMap<MatcherType, MatchedResult> _matchedResults;
	private final double _score;
	
	public PredictedBhv(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnvs, UserBhv uBhv, EnumMap<MatcherType, MatchedResult> matchedResults, double score){
		_timeDate = time;
		_timeZone = timeZone;
		_uEnvs = userEnvs;
		_uBhv = uBhv;
		_matchedResults = matchedResults;
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

	public Map<MatcherType, MatchedResult> getMatchedResultMap() {
		return _matchedResults;
	}
	
	public MatchedResult getMatchedResult(MatcherType matcherType) {
		return _matchedResults.get(matcherType);
	}

	public double getScore() {
		return _score;
	}
	
	public int compareTo(PredictedBhv predictedBhv){
		if(_score < predictedBhv._score) return 1;
		else if(_score == predictedBhv._score) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matched results: ").append(_matchedResults.toString()).append(", ");
		msg.append("predicted bhv: ").append(_uBhv.toString()).append(", ");
		msg.append("score: ").append(_score);
		return msg.toString();
	}
}
