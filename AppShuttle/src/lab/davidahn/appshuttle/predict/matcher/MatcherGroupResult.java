package lab.davidahn.appshuttle.predict.matcher;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class MatcherGroupResult extends MatcherResultElem {
	private EnumMap<MatcherType, MatcherResultElem> matcherResults;
	
	public MatcherGroupResult(Date _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		super(_time, _timeZone, _userEnv);
		matcherResults = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
	}
	
	public void addMatcherResult(MatcherResultElem matcherResult) {
		matcherResults.put(matcherResult.getMatcherType(), matcherResult);
	}

	@Override
	public EnumMap<MatcherType, MatcherResultElem> getMatcherResultMap() {
		return matcherResults;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(super.toString()).append(", ");
		msg.append("matcherResults").append(getMatcherResultMap().toString());
		return msg.toString();
	}
}
