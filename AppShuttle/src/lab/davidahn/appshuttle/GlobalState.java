package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public class GlobalState {
	public static volatile SnapshotUserCxt currUserCxt;
	public static volatile Set<UserBhv> recentMatchedBhvSet;
}
