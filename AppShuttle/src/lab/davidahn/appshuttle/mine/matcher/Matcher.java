package lab.davidahn.appshuttle.mine.matcher;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public interface Matcher {
	
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getMatcherType();
	
}
