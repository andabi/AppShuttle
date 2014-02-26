package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.predict.matcher.MatcherType;



public class TimeMatcherGroup extends BaseMatcherGroup {

	@Override
	public MatcherType getType() {
		return MatcherType.TIME;
	}
	
	@Override
	public int getPriority() {
		return MatcherType.TIME.priority;
	}

}
