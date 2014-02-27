package lab.davidahn.appshuttle.predict.matcher.recent;

import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class RecentMatcherGroup extends MatcherGroup {
	
	@Override
	public MatcherType getType() {
		return MatcherType.FREQUENCY;
	}
}
