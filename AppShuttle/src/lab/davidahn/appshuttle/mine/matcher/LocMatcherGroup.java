package lab.davidahn.appshuttle.mine.matcher;

import java.util.List;

public class LocMatcherGroup extends BaseMatcherGroup implements MatcherGroup {
	
	public LocMatcherGroup(MatcherGroupType matcherGroupType, int priority) {
		super(matcherGroupType, priority);
	}
	
	@Override
	protected String extractViewMsg(List<MatcherResult> matcherResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double computeScore(List<MatcherResult> matcherResults) {
		// TODO Auto-generated method stub
		return 0;
	}
}