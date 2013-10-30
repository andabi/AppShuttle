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
	private Set<UserBhv> _totalBhvSet;
	private Set<BaseUserBhv> _ordinaryBhvSet;	//ordinary -> unfavorates && unblocked
	private Set<FavoratesUserBhv> _favoratesBhvSet;
	private Set<BlockedUserBhv> _blockedBhvSet;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		
		_ordinaryBhvSet = new HashSet<BaseUserBhv>();
		_ordinaryBhvSet.addAll(_userBhvDao.retrieveOrdinaryUserBhv());

		_favoratesBhvSet = new HashSet<FavoratesUserBhv>();
		_favoratesBhvSet.addAll(_userBhvDao.retrieveFavoratesUserBhv());

		_blockedBhvSet = new HashSet<BlockedUserBhv>();
		_blockedBhvSet.addAll(_userBhvDao.retrieveBlockedUserBhv());
		
		_totalBhvSet = new HashSet<UserBhv>();
		_totalBhvSet.addAll(_ordinaryBhvSet);
		_totalBhvSet.addAll(_favoratesBhvSet);
		_totalBhvSet.addAll(_blockedBhvSet);
	}
	public synchronized static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public Set<UserBhv> getTotalBhvSet(){
		return Collections.unmodifiableSet(new HashSet<UserBhv>(_totalBhvSet));
	}
	
	public Set<BaseUserBhv> getOrdinaryBhvSet(){
		return Collections.unmodifiableSet(new HashSet<BaseUserBhv>(_ordinaryBhvSet));
	}
	
	public Set<BlockedUserBhv> getBlockedBhvSet(){
		return Collections.unmodifiableSet(new HashSet<BlockedUserBhv>(_blockedBhvSet));
	}
	public Set<FavoratesUserBhv> getFavoratesBhvSet(){
		return Collections.unmodifiableSet(new HashSet<FavoratesUserBhv>(_favoratesBhvSet));
	}
	public synchronized void registerBhv(BaseUserBhv uBhv){
		if(_totalBhvSet.contains(uBhv))
			return ;
//		if(_ordinaryBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoratesBhvSet.contains(uBhv))
//			return ;

		_userBhvDao.storeUserBhv(uBhv);

		_totalBhvSet.add(uBhv);
		_ordinaryBhvSet.add(uBhv);
	}
	
	public synchronized void unregisterBhv(BaseUserBhv uBhv){
		if(!_totalBhvSet.contains(uBhv))
			return ;
//		if(!_ordinaryBhvSet.contains(uBhv) && 
//				!_blockedBhvSet.contains(uBhv) || 
//				!_favoratesBhvSet.contains(uBhv))
//			return ;

		_userBhvDao.deleteUserBhv(uBhv);

		_ordinaryBhvSet.remove(uBhv);
		_totalBhvSet.remove(uBhv);
	}
	
	public synchronized void block(UserBhv uBhv){
		if(_blockedBhvSet.contains(uBhv) || 
				!_ordinaryBhvSet.contains(uBhv))
			return ;

		long currTime = System.currentTimeMillis();
		BlockedUserBhv blockedUserBhv = new BlockedUserBhv(uBhv, currTime);
		
		_userBhvDao.block(blockedUserBhv);

		_blockedBhvSet.add(blockedUserBhv);

		_ordinaryBhvSet.remove(uBhv);
	}
	
	public synchronized void unblock(BlockedUserBhv uBhv){
		if(!_blockedBhvSet.contains(uBhv) || 
				_ordinaryBhvSet.contains(uBhv))
			return ;

		_userBhvDao.unblock(uBhv);

		_blockedBhvSet.remove(uBhv);
		
		_ordinaryBhvSet.add((BaseUserBhv)uBhv.getUserBhv());
	}
	
	
	public synchronized void favorates(UserBhv uBhv){
		if(_favoratesBhvSet.contains(uBhv) || 
				!_ordinaryBhvSet.contains(uBhv))
			return ;

		long currTime = System.currentTimeMillis();
		FavoratesUserBhv favoratesUserBhv = new FavoratesUserBhv(uBhv, currTime);
		
		_userBhvDao.favorates(favoratesUserBhv);

		_favoratesBhvSet.add(favoratesUserBhv);

		_ordinaryBhvSet.remove(uBhv);
	}
	
	public synchronized void unfavorates(FavoratesUserBhv uBhv){
		if(!_favoratesBhvSet.contains(uBhv) || 
				_ordinaryBhvSet.contains(uBhv))
			return ;

		_userBhvDao.unfavorates(uBhv);

		_favoratesBhvSet.remove(uBhv);
		
		_ordinaryBhvSet.add((BaseUserBhv)uBhv.getUserBhv());
	}
}