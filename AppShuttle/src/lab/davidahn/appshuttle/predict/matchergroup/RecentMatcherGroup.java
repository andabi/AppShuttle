package lab.davidahn.appshuttle.predict.matchergroup;


public class RecentMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public RecentMatcherGroup() {
		super(MatcherGroupType.FREQUENCY, MatcherGroupType.FREQUENCY.priority);
	}
	
}
