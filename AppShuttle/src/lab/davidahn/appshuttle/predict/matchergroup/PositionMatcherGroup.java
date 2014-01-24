package lab.davidahn.appshuttle.predict.matchergroup;


public class PositionMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public PositionMatcherGroup() {
		super(MatcherGroupType.POSITION, MatcherGroupType.POSITION.priority);
	}
}
