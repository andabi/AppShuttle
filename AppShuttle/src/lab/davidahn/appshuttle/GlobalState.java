package lab.davidahn.appshuttle;

import java.util.List;

import lab.davidahn.appshuttle.model.MatchedCxt;
import lab.davidahn.appshuttle.model.UserBhv;
import lab.davidahn.appshuttle.model.UserEnv;
import lab.davidahn.appshuttle.model.UserLoc;
import android.content.SharedPreferences;

public class GlobalState {
	public static volatile boolean isInUse;
	public static volatile UserEnv currentUEnv;
	public static volatile UserLoc place;
	public static volatile List<UserBhv> recentMatchedBhvList;
	public static volatile boolean moved;
	public static volatile List<MatchedCxt> recentLocMatchedCxtList;
//	public static volatile SharedPreferences settings;
}
