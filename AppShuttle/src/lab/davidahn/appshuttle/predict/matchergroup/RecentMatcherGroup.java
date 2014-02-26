package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class RecentMatcherGroup extends BaseMatcherGroup {
	
	@Override
	public MatcherType getType() {
		return MatcherType.FREQUENCY;
	}

	@Override
	public int getPriority() {
		return MatcherGroupType.FREQUENCY.priority;
	}
}
