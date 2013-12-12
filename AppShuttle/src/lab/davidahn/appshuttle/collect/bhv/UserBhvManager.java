package lab.davidahn.appshuttle.collect.bhv;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import lab.davidahn.appshuttle.view.BlockedUserBhv;
import lab.davidahn.appshuttle.view.FavoriteUserBhv;
import lab.davidahn.appshuttle.view.OrdinaryUserBhv;

/**
 * 
 * @author andabi
 * @thread safe
 */

public class UserBhvManager {
	private Set<UserBhv> _totalBhvSet;
	private Set<OrdinaryUserBhv> _ordinaryBhvSet;	//ordinary -> unfavorite && unblocked
	private Set<FavoriteUserBhv> _favoriteBhvSet;
	private Set<BlockedUserBhv> _blockedBhvSet;
	private UserBhvDao _userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		_userBhvDao = UserBhvDao.getInstance();
		
		_ordinaryBhvSet = new HashSet<OrdinaryUserBhv>();
		_ordinaryBhvSet.addAll(_userBhvDao.retrieveOrdinaryUserBhv());

		_favoriteBhvSet = new HashSet<FavoriteUserBhv>();
		_favoriteBhvSet.addAll(_userBhvDao.retrieveFavoriteUserBhv());
		
		_blockedBhvSet = new HashSet<BlockedUserBhv>();
		_blockedBhvSet.addAll(_userBhvDao.retrieveBlockedUserBhv());
		
		_totalBhvSet = new HashSet<UserBhv>();
		_totalBhvSet.addAll(_ordinaryBhvSet);
		_totalBhvSet.addAll(_favoriteBhvSet);
		_totalBhvSet.addAll(_blockedBhvSet);
	}
	public static UserBhvManager getInstance() {
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
	public Set<FavoriteUserBhv> getFavoriteBhvSetSorted(){
		return Collections.unmodifiableSet(new TreeSet<FavoriteUserBhv>(_favoriteBhvSet));
	}
	public synchronized void registerBhv(UserBhv uBhv){
		if(_totalBhvSet.contains(uBhv))
			return ;
//		if(_ordinaryBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoriteBhvSet.contains(uBhv))
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
//				!_favoriteBhvSet.contains(uBhv))
//			return ;

		_userBhvDao.deleteUserBhv(uBhv);

		_totalBhvSet.remove(uBhv);
		
		if(_ordinaryBhvSet.contains(uBhv)) {
			_ordinaryBhvSet.remove(uBhv);
			return;
		}
		
		if(_favoriteBhvSet.contains(uBhv)) {
			_favoriteBhvSet.remove(uBhv);
			return;
		}
		if(_blockedBhvSet.contains(uBhv)) {
			_blockedBhvSet.remove(uBhv);
			return;
		}
	}
	
	public synchronized BlockedUserBhv block(OrdinaryUserBhv uBhv){
		if(_blockedBhvSet.contains(uBhv) || 
				!_ordinaryBhvSet.contains(uBhv))
			return null;

		_ordinaryBhvSet.remove(uBhv);

		long currTime = System.currentTimeMillis();
		BlockedUserBhv blockedUserBhv = new BlockedUserBhv(uBhv, currTime);
		_userBhvDao.block(blockedUserBhv);
		_blockedBhvSet.add(blockedUserBhv);
		
		return blockedUserBhv;
	}
	
	public synchronized OrdinaryUserBhv unblock(BlockedUserBhv uBhv){
		if(!_blockedBhvSet.contains(uBhv) || 
				_ordinaryBhvSet.contains(uBhv))
			return null;

		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv.getUserBhv());
		_ordinaryBhvSet.add(ordinaryUserBhv);

		_userBhvDao.unblock(uBhv);
		_blockedBhvSet.remove(uBhv);
		
		return ordinaryUserBhv;
	}
	
	
	public synchronized FavoriteUserBhv favorite(OrdinaryUserBhv uBhv){
		if(_favoriteBhvSet.contains(uBhv) || 
				!_ordinaryBhvSet.contains(uBhv))
			return null;

		_ordinaryBhvSet.remove(uBhv);

		long currTime = System.currentTimeMillis();
		
		FavoriteUserBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteUserBhv(uBhv, currTime, false);
		
		if(!FavoriteUserBhv.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();
		
		_userBhvDao.favorite(favoriteUserBhv);
		_favoriteBhvSet.add(favoriteUserBhv);
		
		return favoriteUserBhv;
	}
	
	public synchronized OrdinaryUserBhv unfavorite(FavoriteUserBhv uBhv){
		if(!_favoriteBhvSet.contains(uBhv) ||
				_ordinaryBhvSet.contains(uBhv))
			return null;
		
		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv.getUserBhv());
		_ordinaryBhvSet.add(ordinaryUserBhv);

		uBhv.setUnNotifiable();
//		FavoriteUserBhv.setUnNotifiable(uBhv);
		_userBhvDao.unfavorite(uBhv);
		_favoriteBhvSet.remove(uBhv);
		
		return ordinaryUserBhv;
	}
	
//	public void setFavoriteNofifiable(FavoriteUserBhv favoriteUserBhv) {
//		_userBhvDao.updateNotifiable(favoriteUserBhv);
//	}
//	
//	public void setFavoriteNotNofifiable(FavoriteUserBhv favoriteUserBhv) {
//		_userBhvDao.updateUnNotifiable(favoriteUserBhv);
//	}
}