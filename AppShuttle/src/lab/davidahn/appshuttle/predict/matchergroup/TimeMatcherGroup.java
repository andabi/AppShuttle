package lab.davidahn.appshuttle.predict.matchergroup;



public class TimeMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public TimeMatcherGroup() {
		super(MatcherGroupType.TIME, MatcherGroupType.TIME.priority);
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
