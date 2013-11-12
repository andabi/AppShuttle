package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public interface Matcher {
	
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getMatcherType();
	
}
