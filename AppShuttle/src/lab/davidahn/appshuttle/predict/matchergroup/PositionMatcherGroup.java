package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.predict.matcher.MatcherType;


public class PositionMatcherGroup extends BaseMatcherGroup {
	
	@Override
	public MatcherType getType() {
		return MatcherType.POSITION;
	}

	@Override
	public int getPriority() {
		return MatcherType.POSITION.priority;
	}
}
