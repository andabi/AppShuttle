package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;


public abstract class BaseMatcherElem implements MatcherElem {
	public abstract MatcherType getType();
	public abstract MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);
	public abstract int getPriority();
}
