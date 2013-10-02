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
	private Set<UserBhv> _bhvSet;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		_bhvSet = new HashSet<UserBhv>();

		_bhvSet.addAll(_userBhvDao.retrieveUserBhv());
	}
	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public Set<UserBhv> getBhvSet(){
		return Collections.unmodifiableSet(_bhvSet);
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(_bhvSet.contains(uBhv))
			return ;

		_userBhvDao.storeUserBhv(uBhv);

		_bhvSet.add(uBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!_bhvSet.contains(uBhv))
			return ;

		_userBhvDao.deleteUserBhv(uBhv);

		_bhvSet.remove(uBhv);
	}
}