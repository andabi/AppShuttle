package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvFragment;
import lab.davidahn.appshuttle.view.NormalBhv;
import lab.davidahn.appshuttle.view.ViewableUserBhv;

/*
 * Thread-safe
 */
public class UserBhvManager {
	private Map<UserBhv, ViewableUserBhv> viewableUserBhvs;
	private Set<NormalBhv> normalBhvs;	//normal = unfavorite && unblocked
	private Set<FavoriteBhv> favoriteBhvs;
	private Set<BlockedBhv> blockedBhvs;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		viewableUserBhvs = new HashMap<UserBhv, ViewableUserBhv>();
		
		normalBhvs = new HashSet<NormalBhv>();
		for(NormalBhv normalUserBhv : userBhvDao.retrieveNormalUserBhv()){
			viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
			normalBhvs.add(normalUserBhv);
		}

		favoriteBhvs = new HashSet<FavoriteBhv>();
		for(FavoriteBhv favoriteUserBhv : userBhvDao.retrieveFavoriteUserBhv()){
			viewableUserBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
			favoriteBhvs.add(favoriteUserBhv);
		}

		blockedBhvs = new HashSet<BlockedBhv>();
		for(BlockedBhv blockedUserBhv : userBhvDao.retrieveBlockedUserBhv()){
			viewableUserBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
			blockedBhvs.add(blockedUserBhv);
		}
	}
	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public ViewableUserBhv getViewableUserBhv(UserBhv uBhv){
		return viewableUserBhvs.get(uBhv);
	}
	
	public Set<UserBhv> getBhvSet(){
		return new HashSet<UserBhv>(viewableUserBhvs.keySet());
//		return Collections.unmodifiableSet(viewableUserBhvs.keySet());
	}
	
	public Set<NormalBhv> getNormalBhvSet(){
		return new HashSet<NormalBhv>(normalBhvs);
	}
	
	public Set<FavoriteBhv> getFavoriteBhvSet(){
		return new HashSet<FavoriteBhv>(favoriteBhvs);
	}
	
	public Set<BlockedBhv> getBlockedBhvSet(){
		return new HashSet<BlockedBhv>(blockedBhvs);
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(viewableUserBhvs.keySet().contains(uBhv))
			return ;
//		if(_normalBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoriteBhvSet.contains(uBhv))
//			return ;
		NormalBhv normalUserBhv = new NormalBhv(uBhv);

		userBhvDao.storeUserBhv(normalUserBhv);

		viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		normalBhvs.add(normalUserBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!viewableUserBhvs.keySet().contains(uBhv))
			return ;
//		if(!_normalBhvSet.contains(uBhv) && 
//				!_blockedBhvSet.contains(uBhv) || 
//				!_favoriteBhvSet.contains(uBhv))
//			return ;

		userBhvDao.deleteUserBhv(uBhv);

		viewableUserBhvs.remove(uBhv);
		
		if(normalBhvs.contains(uBhv)) {
			normalBhvs.remove(uBhv);
			return;
		}
		if(favoriteBhvs.contains(uBhv)) {
			favoriteBhvs.remove(uBhv);
			return;
		}
		if(blockedBhvs.contains(uBhv)) {
			blockedBhvs.remove(uBhv);
			return;
		}
	}
	
	public synchronized BlockedBhv block(ViewableUserBhv uBhv){
		if(blockedBhvs.contains(uBhv))
			return null;

		if(normalBhvs.contains(uBhv)) {
			normalBhvs.remove(uBhv);
		} else if(favoriteBhvs.contains(uBhv)) {
			favoriteBhvs.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		BlockedBhv blockedUserBhv = new BlockedBhv(uBhv, currTime);

		userBhvDao.block(blockedUserBhv);
		blockedBhvs.add(blockedUserBhv);

		viewableUserBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
		
		return blockedUserBhv;
	}
	
	public synchronized NormalBhv unblock(BlockedBhv uBhv){
		if(!blockedBhvs.contains(uBhv))
			return null;

		NormalBhv normalUserBhv = new NormalBhv(uBhv.getUserBhv());
		normalBhvs.add(normalUserBhv);

		userBhvDao.unblock(uBhv);
		blockedBhvs.remove(uBhv);
		
		viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		return normalUserBhv;
	}
	
	
	public synchronized FavoriteBhv favorite(ViewableUserBhv uBhv){
		if(favoriteBhvs.contains(uBhv))
			return null;

		if(normalBhvs.contains(uBhv)) {
			normalBhvs.remove(uBhv);
		} else if(blockedBhvs.contains(uBhv)) {
			blockedBhvs.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		
		FavoriteBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteBhv(uBhv, currTime, false);
		
		if(!FavoriteBhvFragment.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();
		
		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvs.add(favoriteUserBhv);
		
		viewableUserBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
		
		return favoriteUserBhv;
	}
	
	public synchronized NormalBhv unfavorite(FavoriteBhv uBhv){
		if(!favoriteBhvs.contains(uBhv))
				return null;
		
		NormalBhv normalUserBhv = new NormalBhv(uBhv.getUserBhv());
		normalBhvs.add(normalUserBhv);

		uBhv.setUnNotifiable();
		userBhvDao.unfavorite(uBhv);
		favoriteBhvs.remove(uBhv);
		
		viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		return normalUserBhv;
	}
	
//	public void setFavoriteNofifiable(FavoriteUserBhv favoriteUserBhv) {
//		_userBhvDao.updateNotifiable(favoriteUserBhv);
//	}
//	
//	public void setFavoriteNotNofifiable(FavoriteUserBhv favoriteUserBhv) {
//		_userBhvDao.updateUnNotifiable(favoriteUserBhv);
//	}
}