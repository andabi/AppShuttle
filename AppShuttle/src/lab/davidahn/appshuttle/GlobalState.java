package lab.davidahn.appshuttle;

import java.util.List;

import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bean.cxt.MatchedResult;
import lab.davidahn.appshuttle.bean.cxt.UserCxt;
import lab.davidahn.appshuttle.bhv.UserBhv;

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
