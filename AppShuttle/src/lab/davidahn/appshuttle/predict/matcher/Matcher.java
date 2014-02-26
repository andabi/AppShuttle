package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public interface Matcher {
	
	public MatcherResult matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getMatcherType();
	
}
