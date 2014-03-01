package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvDao;

/*
 * @thread safe
 */
public class BlockedBhvManager {
	private Map<UserBhv, BlockedBhv> blockedBhvs;
	private UserBhvDao userBhvDao;

	private static BlockedBhvManager blockedBhvManager = new BlockedBhvManager();

	private BlockedBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		blockedBhvs = new HashMap<UserBhv, BlockedBhv>();
		updateBlockedBhvSet();
	}

	public static BlockedBhvManager getInstance() {
		return blockedBhvManager;
	}

	public synchronized Set<BlockedBhv> getBlockedBhvSet() {
		return new HashSet<BlockedBhv>(blockedBhvs.values());
	}

	public synchronized BlockedBhv getBlockedBhv(UserBhv uBhv) {
		return blockedBhvs.get(uBhv);
	}
	
	public void updateBlockedBhvSet(){
		blockedBhvs.clear();
		for (BlockedBhv blockedUserBhv : userBhvDao.retrieveBlockedUserBhv())
			blockedBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
	}

	public synchronized BlockedBhv block(UserBhv uBhv) {
		if (blockedBhvs.containsKey(uBhv))
			return null;

		long currTime = System.currentTimeMillis();
		BlockedBhv blockedUserBhv = new BlockedBhv(uBhv, currTime);

		userBhvDao.block(blockedUserBhv);
		blockedBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);

		return blockedUserBhv;
	}

	public synchronized void unblock(BlockedBhv uBhv) {
		if (!blockedBhvs.containsKey(uBhv))
			return;
		userBhvDao.unblock(uBhv);
		blockedBhvs.remove(uBhv);
	}
	
	public synchronized List<BlockedBhv> getBlockedBhvListSorted(){
		updateBlockedBhvSet();
		List<BlockedBhv> blockedBhvList = new ArrayList<BlockedBhv>(getBlockedBhvSet());
		Collections.sort(blockedBhvList, Collections.reverseOrder());
		return Collections.unmodifiableList(blockedBhvList);
	}
}