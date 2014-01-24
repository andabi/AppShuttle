package lab.davidahn.appshuttle.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;

public class FavoriteBhvManager {

	public static List<FavoriteBhv> getFavoriteBhvListSorted(){
		List<FavoriteBhv> favorateBhvList = new ArrayList<FavoriteBhv>(
				UserBhvManager.getInstance().getFavoriteBhvSet());
		Collections.sort(favorateBhvList);
		return Collections.unmodifiableList(favorateBhvList);
	}

	public static List<FavoriteBhv> getNotifiableFavoriteBhvList() {
		List<FavoriteBhv> res = new ArrayList<FavoriteBhv>();
		for(FavoriteBhv uBhv : getFavoriteBhvListSorted()) {
			if(uBhv.isNotifiable())
				res.add(uBhv);
		}
		return res;
	}

	public synchronized static boolean trySetNotifiable(FavoriteBhv favoriteUserBhv) {
		if(favoriteUserBhv.trySetNotifiable()) {
			UserBhvDao.getInstance().updateNotifiable(favoriteUserBhv);
			return true;
		} else {
			return false;
		}
	}

	public synchronized static void setUnNotifiable(FavoriteBhv favoriteUserBhv) {
		favoriteUserBhv.setUnNotifiable();
		UserBhvDao.getInstance().updateUnNotifiable(favoriteUserBhv);
	}

	public static boolean isFullProperNumFavorite() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		int properNumFavorite = preferences.getInt("viewer.noti.proper_num_favorite", 3);
	
		if(AppShuttleApplication.numFavoriteNotifiable >= properNumFavorite)
			return true;
		
		return false;
	}

	public static int getProperNumFavorite() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		return preferences.getInt("viewer.noti.proper_num_favorite", 3);
	}
	

}