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
	//usual = unblocked && unfavorated
	private Set<UserBhv> _usualBhvSet;
	private Set<UserBhv> _blockedBhvSet;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		
		_usualBhvSet = new HashSet<UserBhv>();
		_usualBhvSet.addAll(_userBhvDao.retrieveUsualUserBhv());

		_blockedBhvSet = new HashSet<UserBhv>();
		_blockedBhvSet.addAll(_userBhvDao.retrieveBlockedUserBhv());
	}
	public synchronized static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public Set<UserBhv> getUsualBhvSet(){
		return Collections.unmodifiableSet(new HashSet<UserBhv>(_usualBhvSet));
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(_usualBhvSet.contains(uBhv) || _blockedBhvSet.contains(uBhv))
			return ;

		_userBhvDao.storeUserBhv(uBhv);

		_usualBhvSet.add(uBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!_usualBhvSet.contains(uBhv) && !_blockedBhvSet.contains(uBhv))
			return ;

		_userBhvDao.deleteUserBhv(uBhv);

		_usualBhvSet.remove(uBhv);
	}
	
	public Set<UserBhv> getBlockedBhvSet(){
		return Collections.unmodifiableSet(new HashSet<UserBhv>(_blockedBhvSet));
	}
	
	public synchronized void block(UserBhv uBhv){
		if(_blockedBhvSet.contains(uBhv) || !_usualBhvSet.contains(uBhv))
			return ;

		long currTime = System.currentTimeMillis();
		BlockedUserBhv blockedUserBhv = new BlockedUserBhv(uBhv, currTime);
		
		_userBhvDao.block(blockedUserBhv);

		_blockedBhvSet.add(blockedUserBhv);

		_usualBhvSet.remove(uBhv);
	}
	
	public synchronized void unblock(UserBhv uBhv){
		if(!_blockedBhvSet.contains(uBhv) || _usualBhvSet.contains(uBhv))
			return ;

		_userBhvDao.unblock(uBhv);

		_blockedBhvSet.remove(uBhv);
		
		_usualBhvSet.add(uBhv);
	}
}