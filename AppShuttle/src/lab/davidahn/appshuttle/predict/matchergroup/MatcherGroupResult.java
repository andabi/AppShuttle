package lab.davidahn.appshuttle.predict.matchergroup;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class MatcherGroupResult implements Comparable<MatcherGroupResult> {
	private MatcherGroupType matcherGroupType;
	private Date timeDate;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private UserBhv targetUBhv;
	private EnumMap<MatcherType, MatcherResult> matcherResults;
	private double score;
	private String viewMsg;
	
	public MatcherGroupResult(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		timeDate = _time;
		timeZone = _timeZone;
		userEnvs = _userEnv;
		matcherResults = new EnumMap<MatcherType, MatcherResult>(MatcherType.class);
	}
	
	public Date getTime() {
		return timeDate;
	}

	public void setTime(Date _time) {
		timeDate = _time;
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

	public void setUserEnv(Map<EnvType, UserEnv> _userEnv) {
		userEnvs = _userEnv;
	}

	public UserBhv getUserBhvs() {
		return targetUBhv;
	}
	
	public UserEnv getTargetUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}

	public void setTargetUserBhv(UserBhv bhvName) {
		targetUBhv = bhvName;
	}

	public MatcherGroupType getMatcherGroupType() {
		return matcherGroupType;
	}

	public void setMatcherGroupType(MatcherGroupType _matcherGroupType) {
		matcherGroupType = _matcherGroupType;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double _score) {
		score = _score;
	}

	public String getViewMsg() {
		return viewMsg;
	}
	
	public void setViewMsg(String _viewMsg) {
		viewMsg = _viewMsg;
	}

	public void addMatcherResult(MatcherResult matcherResult) {
		matcherResults.put(matcherResult.getMatcherType(), matcherResult);
	}
	
	public EnumMap<MatcherType, MatcherResult> getMatcherResultMap() {
		return matcherResults;
	}

	@Override
	public int compareTo(MatcherGroupResult matcherGroupResult){
		MatcherGroupTypeComparator comparator = new MatcherGroupTypeComparator();
		int comp = comparator.compare(matcherGroupType, matcherGroupResult.matcherGroupType);		
		if(comp != 0)
			return comp;
		
		if(score < matcherGroupResult.score) 
			return 1;
		else if(score == matcherGroupResult.score) 
			return 0;
		else 
			return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matcherType: ").append(matcherGroupType).append(", ");
		msg.append("timeDate").append(timeDate).append(", ");
		msg.append("timeZone").append(timeZone).append(", ");
		msg.append("userEnvs").append(userEnvs.toString()).append(", ");
		msg.append("targetUBhv").append(targetUBhv.toString()).append(", ");
		msg.append("score").append(score);		
		return msg.toString();
	}
}
