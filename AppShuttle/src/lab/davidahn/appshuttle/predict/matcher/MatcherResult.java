package lab.davidahn.appshuttle.predict.matcher;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class MatcherResult extends MatcherResultElem {
	private double likelihood;
	private double inverseEntropy;
	private int numTotalHistory;
	private int numRelatedHistory;
	private Map<MatcherCountUnit, Double> relatedHistory;
	
	public MatcherResult(long _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		super(_time, _timeZone, _userEnv);
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
	public int getNumTotalHistory() {
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
	public Map<MatcherCountUnit, Double> getRelatedHistory(){
		return relatedHistory;
	}
	public void setRelatedHistory(Map<MatcherCountUnit, Double> _relatedHistory) {
		relatedHistory = _relatedHistory;
	}

	public void addRelatedHistory(MatcherCountUnit durationUserBhv, double relatedness) {
		if(relatedHistory == null) relatedHistory = new HashMap<MatcherCountUnit, Double>();
		relatedHistory.put(durationUserBhv, relatedness);
	}
	
	@Override
	public EnumMap<MatcherType, MatcherResultElem> getAllParticipantMatchersWithResults() {
		EnumMap<MatcherType, MatcherResultElem> matcherResultMap = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
		matcherResultMap.put(matcherType, this);
		return matcherResultMap;
	}
	
	@Override
	public MatcherType getMatcherSelectedByPriority(){
		return matcherType;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(super.toString()).append(", ");
		msg.append("likelihood: ").append(likelihood).append(", ");
		msg.append("inverseEntropy").append(inverseEntropy).append(", ");
		msg.append("numTotalHistory: ").append(numTotalHistory).append(", ");
		msg.append("numRelatedHistory: ").append(numRelatedHistory).append(", ");
		msg.append("relatedHistory").append(relatedHistory);
		return msg.toString();
	}
}
