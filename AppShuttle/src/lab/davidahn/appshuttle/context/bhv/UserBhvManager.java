package lab.davidahn.appshuttle.context.bhv;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import lab.davidahn.appshuttle.view.BlockedUserBhv;
import lab.davidahn.appshuttle.view.FavoratesUserBhv;
import lab.davidahn.appshuttle.view.OrdinaryUserBhv;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class UserBhvManager {
	private Set<UserBhv> _totalBhvSet;
	private Set<OrdinaryUserBhv> _ordinaryBhvSet;	//ordinary -> unfavorates && unblocked
	private Set<FavoratesUserBhv> _favoratesBhvSet;
	private Set<BlockedUserBhv> _blockedBhvSet;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		
		_ordinaryBhvSet = new HashSet<OrdinaryUserBhv>();
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
	
	public Set<OrdinaryUserBhv> getOrdinaryBhvSet(){
		return Collections.unmodifiableSet(new HashSet<OrdinaryUserBhv>(_ordinaryBhvSet));
	}
	
	public Set<BlockedUserBhv> getBlockedBhvSetSorted(){
		return Collections.unmodifiableSet(new TreeSet<BlockedUserBhv>(_blockedBhvSet));
	}
	public Set<FavoratesUserBhv> getFavoratesBhvSetSorted(){
		return Collections.unmodifiableSet(new TreeSet<FavoratesUserBhv>(_favoratesBhvSet));
	}
	public synchronized void registerBhv(UserBhv uBhv){
		if(_totalBhvSet.contains(uBhv))
			return ;
//		if(_ordinaryBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoratesBhvSet.contains(uBhv))
//			return ;
		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv);

		_userBhvDao.storeUserBhv(ordinaryUserBhv);

		_totalBhvSet.add(ordinaryUserBhv);
		
		_ordinaryBhvSet.add(ordinaryUserBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!_totalBhvSet.contains(uBhv))
			return ;
//		if(!_ordinaryBhvSet.contains(uBhv) && 
//				!_blockedBhvSet.contains(uBhv) || 
//				!_favoratesBhvSet.contains(uBhv))
//			return ;

		_userBhvDao.deleteUserBhv(uBhv);

		_totalBhvSet.remove(uBhv);
		
		if(_ordinaryBhvSet.contains(uBhv)) {
			_ordinaryBhvSet.remove(uBhv);
			return;
		}
		
		if(_favoratesBhvSet.contains(uBhv)) {
			_favoratesBhvSet.remove(uBhv);
			return;
		}
		if(_blockedBhvSet.contains(uBhv)) {
			_blockedBhvSet.remove(uBhv);
			return;
		}
	}
	
	public synchronized void block(OrdinaryUserBhv uBhv){
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
		
		_ordinaryBhvSet.add(new OrdinaryUserBhv(uBhv.getUserBhv()));
	}
	
	
	public synchronized void favorates(OrdinaryUserBhv uBhv){
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
		
		_ordinaryBhvSet.add(new OrdinaryUserBhv(uBhv.getUserBhv()));
	}
}