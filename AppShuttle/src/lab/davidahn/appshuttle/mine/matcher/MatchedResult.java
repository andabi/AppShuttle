package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class MatchedResult implements Comparable<MatchedResult> {
	private MatcherType matcherType;
	private Date time;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private UserBhv userBhv;
	private double likelihood;
	private double inverseEntropy;
	private int numTotalCxt;
	private int numRelatedCxt;
	private Map<MatcherCountUnit, Double> relatedCxt;
	
	public MatchedResult(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnv){
		this.time = time;
		this.timeZone = timeZone;
		this.userEnvs = userEnv;
	}
	
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnv() {
		return userEnvs;
	}
	public void setUserEnv(Map<EnvType, UserEnv> userEnv) {
		this.userEnvs = userEnv;
	}

	public double getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(double likelihood) {
		this.likelihood = likelihood;
	}
	
	public double getInverseEntropy() {
		return inverseEntropy;
	}

	public void setInverseEntropy(double inverseEntropy) {
		this.inverseEntropy = inverseEntropy;
	}

	public UserBhv getUserBhvs() {
		return userBhv;
	}
	
	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}

	public void setUserBhv(UserBhv bhvName) {
		this.userBhv = bhvName;
	}

	public int getNumTotalCxt() {
		return numTotalCxt;
	}

	public void setNumTotalCxt(int numTotalCxt) {
		this.numTotalCxt = numTotalCxt;
	}

	public int getNumRelatedCxt() {
		return numRelatedCxt;
	}

	public void setNumRelatedCxt(int numRelatedCxt) {
		this.numRelatedCxt = numRelatedCxt;
	}
	
	public Map<MatcherCountUnit, Double> getRelatedCxt(){
		return relatedCxt;
	}
	
	public void setRelatedCxt(Map<MatcherCountUnit, Double> relatedCxt) {
		this.relatedCxt = relatedCxt;
	}

	public void addRelatedCxt(MatcherCountUnit rfdUCxt, double relatedness) {
		if(relatedCxt == null) relatedCxt = new HashMap<MatcherCountUnit, Double>();
		relatedCxt.put(rfdUCxt, relatedness);
	}

	public MatcherType getMatcherType() {
		return matcherType;
	}

	public void setMatcherType(MatcherType matcherType) {
		this.matcherType = matcherType;
	}

	public int compareTo(MatchedResult matchedCxt){
		if(likelihood < matchedCxt.likelihood) return 1;
		else if(likelihood == matchedCxt.likelihood) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(userEnvs.toString()).append(", ");
		msg.append(userBhv.toString()).append(", ");
		msg.append("condition: ").append(matcherType).append(", ");
		msg.append("likelihood: ").append(likelihood).append(", ");
		msg.append("numTotalCxt: ").append(numTotalCxt).append(", ");
		msg.append("relatedCxt: ").append(relatedCxt);
		return msg.toString();
	}
}
