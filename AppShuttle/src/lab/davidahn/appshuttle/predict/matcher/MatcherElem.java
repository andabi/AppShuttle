package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public interface MatcherElem {
	
	public MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getType();
	
	public int getPriority();
}
