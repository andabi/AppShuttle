package lab.davidahn.appshuttle.mine.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class MatcherGroupResult implements Comparable<MatcherGroupResult> {
	private MatcherGroupType _matcherGroupType;
	private Date _timeDate;
	private TimeZone _timeZone;
	private Map<EnvType, UserEnv> _userEnvs;
	private UserBhv targetUBhv;
	private EnumMap<MatcherType, MatcherResult> matcherResults;
	private double _score;
	private String viewMsg;
	
	public MatcherGroupResult(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnv){
		_timeDate = time;
		_timeZone = timeZone;
		_userEnvs = userEnv;
		matcherResults = new EnumMap<MatcherType, MatcherResult>(MatcherType.class);
	}
	
	public Date getTime() {
		return _timeDate;
	}

//	public void setTime(Date time) {
//		_timeDate = time;
//	}

	public TimeZone getTimeZone() {
		return _timeZone;
	}

//	public void setTimeZone(TimeZone timeZone) {
//		_timeZone = timeZone;
//	}

	public Map<EnvType, UserEnv> getUserEnv() {
		return _userEnvs;
	}

//	public void setUserEnv(Map<EnvType, UserEnv> userEnv) {
//		_userEnvs = userEnv;
//	}

	public UserBhv getUserBhvs() {
		return targetUBhv;
	}
	
	public UserEnv getTargetUserEnv(EnvType envType) {
		return _userEnvs.get(envType);
	}

	public void setTargetUserBhv(UserBhv bhvName) {
		targetUBhv = bhvName;
	}

	public MatcherGroupType getMatcherGroupType() {
		return _matcherGroupType;
	}

	public void setMatcherGroupType(MatcherGroupType matcherGroupType) {
		_matcherGroupType = matcherGroupType;
	}
	
	public double getScore() {
		return _score;
	}

	public void setScore(double score) {
		_score = score;
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
		int comp = comparator.compare(_matcherGroupType, matcherGroupResult._matcherGroupType);		
		if(comp != 0)
			return comp;
		
		if(_score < matcherGroupResult._score) 
			return 1;
		else if(_score == matcherGroupResult._score) 
			return 0;
		else 
			return -1;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("matcherType: ").append(_matcherGroupType).append(", ");
		msg.append("timeDate").append(_timeDate).append(", ");
		msg.append("timeZone").append(_timeZone).append(", ");
		msg.append(_userEnvs.toString()).append(", ");
		msg.append(targetUBhv.toString()).append(", ");
		msg.append("score").append(_score);		
		return msg.toString();
	}

}
