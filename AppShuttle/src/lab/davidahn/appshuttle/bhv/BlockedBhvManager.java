package lab.davidahn.appshuttle.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockedBhvManager {

	public static List<BlockedBhv> getBlockedBhvListSorted(){
		List<BlockedBhv> blockedBhvList = new ArrayList<BlockedBhv>(
				UserBhvManager.getInstance().getBlockedBhvSet());
		Collections.sort(blockedBhvList, Collections.reverseOrder());
		return Collections.unmodifiableList(blockedBhvList);
	}

}