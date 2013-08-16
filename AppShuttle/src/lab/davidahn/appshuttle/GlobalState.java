package lab.davidahn.appshuttle;

import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bean.cxt.MatchedCxt;
import lab.davidahn.appshuttle.bean.env.EnvType;
import lab.davidahn.appshuttle.bean.env.UserEnv;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class GlobalState {
	public static volatile boolean isInUse;
	public static volatile Map<EnvType, UserEnv> currentUEnvs;
	public static volatile UserLoc place;
	public static volatile List<UserBhv> recentMatchedBhvList;
	public static volatile boolean moved;
	public static volatile List<MatchedCxt> recentLocMatchedCxtList;
//	public static volatile SharedPreferences settings;
}
