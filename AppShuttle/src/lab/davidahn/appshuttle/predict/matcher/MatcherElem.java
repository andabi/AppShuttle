package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;

public interface MatcherElem {
	
	public MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);

	public MatcherType getType();
	
	public int getPriority();
}
