package lab.davidahn.appshuttle.bhv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * @thread safe
 */
public class UserBhvManager {
	private Map<UserBhv, ViewableUserBhv> userBhvMap;
	private Set<NormalBhv> normalBhvSet;	//normal = unfavorite && unblocked
	private Set<FavoriteBhv> favoriteBhvSet;
	private Set<BlockedBhv> blockedBhvSet;
	private UserBhvDao userBhvDao;

	private static UserBhvManager userBhvManager = new UserBhvManager();
	private UserBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		userBhvMap = new HashMap<UserBhv, ViewableUserBhv>();
		
		normalBhvSet = new HashSet<NormalBhv>();
		for(NormalBhv normalUserBhv : userBhvDao.retrieveNormalUserBhv()){
			userBhvMap.put(normalUserBhv.getUserBhv(), normalUserBhv);
			normalBhvSet.add(normalUserBhv);
		}

		favoriteBhvSet = new HashSet<FavoriteBhv>();
		for(FavoriteBhv favoriteUserBhv : userBhvDao.retrieveFavoriteUserBhv()){
			userBhvMap.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
			favoriteBhvSet.add(favoriteUserBhv);
		}

		blockedBhvSet = new HashSet<BlockedBhv>();
		for(BlockedBhv blockedUserBhv : userBhvDao.retrieveBlockedUserBhv()){
			userBhvMap.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
			blockedBhvSet.add(blockedUserBhv);
		}
	}
	public static UserBhvManager getInstance() {
		return userBhvManager;
	}
	
	public ViewableUserBhv getViewableUserBhv(UserBhv uBhv){
		return userBhvMap.get(uBhv);
	}
	
	public Set<UserBhv> getBhvSet(){
		return new HashSet<UserBhv>(userBhvMap.keySet());
	}
	
	public Set<NormalBhv> getNormalBhvSet(){
		return new HashSet<NormalBhv>(normalBhvSet);
	}
	
	public Set<FavoriteBhv> getFavoriteBhvSet(){
		return new HashSet<FavoriteBhv>(favoriteBhvSet);
	}
	
	public Set<BlockedBhv> getBlockedBhvSet(){
		return new HashSet<BlockedBhv>(blockedBhvSet);
	}
	
	public synchronized void registerBhv(UserBhv uBhv){
		if(userBhvMap.keySet().contains(uBhv))
			return ;

		NormalBhv normalUserBhv = new NormalBhv(uBhv);

		userBhvDao.storeUserBhv(normalUserBhv);

		userBhvMap.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		normalBhvSet.add(normalUserBhv);
	}
	
	public synchronized void unregisterBhv(UserBhv uBhv){
		if(!userBhvMap.containsKey(uBhv))
			return ;

		userBhvDao.deleteUserBhv(uBhv);

		userBhvMap.remove(uBhv);
		
		if(normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
			return;
		}
		if(favoriteBhvSet.contains(uBhv)) {
			favoriteBhvSet.remove(uBhv);
			return;
		}
		if(blockedBhvSet.contains(uBhv)) {
			blockedBhvSet.remove(uBhv);
			return;
		}
	}
	
	public synchronized BlockedBhv block(ViewableUserBhv uBhv){
		if(blockedBhvSet.contains(uBhv))
			return null;

		if(normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
		} else if(favoriteBhvSet.contains(uBhv)) {
			favoriteBhvSet.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		BlockedBhv blockedUserBhv = new BlockedBhv(uBhv, currTime);

		userBhvDao.block(blockedUserBhv);
		blockedBhvSet.add(blockedUserBhv);

		userBhvMap.put(blockedUserBhv.getUserBhv(), blockedUserBhv);
		
		return blockedUserBhv;
	}
	
	public synchronized NormalBhv unblock(BlockedBhv uBhv){
		if(!blockedBhvSet.contains(uBhv))
			return null;

		NormalBhv normalUserBhv = new NormalBhv(uBhv.getUserBhv());
		normalBhvSet.add(normalUserBhv);

		userBhvDao.unblock(uBhv);
		blockedBhvSet.remove(uBhv);
		
		userBhvMap.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		return normalUserBhv;
	}
	
	
	public synchronized FavoriteBhv favorite(ViewableUserBhv uBhv){
		if(favoriteBhvSet.contains(uBhv))
			return null;

		if(normalBhvSet.contains(uBhv)) {
			normalBhvSet.remove(uBhv);
		} else if(blockedBhvSet.contains(uBhv)) {
			blockedBhvSet.remove(uBhv);
		}
		
		long currTime = System.currentTimeMillis();
		
		FavoriteBhv favoriteUserBhv;
		favoriteUserBhv = new FavoriteBhv(uBhv, currTime, false);
		
		if(!FavoriteBhvManager.isFullProperNumFavorite())
			favoriteUserBhv.trySetNotifiable();
		
		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvSet.add(favoriteUserBhv);
		
		userBhvMap.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
		
		return favoriteUserBhv;
	}
	
	public synchronized NormalBhv unfavorite(FavoriteBhv uBhv){
		if(!favoriteBhvSet.contains(uBhv))
				return null;
		
		NormalBhv normalUserBhv = new NormalBhv(uBhv.getUserBhv());
		normalBhvSet.add(normalUserBhv);

		uBhv.setUnNotifiable();
		userBhvDao.unfavorite(uBhv);
		favoriteBhvSet.remove(uBhv);
		
		userBhvMap.put(normalUserBhv.getUserBhv(), normalUserBhv);
		
		return normalUserBhv;
	}
}