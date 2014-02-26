package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;


public abstract class BaseMatcherElem implements MatcherElem {
	public abstract MatcherType getType();
	public abstract MatcherResultElem matchAndGetResult(UserBhv uBhv, SnapshotUserCxt currUCxt);
	public abstract int getPriority();
}
