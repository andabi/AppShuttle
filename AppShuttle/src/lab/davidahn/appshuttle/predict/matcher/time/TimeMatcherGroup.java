package lab.davidahn.appshuttle.predict.matcher.time;

import lab.davidahn.appshuttle.predict.matcher.MatcherGroup;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;



public class TimeMatcherGroup extends MatcherGroup {

	@Override
	public MatcherType getType() {
		return MatcherType.TIME;
	}
}
