package lab.davidahn.appshuttle.predict.matchergroup;



public class TimeMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public TimeMatcherGroup() {
		super(MatcherGroupType.TIME, MatcherGroupType.TIME.priority);
	}
}
