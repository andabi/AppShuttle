package lab.davidahn.appshuttle.predict.matcher;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class MatcherGroupResult extends MatcherResultElem {
	private EnumMap<MatcherType, MatcherResultElem> childMatcherResults;
	
	public MatcherGroupResult(long _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnv){
		super(_time, _timeZone, _userEnv);
		childMatcherResults = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
	}
	
	public void addMatcherResult(MatcherResultElem matcherResult) {
		childMatcherResults.put(matcherResult.getMatcherType(), matcherResult);
	}

	@Override
	public EnumMap<MatcherType, MatcherResultElem> getChildMatchersWithResult() {
		return childMatcherResults;
	}

	@Override
	public EnumMap<MatcherType, MatcherResultElem> getAllLeafMatchersWithResult(){
		EnumMap<MatcherType, MatcherResultElem> res = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
		for(MatcherResultElem resultElem : childMatcherResults.values())
			res.putAll(resultElem.getAllLeafMatchersWithResult());
		return res;
	}

	@Override
	public MatcherType getFinalMatcher(){
		 return Collections.max(childMatcherResults.keySet());
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(super.toString()).append(", ");
		msg.append("child matchers: ").append(getChildMatchersWithResult().toString());
		return msg.toString();
	}
}
