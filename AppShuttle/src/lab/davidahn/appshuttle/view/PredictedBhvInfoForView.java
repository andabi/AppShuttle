package lab.davidahn.appshuttle.view;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.mine.matcher.MatchedResult;
import lab.davidahn.appshuttle.mine.matcher.MatcherType;

//TODO
//public class PredictedBhvInfoForView implements Comparable<PredictedBhvInfoForView> {
//	private final Date _timeDate;
//	private final TimeZone _timeZone;
//	private final Map<EnvType, UserEnv> _uEnvs;
//	private final UserBhv _uBhv;
//	private final EnumMap<MatcherType, MatchedResult> _matchedResults;
//	private final double _score;
//	
//	public PredictedBhvInfoForView(Date time, TimeZone timeZone, Map<EnvType, UserEnv> userEnvs, UserBhv uBhv, EnumMap<MatcherType, MatchedResult> matchedResults, double score){
//		_timeDate = time;
//		_timeZone = timeZone;
//		_uEnvs = userEnvs;
//		_uBhv = uBhv;
//		_matchedResults = matchedResults;
//		_score = score;
//	}
//	
//	public Date getTime() {
//		return _timeDate;
//	}
//
//	public TimeZone getTimeZone() {
//		return _timeZone;
//	}
//
//	public Map<EnvType, UserEnv> getUserEnvMap() {
//		return _uEnvs;
//	}
//
//	public UserEnv getUserEnv(EnvType envType) {
//		return _uEnvs.get(envType);
//	}
//	
//	public UserBhv getUserBhv() {
//		return _uBhv;
//	}
//
//	public Map<MatcherType, MatchedResult> getMatchedResultMap() {
//		return _matchedResults;
//	}
//	
//	public MatchedResult getMatchedResult(MatcherType matcherType) {
//		return _matchedResults.get(matcherType);
//	}
//
//	public double getScore() {
//		return _score;
//	}
//	
//	public String makeViewMessage(){
//		StringBuffer msg = new StringBuffer();
//		for(MatcherType matcherType : _matchedResults.keySet()){
//			msg.append(matcherType.viewMsg).append(", ");
//		}
//		msg.delete(msg.length() - 2 , msg.length());
//		return msg.toString();
//	}
//	
//	public int compareTo(PredictedBhvInfoForView predictedBhv){
//		if(_score < predictedBhv._score) return 1;
//		else if(_score == predictedBhv._score) return 0;
//		else return -1;
//	}
//	
//	public String toString(){
//		StringBuffer msg = new StringBuffer();
//		msg.append("matched results: ").append(_matchedResults.toString()).append(", ");
//		msg.append("predicted bhv: ").append(_uBhv.toString()).append(", ");
//		msg.append("score: ").append(_score);
//		return msg.toString();
//	}
//}
