package lab.davidahn.appshuttle.predict.matcher.position;

import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;


public class PositionMatcherGroup extends MatcherGroup {
	
	@Override
	public MatcherType getType() {
		return MatcherType.POSITION;
	}
}
