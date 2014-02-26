package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.MatcherElem;

public interface MatcherGroup {
	public MatcherGroupType getType();
	
	public void registerMatcher(MatcherElem matcher);

	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);
}