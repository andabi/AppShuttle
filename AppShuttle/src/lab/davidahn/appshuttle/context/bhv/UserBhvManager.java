package lab.davidahn.appshuttle.context.bhv;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class UserBhvManager {
	private List<UserBhv> _bhvList;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		_bhvList = _userBhvDao.retrieveUserBhv();
	}

	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public List<UserBhv> getBhvList(){
		return Collections.unmodifiableList(_bhvList);
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