package lab.davidahn.appshuttle.predict.matcher;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class MatcherGroupResult extends MatcherResultElem {
	private EnumMap<MatcherType, MatcherResultElem> participantMatcherResults;
	
	public MatcherGroupResult(long _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		super(_time, _timeZone, _userEnv);
		participantMatcherResults = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
	}
	
	public void addMatcherResult(MatcherResultElem matcherResult) {
		participantMatcherResults.put(matcherResult.getMatcherType(), matcherResult);
	}

	@Override
	public EnumMap<MatcherType, MatcherResultElem> getAllParticipantMatchersWithResults() {
		return participantMatcherResults;
	}

	@Override
	public MatcherType getMatcherSelectedByPriority(){
		 return Collections.max(participantMatcherResults.keySet());
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(super.toString()).append(", ");
		msg.append("matcherResults").append(getAllParticipantMatchersWithResults().toString());
		return msg.toString();
	}
}
