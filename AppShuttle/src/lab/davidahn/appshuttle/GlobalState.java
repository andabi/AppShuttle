package lab.davidahn.appshuttle;

import java.util.List;

import lab.davidahn.appshuttle.context.UserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.mine.matcher.MatchedResult;

public class GlobalState {
	public static volatile boolean isInUse;
	public static volatile UserCxt currentUCxt;
	public static volatile UserCxt prevUCxt;
	public static volatile UserLoc place;
	public static volatile List<UserBhv> recentMatchedBhvList;
	public static volatile boolean moved;
	public static volatile List<MatchedResult> recentLocMatchedCxtList;
//	public static volatile SharedPreferences settings;
}
