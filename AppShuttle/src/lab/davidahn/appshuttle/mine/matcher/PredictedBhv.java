package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class PredictedBhv implements Comparable<PredictedBhv> {
	private final Date time;
	private final TimeZone timeZone;
	private final Map<EnvType, UserEnv> userEnvs;
	private final UserBhv uBhv;
	private final EnumMap<MatcherType, MatchedResult> matchedResults;
	private final double score;
	
	public PredictedBhv(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnvs, UserBhv uBhv, EnumMap<MatcherType, MatchedResult> matchedResults, double score){
		this.time = time;
		this.timeZone = timeZone;
		this.userEnvs = userEnvs;
		this.uBhv = uBhv;
		this.matchedResults = matchedResults;
		this.score = score;
	}
	
	public Date getTime() {
		return time;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnvMap() {
		return userEnvs;
	}

	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}
	
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public Map<MatcherType, MatchedResult> getMatchedResultMap() {
		return matchedResults;
	}
	
	public MatchedResult getMatchedResult(MatcherType matcherType) {
		return matchedResults.get(matcherType);
	}

	public double getScore() {
		return score;
	}
	
	public int compareTo(PredictedBhv predictedBhv){
		if(score < predictedBhv.score) return 1;
		else if(score == predictedBhv.score) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matched results: ").append(matchedResults.toString()).append(", ");
		msg.append("predicted bhv: ").append(uBhv.toString()).append(", ");
		msg.append("score: ").append(score);
		return msg.toString();
	}
}
