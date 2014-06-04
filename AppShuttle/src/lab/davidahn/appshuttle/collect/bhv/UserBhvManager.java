package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashSet;
import java.util.Set;

/*
 * @thread safe
 */
public class UserBhvManager {
	private Set<UserBhv> userBhvSet;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();

	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		userBhvSet = new HashSet<UserBhv>();
		for (UserBhv userBhv : userBhvDao.retrieveUserBhv())
			userBhvSet.add(userBhv);
	}

	public static UserBhvManager getInstance() {
		return userBhvManager;
	}

	public synchronized Set<UserBhv> getRegisteredBhvSet() {
		return new HashSet<UserBhv>(userBhvSet);
	}
	
	public synchronized UserBhv getRegisteredUserBhv(UserBhvType bhvType, String bhvName) {
		UserBhv target = BaseUserBhv.create(bhvType, bhvName);
		return getRegisteredUserBhv(target);
	}
	
	public synchronized UserBhv getRegisteredUserBhv(UserBhv target) {
		for(UserBhv bhv : userBhvSet)
			if(bhv.equals(target)) return bhv;
		return null;
	}

	public synchronized void register(UserBhv bhv) {
		if (userBhvSet.contains(bhv))
			return;
		userBhvDao.storeUserBhv(bhv);
		userBhvSet.add(bhv);
	}

	public synchronized void unregister(UserBhv uBhv) {
		if (!userBhvSet.contains(uBhv))
			return;
		userBhvDao.deleteUserBhv(uBhv);
		userBhvSet.remove(uBhv);
	}
}