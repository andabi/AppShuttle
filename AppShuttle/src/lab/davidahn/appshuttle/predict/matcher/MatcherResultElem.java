package lab.davidahn.appshuttle.predict.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;


public abstract class MatcherResultElem implements Comparable<MatcherResultElem> {
	protected MatcherType matcherType;
	protected Date timeDate;
	protected TimeZone timeZone;
	protected Map<EnvType, UserEnv> userEnvs;
	protected UserBhv bhv;
	protected double score;
	protected String viewMsg;

	public MatcherResultElem(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		timeDate = _time;
		timeZone = _timeZone;
		userEnvs = _userEnv;
	}
	
	public MatcherType getMatcherType() {
		return matcherType;
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
	public void setMatcherType(MatcherType _matcherType) {
		matcherType = _matcherType;
	}
	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}
	public Map<EnvType, UserEnv> getUserEnvs() {
		return userEnvs;
	}
	public void setUserEnv(Map<EnvType, UserEnv> _userEnv) {
		userEnvs = _userEnv;
	}
	public UserBhv getUserBhv() {
		return bhv;
	}
	public void setUserBhv(UserBhv _bhv) {
		bhv = _bhv;
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
	
	public abstract EnumMap<MatcherType, MatcherResultElem> getAllParticipantMatchersWithResults();
	
	public abstract MatcherType getMatcherSelectedByPriority();

	@Override
	public int compareTo(MatcherResultElem matcherResult){
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
		msg.append("userEnvs").append(userEnvs.toString()).append(", ");
		msg.append("bhv").append(bhv.toString()).append(", ");
		msg.append("score").append(score);		
		return msg.toString();
	}
}