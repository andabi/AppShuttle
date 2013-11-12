package lab.davidahn.appshuttle.predict.matchergroup;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.Matcher;

public interface MatcherGroup {
	public MatcherGroupType getMatcherGroupType();
	
	public int getPriority();

	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public void registerMatcher(Matcher matcher);
	
}