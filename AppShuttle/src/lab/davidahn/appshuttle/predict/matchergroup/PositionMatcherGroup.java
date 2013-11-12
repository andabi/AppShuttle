package lab.davidahn.appshuttle.predict.matchergroup;


public class PositionMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public PositionMatcherGroup() {
		super(MatcherGroupType.POSITION, MatcherGroupType.POSITION.priority);
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
