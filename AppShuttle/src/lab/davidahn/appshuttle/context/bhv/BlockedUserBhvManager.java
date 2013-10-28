package lab.davidahn.appshuttle.context.bhv;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class BlockedUserBhvManager {
	private Set<UserBhv> _blockedBhvSet;
	private UserBhvDao _userBhvDao;

	private static BlockedUserBhvManager blockedUserBhvManager = new BlockedUserBhvManager();
	private BlockedUserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		_blockedBhvSet = new HashSet<UserBhv>();

		_blockedBhvSet.addAll(_userBhvDao.retrieveBlockedUserBhv());
	}
	public synchronized static BlockedUserBhvManager getInstance() {
		return blockedUserBhvManager;
	}
	
	public Set<UserBhv> getBlockedBhvSet(){
		return Collections.unmodifiableSet(_blockedBhvSet);
	}
	
	public synchronized void block(UserBhv uBhv){
		if(_blockedBhvSet.contains(uBhv))
			return ;

		_userBhvDao.block(uBhv);

		_blockedBhvSet.add(uBhv);
	}
	
	public synchronized void unblock(UserBhv uBhv){
		if(!_blockedBhvSet.contains(uBhv))
			return ;

		_userBhvDao.unblock(uBhv);

		_blockedBhvSet.remove(uBhv);
	}
}