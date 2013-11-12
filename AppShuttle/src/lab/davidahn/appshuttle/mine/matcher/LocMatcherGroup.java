package lab.davidahn.appshuttle.mine.matcher;


public class LocMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public LocMatcherGroup() {
		super(MatcherGroupType.LOCATION, MatcherGroupType.LOCATION.priority);
	}
	
//	@Override
//	protected String extractViewMsg(List<MatcherResult> matcherResults) {
//		return null;
//	}
//
//	@Override
//	protected double computeScore(List<MatcherResult> matcherResults) {
//		return 0;
//	}
}
