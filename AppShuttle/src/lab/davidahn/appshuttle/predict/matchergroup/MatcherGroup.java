package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.predict.matcher.Matcher;

public interface MatcherGroup {
	public MatcherGroupType getMatcherGroupType();
	
	public void registerMatcher(Matcher matcher);

	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public int getPriority();
}