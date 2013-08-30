package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.UserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public class GlobalState {
	public static volatile boolean inPresent;
	public static volatile UserCxt currentUCxt;
	public static volatile UserCxt prevUCxt;
	public static volatile boolean locMoved;
	public static volatile boolean placeMoved;
	public static volatile Set<UserBhv> recentMatchedBhvSet;
}
