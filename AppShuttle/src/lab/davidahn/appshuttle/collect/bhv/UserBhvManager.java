package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashSet;
import java.util.Set;

/*
 * @thread safe
 */
public class UserBhvManager {
	private Set<BaseUserBhv> userBhvSet;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();

	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		userBhvSet = new HashSet<BaseUserBhv>();
		for (BaseUserBhv userBhv : userBhvDao.retrieveUserBhv())
			userBhvSet.add(userBhv);
	}

	public static UserBhvManager getInstance() {
		return userBhvManager;
	}

	public synchronized Set<BaseUserBhv> getRegisteredBhvSet() {
		return new HashSet<BaseUserBhv>(userBhvSet);
	}
	
	public synchronized BaseUserBhv getRegisteredUserBhv(UserBhvType bhvType, String bhvName) {
		BaseUserBhv target = BaseUserBhv.create(bhvType, bhvName);
		return getRegisteredUserBhv(target);
	}
	
	public synchronized BaseUserBhv getRegisteredUserBhv(UserBhv target) {
		for(BaseUserBhv bhv : userBhvSet)
			if(bhv.equals(target)) return bhv;
		return null;
	}

	public synchronized void register(BaseUserBhv bhv) {
		if (userBhvSet.contains(bhv))
			return;
		userBhvDao.storeUserBhv(bhv);
		userBhvSet.add(bhv);
	}

	public synchronized void unregister(BaseUserBhv uBhv) {
		if (!userBhvSet.contains(uBhv))
			return;
		userBhvDao.deleteUserBhv(uBhv);
		userBhvSet.remove(uBhv);
	}
}