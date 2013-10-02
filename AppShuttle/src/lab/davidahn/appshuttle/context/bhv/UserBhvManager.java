package lab.davidahn.appshuttle.context.bhv;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class UserBhvManager {
	private Set<UserBhv> _bhvList;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		_bhvList = new HashSet<UserBhv>();

		_bhvList.addAll(_userBhvDao.retrieveUserBhv());
	}
	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public Set<UserBhv> getBhvList(){
		return Collections.unmodifiableSet(_bhvList);
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(_bhvList.contains(uBhv))
			return ;

		_userBhvDao.storeUserBhv(uBhv);

		_bhvList.add(uBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){	
		_userBhvDao.deleteUserBhv(uBhv);

		_bhvList.remove(uBhv);
	}
}