package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvDao;
import android.content.SharedPreferences;
import android.util.Log;

/*
 * @thread safe
 */
public class FavoriteBhvManager {
	private Map<UserBhv, FavoriteBhv> favoriteBhvs;
	private UserBhvDao userBhvDao;

	private static FavoriteBhvManager favoriteBhvManager = new FavoriteBhvManager();

	private FavoriteBhvManager() {
		userBhvDao = UserBhvDao.getInstance();
		favoriteBhvs = new HashMap<UserBhv, FavoriteBhv>();
		updateFavoriteBhv();
	}

	public static FavoriteBhvManager getInstance() {
		return favoriteBhvManager;
	}

	public synchronized Set<FavoriteBhv> getFavoriteBhvSet() {
		return new HashSet<FavoriteBhv>(favoriteBhvs.values());
	}
	
	public synchronized Set<FavoriteBhv> getNotifiableFavoriteBhvSet() {
		Set<FavoriteBhv> res = new HashSet<FavoriteBhv>();
		for(FavoriteBhv bhv : getFavoriteBhvSet())
			if(bhv.isNotifiable())
				res.add(bhv);
		return res;
	}

	public synchronized FavoriteBhv getFavoriteBhv(UserBhv uBhv) {
		return favoriteBhvs.get(uBhv);
	}

	private void updateFavoriteBhv(){
		favoriteBhvs.clear();
		for (FavoriteBhv favoriteUserBhv : userBhvDao.retrieveFavoriteUserBhv())
			favoriteBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);
	}
	
	public synchronized FavoriteBhv favorite(UserBhv uBhv) {
		if (favoriteBhvs.containsKey(uBhv))
			return null;

		long currTime = System.currentTimeMillis();
		int order = 1;
		List<FavoriteBhv> favoriteBhvListSorted = getFavoriteBhvListSorted();
		if(!favoriteBhvListSorted.isEmpty()) 
			order = favoriteBhvListSorted.get(favoriteBhvListSorted.size() - 1).getOrder() + 1;
		FavoriteBhv favoriteUserBhv = new FavoriteBhv(uBhv, currTime, false, order);
		
		if (!isFullProperNumFavorite())
			FavoriteBhvManager.getInstance().trySetNotifiable(favoriteUserBhv);

		userBhvDao.favorite(favoriteUserBhv);
		favoriteBhvs.put(favoriteUserBhv.getUserBhv(), favoriteUserBhv);

		Log.d("test", favoriteBhvs.size()+"");
		
		return favoriteUserBhv;
	}

	public synchronized void unfavorite(FavoriteBhv uBhv) {
		if (!favoriteBhvs.containsKey(uBhv))
			return;
		FavoriteBhvManager.getInstance().setUnNotifiable(uBhv);
		userBhvDao.unfavorite(uBhv);
		favoriteBhvs.remove(uBhv);
	}
	
	public synchronized boolean trySetNotifiable(FavoriteBhv favoriteUserBhv) {
		if(favoriteUserBhv.isNotifiable())
			return true;
		if(!isFullProperNumFavorite()) {
			favoriteUserBhv.setNotifiable(true);
//			AppShuttleApplication.numFavoriteNotifiable++;
			UserBhvDao.getInstance().updateNotifiable(favoriteUserBhv);
			return true;
		}
		return false;
	}

	public synchronized void setUnNotifiable(FavoriteBhv favoriteUserBhv) {
		if(!favoriteUserBhv.isNotifiable())
			return ;
		favoriteUserBhv.setNotifiable(false);
//		AppShuttleApplication.numFavoriteNotifiable--;
		UserBhvDao.getInstance().updateUnNotifiable(favoriteUserBhv);
	}

	public synchronized List<FavoriteBhv> getNotifiableFavoriteBhvList() {
		List<FavoriteBhv> res = new ArrayList<FavoriteBhv>();
		for(FavoriteBhv uBhv : getFavoriteBhvListSorted()) {
			if(uBhv.isNotifiable())
				res.add(uBhv);
		}
		return res;
	}
	
	public void updateOrder(FavoriteBhv favoriteUserBhv, int order) {
		favoriteUserBhv.setOrder(order);
		UserBhvDao.getInstance().updateOrder(favoriteUserBhv, order);
	}
	
	public synchronized List<FavoriteBhv> getFavoriteBhvListSorted(){
		updateFavoriteBhv();
		List<FavoriteBhv> favorateBhvList = new ArrayList<FavoriteBhv>(getFavoriteBhvSet());
		Collections.sort(favorateBhvList);
		return Collections.unmodifiableList(favorateBhvList);
	}

	public boolean isFullProperNumFavorite() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		int properNumFavorite = preferences.getInt("viewer.noti.proper_num_favorite", 3);
	
		if(getNotifiableFavoriteBhvSet().size() >= properNumFavorite)
			return true;
		
		return false;
	}
	
	public int getProperNumFavorite() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		return preferences.getInt("viewer.noti.proper_num_favorite", 3);
	}
}