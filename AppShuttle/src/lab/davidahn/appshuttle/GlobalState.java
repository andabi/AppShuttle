package lab.davidahn.appshuttle;

import java.util.List;

import lab.davidahn.appshuttle.bean.MatchedCxt;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class GlobalState {
	public static volatile boolean isInUse;
	public static volatile UserEnv currentUEnv;
	public static volatile UserLoc place;
	public static volatile List<UserBhv> recentMatchedBhvList;
	public static volatile boolean moved;
	public static volatile List<MatchedCxt> recentLocMatchedCxtList;
//	public static volatile SharedPreferences settings;
}
