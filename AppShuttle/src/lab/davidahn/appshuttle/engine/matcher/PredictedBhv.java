package lab.davidahn.appshuttle.engine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bean.MatcherType;
import lab.davidahn.appshuttle.bean.cxt.MatchedResult;
import lab.davidahn.appshuttle.bean.env.EnvType;
import lab.davidahn.appshuttle.bean.env.UserEnv;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class PredictedBhv implements Comparable<PredictedBhv> {
	private Date time;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private UserBhv uBhv;
	private EnumMap<MatcherType, MatchedResult> matchedResults;
	private double score;
//	private Map<MatcherType, Double> likelihoods;
	
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

	public Map<EnvType, UserEnv> getUserEnvs() {
		return userEnvs;
	}

	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}
	
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public Map<MatcherType, MatchedResult> getMatchedResults() {
		return matchedResults;
	}

	public double getScore() {
		return score;
	}
	
//	public void addMatchedResult(MatchedResult matchedCxt){
//		matchedResults.add(matchedCxt);
//	}
	
	public int compareTo(PredictedBhv predictedBhv){
		if(score < predictedBhv.score) return 1;
		else if(score == predictedBhv.score) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matched cxts: ").append(matchedResults.toString()).append(", ");
		msg.append("predicted bhv: ").append(uBhv.toString()).append(", ");
		msg.append("score: ").append(score);
		return msg.toString();
	}

	public String getMatcherTypeString() {
		StringBuffer res = new StringBuffer();
		for(MatcherType matcherType : matchedResults.keySet()){
//			if(matchedResults.get(matcherType).isMatched())
			res.append(matcherType.toString().charAt(0));
		}
		return res.toString();
	}
}
