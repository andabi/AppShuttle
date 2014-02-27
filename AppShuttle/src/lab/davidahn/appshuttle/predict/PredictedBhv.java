package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;

public class PredictedBhv implements UserBhv, Comparable<PredictedBhv> {
	private final UserBhv uBhv;
	private final Date timeDate;
	private final TimeZone timeZone;
	private final Map<EnvType, UserEnv> uEnvs;
	private final EnumMap<MatcherGroupType, MatcherGroupResult> matcherGroupResults;
	private final double score;

	public PredictedBhv(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnvs, UserBhv _uBhv, EnumMap<MatcherGroupType, MatcherGroupResult> _matcherGroupResults, double _score){
		timeDate = _time;
		timeZone = _timeZone;
		uEnvs = _userEnvs;
		uBhv = _uBhv;
		matcherGroupResults = _matcherGroupResults;
		score = _score;
	}

	public Date getTimeDate() {
		return timeDate;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public Map<EnvType, UserEnv> getUserEnvMap() {
		return uEnvs;
	}

	public UserEnv getUserEnv(EnvType envType) {
		return uEnvs.get(envType);
	}
	
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public Map<MatcherGroupType, MatcherGroupResult> getMatcherGroupResultMap() {
		return matcherGroupResults;
	}
	
	public MatcherGroupResult getMatcherGroupResult(MatcherGroupType matcherGroupType) {
		return matcherGroupResults.get(matcherGroupType);
	}

	public double getScore() {
		return score;
	}
	
	public EnumMap<MatcherType, MatcherResult> getMatcherResultMap(){
		EnumMap<MatcherType, MatcherResult> matcherResultMap = new EnumMap<MatcherType, MatcherResult>(MatcherType.class);
		for(MatcherGroupType matcherGroupType : matcherGroupResults.keySet())
			matcherResultMap.putAll(matcherGroupResults.get(matcherGroupType).getMatcherResultMap());
		return matcherResultMap;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return uBhv.hashCode();
	}
	
	@Override
	public int compareTo(PredictedBhv predictedBhvInfo){
		return timeDate.compareTo(predictedBhvInfo.timeDate);
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("bhv: ").append(uBhv.toString()).append(", ");
		msg.append("time: ").append(timeDate.toString()).append(", ");
		msg.append("matcher group results: ").append(matcherGroupResults.toString()).append(", ");
		msg.append("score: ").append(score);
		return msg.toString();
	}

	public static List<PredictedBhv> getRecentPredictedBhvList() {
		return new ArrayList<PredictedBhv>(AppShuttleApplication.predictedBhvMap.values());
	}
	
	public static PredictedBhv getRecentPredictedBhv(UserBhv bhv) {
		return AppShuttleApplication.predictedBhvMap.get(bhv);
	}
	
	protected static void updatePredictedBhv(PredictedBhv bhv) {
		AppShuttleApplication.predictedBhvMap.put(bhv.getUserBhv(), bhv);
	}
	
	protected static void updatePredictedBhvList(List<PredictedBhv> list) {
		Map<UserBhv, PredictedBhv> map = new HashMap<UserBhv, PredictedBhv>();
		for(PredictedBhv bhv : list)
			map.put(bhv.getUserBhv(), bhv);
		AppShuttleApplication.predictedBhvMap = map;
	}

	@Override
	public UserBhvType getBhvType() {
		return uBhv.getBhvType();
	}

	@Override
	public void setBhvType(UserBhvType bhvType) {
		uBhv.setBhvType(bhvType);
	}

	@Override
	public String getBhvName() {
		return uBhv.getBhvName();
	}

	@Override
	public void setBhvName(String bhvName) {
		uBhv.setBhvName(bhvName);
	}

	@Override
	public Object getMeta(String key) {
		return uBhv.getMeta(key);
	}

	@Override
	public void setMeta(String key, Object val) {
		uBhv.setMeta(key, val);
	}
}
