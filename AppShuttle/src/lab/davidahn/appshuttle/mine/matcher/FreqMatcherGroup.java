package lab.davidahn.appshuttle.mine.matcher;


public class FreqMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public FreqMatcherGroup() {
		super(MatcherGroupType.FREQUENCY, MatcherGroupType.FREQUENCY.priority);
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
