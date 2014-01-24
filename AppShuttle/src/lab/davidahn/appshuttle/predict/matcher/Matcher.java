package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;

public interface Matcher {
	
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getMatcherType();
	
}
