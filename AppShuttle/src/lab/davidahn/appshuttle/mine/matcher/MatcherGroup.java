package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public interface MatcherGroup {
	public MatcherGroupType getMatcherGroupType();
	
	public int getPriority();

	public MatcherGroupResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public void registerMatcher(Matcher matcher);
	
}