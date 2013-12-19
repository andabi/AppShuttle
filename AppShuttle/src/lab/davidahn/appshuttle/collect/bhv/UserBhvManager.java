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
	private Set<UserBhv> totalBhvSet;
	private Set<OrdinaryUserBhv> ordinaryBhvSet;	//ordinary = unfavorite && unblocked
	private Set<FavoriteUserBhv> favoriteBhvSet;
	private Set<BlockedUserBhv> _blockedBhvSet;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		
		ordinaryBhvSet = new HashSet<OrdinaryUserBhv>();
		ordinaryBhvSet.addAll(userBhvDao.retrieveOrdinaryUserBhv());

		favoriteBhvSet = new HashSet<FavoriteUserBhv>();
		favoriteBhvSet.addAll(userBhvDao.retrieveFavoriteUserBhv());
		
		_blockedBhvSet = new HashSet<BlockedUserBhv>();
		_blockedBhvSet.addAll(userBhvDao.retrieveBlockedUserBhv());
		
		totalBhvSet = new HashSet<UserBhv>();
		totalBhvSet.addAll(ordinaryBhvSet);
		totalBhvSet.addAll(favoriteBhvSet);
		totalBhvSet.addAll(_blockedBhvSet);
	}
	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public Set<UserBhv> getTotalBhvSet(){
		return Collections.unmodifiableSet(new HashSet<UserBhv>(totalBhvSet));
	}
	
	public Set<OrdinaryUserBhv> getOrdinaryBhvSet(){
		return Collections.unmodifiableSet(new HashSet<OrdinaryUserBhv>(ordinaryBhvSet));
	}
	
	public Set<BlockedUserBhv> getBlockedBhvSetSorted(){
		return Collections.unmodifiableSet(new TreeSet<BlockedUserBhv>(_blockedBhvSet));
	}
	public Set<FavoriteUserBhv> getFavoriteBhvSetSorted(){
		return Collections.unmodifiableSet(new TreeSet<FavoriteUserBhv>(favoriteBhvSet));
	}
	public synchronized void registerBhv(UserBhv uBhv){
		if(totalBhvSet.contains(uBhv))
			return ;
//		if(_ordinaryBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoriteBhvSet.contains(uBhv))
//			return ;
		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv);

		userBhvDao.storeUserBhv(ordinaryUserBhv);

		totalBhvSet.add(ordinaryUserBhv);
		
		ordinaryBhvSet.add(ordinaryUserBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!totalBhvSet.contains(uBhv))
			return ;
//		if(!_ordinaryBhvSet.contains(uBhv) && 
//				!_blockedBhvSet.contains(uBhv) || 
//				!_favoriteBhvSet.contains(uBhv))
//			return ;

		userBhvDao.deleteUserBhv(uBhv);

		totalBhvSet.remove(uBhv);
		
		if(ordinaryBhvSet.contains(uBhv)) {
			ordinaryBhvSet.remove(uBhv);
			return;
		}
		
		if(favoriteBhvSet.contains(uBhv)) {
			favoriteBhvSet.remove(uBhv);
			return;
		}
		if(_blockedBhvSet.contains(uBhv)) {
			_blockedBhvSet.remove(uBhv);
			return;
		}
	}
	
	public synchronized BlockedUserBhv block(OrdinaryUserBhv uBhv){
		if(_blockedBhvSet.contains(uBhv) || 
				!ordinaryBhvSet.contains(uBhv))
			return null;

		ordinaryBhvSet.remove(uBhv);

		long currTime = System.currentTimeMillis();
		BlockedUserBhv blockedUserBhv = new BlockedUserBhv(uBhv, currTime);
		userBhvDao.block(blockedUserBhv);
		_blockedBhvSet.add(blockedUserBhv);
		
		return blockedUserBhv;
	}
	
	public synchronized OrdinaryUserBhv unblock(BlockedUserBhv uBhv){
		if(!_blockedBhvSet.contains(uBhv) || 
				ordinaryBhvSet.contains(uBhv))
			return null;

		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv.getUserBhv());
		ordinaryBhvSet.add(ordinaryUserBhv);

		userBhvDao.unblock(uBhv);
		_blockedBhvSet.remove(uBhv);
		
		return ordinaryUserBhv;
	}
	
	
	public synchronized FavoriteUserBhv favorite(OrdinaryUserBhv uBhv){
		if(favoriteBhvSet.contains(uBhv) || 
				!ordinaryBhvSet.contains(uBhv))
			return null;

		ordinaryBhvSet.remove(uBhv);

		long currTime = System.currentTimeMillis();
		
		FavoriteUserBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteUserBhv(uBhv, currTime, false);
		
		if(!FavoriteUserBhv.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();
		
		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvSet.add(favoriteUserBhv);
		
		return favoriteUserBhv;
	}
	
	public synchronized OrdinaryUserBhv unfavorite(FavoriteUserBhv uBhv){
		if(!favoriteBhvSet.contains(uBhv) ||
				ordinaryBhvSet.contains(uBhv))
			return null;
		
		OrdinaryUserBhv ordinaryUserBhv = new OrdinaryUserBhv(uBhv.getUserBhv());
		ordinaryBhvSet.add(ordinaryUserBhv);

		uBhv.setUnNotifiable();
//		FavoriteUserBhv.setUnNotifiable(uBhv);
		userBhvDao.unfavorite(uBhv);
		favoriteBhvSet.remove(uBhv);
		
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