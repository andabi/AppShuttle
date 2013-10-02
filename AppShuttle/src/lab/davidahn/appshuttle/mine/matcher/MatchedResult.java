package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class MatchedResult implements Comparable<MatchedResult> {
	private MatcherType _matcherType;
	private Date _timeDate;
	private TimeZone _timeZone;
	private Map<EnvType, UserEnv> _userEnvs;
	private UserBhv _uBhv;
	private double _likelihood;
	private double _inverseEntropy;
	private int _numTotalCxt;
	private int _numRelatedCxt;
	private Map<MatcherCountUnit, Double> _relatedCxt;
	
	public MatchedResult(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnv){
		_timeDate = time;
		_timeZone = timeZone;
		_userEnvs = userEnv;
	}
	
	public Date getTime() {
		return _timeDate;
	}

	public void setTime(Date time) {
		_timeDate = time;
	}

	public TimeZone getTimeZone() {
		return _timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnv() {
		return _userEnvs;
	}
	public void setUserEnv(Map<EnvType, UserEnv> userEnv) {
		_userEnvs = userEnv;
	}

	public double getLikelihood() {
		return _likelihood;
	}

	public void setLikelihood(double likelihood) {
		_likelihood = likelihood;
	}
	
	public double getInverseEntropy() {
		return _inverseEntropy;
	}

	public void setInverseEntropy(double inverseEntropy) {
		_inverseEntropy = inverseEntropy;
	}

	public UserBhv getUserBhvs() {
		return _uBhv;
	}
	
	public UserEnv getUserEnv(EnvType envType) {
		return _userEnvs.get(envType);
	}

	public void setUserBhv(UserBhv bhvName) {
		_uBhv = bhvName;
	}

	public int getNumTotalCxt() {
		return _numTotalCxt;
	}

	public void setNumTotalCxt(int numTotalCxt) {
		_numTotalCxt = numTotalCxt;
	}

	public int getNumRelatedCxt() {
		return _numRelatedCxt;
	}

	public void setNumRelatedCxt(int numRelatedCxt) {
		_numRelatedCxt = numRelatedCxt;
	}
	
	public Map<MatcherCountUnit, Double> getRelatedCxt(){
		return _relatedCxt;
	}
	
	public void setRelatedCxt(Map<MatcherCountUnit, Double> relatedCxt) {
		_relatedCxt = relatedCxt;
	}

	public void addRelatedCxt(MatcherCountUnit rfdUCxt, double relatedness) {
		if(_relatedCxt == null) _relatedCxt = new HashMap<MatcherCountUnit, Double>();
		_relatedCxt.put(rfdUCxt, relatedness);
	}

	public MatcherType getMatcherType() {
		return _matcherType;
	}

	public void setMatcherType(MatcherType matcherType) {
		_matcherType = matcherType;
	}

	public int compareTo(MatchedResult matchedCxt){
		if(_likelihood < matchedCxt._likelihood) return 1;
		else if(_likelihood == matchedCxt._likelihood) return 0;
		else return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(_userEnvs.toString()).append(", ");
		msg.append(_uBhv.toString()).append(", ");
		msg.append("condition: ").append(_matcherType).append(", ");
		msg.append("likelihood: ").append(_likelihood).append(", ");
		msg.append("numTotalCxt: ").append(_numTotalCxt).append(", ");
		msg.append("relatedCxt: ").append(_relatedCxt);
		return msg.toString();
	}
}
