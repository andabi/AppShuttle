package lab.davidahn.appshuttle.bhv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;

/*
 * @thread safe
 */
public class UserBhvManager {
	private Set<UserBhv> userBhvSet;
	private Set<BaseUserBhv> normalBhvSet; // normal = unfavorite && unblocked
	private Map<UserBhv, FavoriteBhv> favoriteBhvs;
	private Map<UserBhv, BlockedBhv> blockedBhvs;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();

	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		userBhvSet = new HashSet<UserBhv>();

		normalBhvSet = new HashSet<BaseUserBhv>();
		for (BaseUserBhv normalUserBhv : userBhvDao.retrieveNormalUserBhv()) {
			userBhvSet.add(normalUserBhv);
			normalBhvSet.add(normalUserBhv);
		}

		favoriteBhvs = new HashMap<UserBhv, FavoriteBhv>();
		for (FavoriteBhv favoriteUserBhv : userBhvDao.retrieveFavoriteUserBhv()) {
			userBhvSet.add(favoriteUserBhv);
			favoriteBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
		}

		blockedBhvs = new HashMap<UserBhv, BlockedBhv>();
		for (BlockedBhv blockedUserBhv : userBhvDao.retrieveBlockedUserBhv()) {
			userBhvSet.add(blockedUserBhv);
			blockedBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
		}
	}

	public static UserBhvManager getInstance() {
		return userBhvManager;
	}

	public Set<UserBhv> getBhvSet() {
		return new HashSet<UserBhv>(userBhvSet);
	}

	public Set<BaseUserBhv> getNormalBhvSet() {
		return new HashSet<BaseUserBhv>(normalBhvSet);
	}

	public Set<FavoriteBhv> getFavoriteBhvSet() {
		return new HashSet<FavoriteBhv>(favoriteBhvs.values());
	}

	public ViewableUserBhv getFavoriteBhv(UserBhv uBhv) {
		return favoriteBhvs.get(uBhv);
	}

	public Set<BlockedBhv> getBlockedBhvSet() {
		return new HashSet<BlockedBhv>(blockedBhvs.values());
	}

	public ViewableUserBhv getBlockedBhv(UserBhv uBhv) {
		return blockedBhvs.get(uBhv);
	}

	public synchronized void registerBhv(UserBhv uBhv) {
		if (userBhvSet.contains(uBhv))
			return;

		BaseUserBhv normalUserBhv = BaseUserBhv.create(uBhv.getBhvType(),
				uBhv.getBhvName());

		userBhvDao.storeUserBhv(normalUserBhv);

		userBhvSet.add(normalUserBhv);

		normalBhvSet.add(normalUserBhv);
	}

	public synchronized void unregisterBhv(UserBhv uBhv) {
		if (!userBhvSet.contains(uBhv))
			return;

		userBhvDao.deleteUserBhv(uBhv);

		userBhvSet.remove(uBhv);

		if (normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
			return;
		}
		if (favoriteBhvs.containsKey(uBhv)) {
			favoriteBhvs.remove(uBhv);
			return;
		}
		if (blockedBhvs.containsKey(uBhv)) {
			blockedBhvs.remove(uBhv);
			return;
		}
	}

	public synchronized BlockedBhv block(ViewableUserBhv uBhv) {
		if (blockedBhvs.containsKey(uBhv))
			return null;

		if (normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
		} else if (favoriteBhvs.containsKey(uBhv)) {
			favoriteBhvs.remove(uBhv);
		}

		long currTime = System.currentTimeMillis();
		BlockedBhv blockedUserBhv = new BlockedBhv(uBhv, currTime);

		userBhvDao.block(blockedUserBhv);
		blockedBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);

		userBhvSet.add(blockedUserBhv);

		return blockedUserBhv;
	}

	public synchronized BaseUserBhv unblock(BlockedBhv uBhv) {
		if (!blockedBhvs.containsKey(uBhv))
			return null;

		BaseUserBhv normalUserBhv = BaseUserBhv.create(uBhv.getBhvType(),
				uBhv.getBhvName());
		normalBhvSet.add(normalUserBhv);

		userBhvDao.unblock(uBhv);
		blockedBhvs.remove(uBhv);

		userBhvSet.add(normalUserBhv);

		return normalUserBhv;
	}

	public synchronized FavoriteBhv favorite(ViewableUserBhv uBhv) {
		if (favoriteBhvs.containsKey(uBhv))
			return null;

		if (normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
		} else if (blockedBhvs.containsKey(uBhv)) {
			blockedBhvs.remove(uBhv);
		}

		long currTime = System.currentTimeMillis();

		FavoriteBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteBhv(uBhv, currTime, false);

		if (!FavoriteBhvManager.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();

		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);

		userBhvSet.add(favoriteUserBhv);

		return favoriteUserBhv;
	}

	public synchronized BaseUserBhv unfavorite(FavoriteBhv uBhv) {
		if (!favoriteBhvs.containsKey(uBhv))
			return null;

		BaseUserBhv normalUserBhv = BaseUserBhv.create(uBhv.getBhvType(),
				uBhv.getBhvName());
		normalBhvSet.add(normalUserBhv);

		uBhv.setUnNotifiable();
		userBhvDao.unfavorite(uBhv);
		favoriteBhvs.remove(uBhv);

		userBhvSet.add(normalUserBhv);

		return normalUserBhv;
	}
}