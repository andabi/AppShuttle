package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.view.BlockedUserBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvFragment;
import lab.davidahn.appshuttle.view.FavoriteUserBhv;
import lab.davidahn.appshuttle.view.NormalUserBhv;
import lab.davidahn.appshuttle.view.ViewableUserBhv;

public class UserBhvManager {
	private Map<UserBhv, ViewableUserBhv> viewableUserBhvs;
	private Set<NormalUserBhv> normalBhvs;	//normal = unfavorite && unblocked
	private Set<FavoriteUserBhv> favoriteBhvs;
	private Set<BlockedUserBhv> blockedBhvs;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		viewableUserBhvs = new HashMap<UserBhv, ViewableUserBhv>();
		
		normalBhvs = new HashSet<NormalUserBhv>();
		for(NormalUserBhv normalUserBhv : userBhvDao.retrieveNormalUserBhv()){
			viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
			normalBhvs.add(normalUserBhv);
		}

		favoriteBhvs = new HashSet<FavoriteUserBhv>();
		for(FavoriteUserBhv favoriteUserBhv : userBhvDao.retrieveFavoriteUserBhv()){
			viewableUserBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
			favoriteBhvs.add(favoriteUserBhv);
		}

		blockedBhvs = new HashSet<BlockedUserBhv>();
		for(BlockedUserBhv blockedUserBhv : userBhvDao.retrieveBlockedUserBhv()){
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
		return viewableUserBhvs.keySet();
	}
	
	public Set<NormalUserBhv> getNormalBhvSet(){
		return normalBhvs;
	}
	
	public Set<FavoriteUserBhv> getFavoriteBhvSet(){
		return favoriteBhvs;
	}
	
	public Set<BlockedUserBhv> getBlockedBhvSet(){
		return blockedBhvs;
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(viewableUserBhvs.keySet().contains(uBhv))
			return ;
//		if(_normalBhvSet.contains(uBhv) || 
//				_blockedBhvSet.contains(uBhv) || 
//				_favoriteBhvSet.contains(uBhv))
//			return ;
		NormalUserBhv normalUserBhv = new NormalUserBhv(uBhv);

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
	
	public synchronized BlockedUserBhv block(ViewableUserBhv uBhv){
		if(blockedBhvs.contains(uBhv))
			return null;

		if(normalBhvs.contains(uBhv)) {
			normalBhvs.remove(uBhv);
		} else if(favoriteBhvs.contains(uBhv)) {
			favoriteBhvs.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		BlockedUserBhv blockedUserBhv = new BlockedUserBhv(uBhv, currTime);

		userBhvDao.block(blockedUserBhv);
		blockedBhvs.add(blockedUserBhv);

		viewableUserBhvs.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
		
		return blockedUserBhv;
	}
	
	public synchronized NormalUserBhv unblock(BlockedUserBhv uBhv){
		if(!blockedBhvs.contains(uBhv))
			return null;

		NormalUserBhv normalUserBhv = new NormalUserBhv(uBhv.getUserBhv());
		normalBhvs.add(normalUserBhv);

		userBhvDao.unblock(uBhv);
		blockedBhvs.remove(uBhv);
		
		viewableUserBhvs.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		return normalUserBhv;
	}
	
	
	public synchronized FavoriteUserBhv favorite(ViewableUserBhv uBhv){
		if(favoriteBhvs.contains(uBhv))
			return null;

		if(normalBhvs.contains(uBhv)) {
			normalBhvs.remove(uBhv);
		} else if(blockedBhvs.contains(uBhv)) {
			blockedBhvs.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		
		FavoriteUserBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteUserBhv(uBhv, currTime, false);
		
		if(!FavoriteBhvFragment.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();
		
		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvs.add(favoriteUserBhv);
		
		viewableUserBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
		
		return favoriteUserBhv;
	}
	
	public synchronized NormalUserBhv unfavorite(FavoriteUserBhv uBhv){
		if(!favoriteBhvs.contains(uBhv))
				return null;
		
		NormalUserBhv normalUserBhv = new NormalUserBhv(uBhv.getUserBhv());
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