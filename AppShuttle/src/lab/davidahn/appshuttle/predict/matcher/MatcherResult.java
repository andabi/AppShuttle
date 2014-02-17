package lab.davidahn.appshuttle.predict.matcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class MatcherResult implements Comparable<MatcherResult> {
	private MatcherType matcherType;
	private Date timeDate;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private UserBhv uBhv;
	private double likelihood;
	private double inverseEntropy;
	private int numTotalHistory;
	private int numRelatedHistory;
	private Map<MatcherCountUnit, Double> relatedHistory;
	private double score;
	
	public MatcherResult(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		timeDate = _time;
		timeZone = _timeZone;
		userEnvs = _userEnv;
	}
	
	public Date getTime() {
		return timeDate;
	}

	public void setTime(Date time) {
		timeDate = time;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone _timeZone) {
		timeZone = _timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnv() {
		return userEnvs;
	}
	public void setUserEnv(Map<EnvType, UserEnv> userEnv) {
		userEnvs = userEnv;
	}

	public double getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(double _likelihood) {
		likelihood = _likelihood;
	}
	
	public double getInverseEntropy() {
		return inverseEntropy;
	}

	public void setInverseEntropy(double _inverseEntropy) {
		inverseEntropy = _inverseEntropy;
	}

	public UserBhv getUserBhvs() {
		return uBhv;
	}
	
	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}

	public void setUserBhv(UserBhv bhvName) {
		uBhv = bhvName;
	}

	public int getNumTotalCxt() {
		return numTotalHistory;
	}

	public void setNumTotalHistory(int _numTotalHistory) {
		numTotalHistory = _numTotalHistory;
	}

	public int getNumRelatedHistory() {
		return numRelatedHistory;
	}

	public void setNumRelatedHistory(int _numRelatedHistory) {
		numRelatedHistory = _numRelatedHistory;
	}
	
	public Map<MatcherCountUnit, Double> getRelatedCxt(){
		return relatedHistory;
	}
	
	public void setRelatedHistory(Map<MatcherCountUnit, Double> _relatedHistory) {
		relatedHistory = _relatedHistory;
	}

	public void addRelatedCxt(MatcherCountUnit durationUserBhv, double relatedness) {
		if(relatedHistory == null) relatedHistory = new HashMap<MatcherCountUnit, Double>();
		relatedHistory.put(durationUserBhv, relatedness);
	}

	public MatcherType getMatcherType() {
		return matcherType;
	}

	public void setMatcherType(MatcherType _matcherType) {
		matcherType = _matcherType;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double _score) {
		score = _score;
	}

	public int compareTo(MatcherResult matcherResult){
		MatcherTypeComparator comparator = new MatcherTypeComparator();
		int comp = comparator.compare(matcherType, matcherResult.matcherType);
		if(comp != 0)
			return comp;
		
		if(score > matcherResult.score) 
			return 1;
		else if(score == matcherResult.score) 
			return 0;
		else 
			return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matcherType: ").append(matcherType).append(", ");
		msg.append("timeDate").append(timeDate).append(", ");
		msg.append("timeZone").append(timeZone).append(", ");
		msg.append(userEnvs.toString()).append(", ");
		msg.append(uBhv.toString()).append(", ");
		msg.append("likelihood: ").append(likelihood).append(", ");
		msg.append("inverseEntropy").append(inverseEntropy).append(", ");
		msg.append("numTotalHistory: ").append(numTotalHistory).append(", ");
		msg.append("numRelatedHistory: ").append(numRelatedHistory).append(", ");
		msg.append("relatedHistory").append(relatedHistory).append(", ");
		msg.append("score").append(score);		
		return msg.toString();
	}
}
