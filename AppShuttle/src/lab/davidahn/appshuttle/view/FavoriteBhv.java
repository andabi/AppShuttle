package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.SharedPreferences;
import android.text.format.DateUtils;
import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;

public class FavoriteBhv extends ViewableUserBhv implements Comparable<FavoriteBhv> {
	private long setTime;
	private boolean isNotifiable;
	
	public FavoriteBhv(UserBhv uBhv, long _setTime, boolean _isNotifiable){
		super(uBhv);
		setTime = _setTime;
		isNotifiable = false;

		if(_isNotifiable)
			trySetNotifiable();
	}

	public long getSetTime() {
		return setTime;
	}
	
	public boolean isNotifiable() {
		return isNotifiable;
	}

	public boolean trySetNotifiable() {
		if(isNotifiable)
			return true;
		
		int notiMaxNumFavorite = AppShuttleApplication.getContext().getPreferences().getInt("viewer.noti.max_num_favorite", 3);
		if(AppShuttleApplication.numFavoriteNotifiable < notiMaxNumFavorite) {
			isNotifiable = true;
			AppShuttleApplication.numFavoriteNotifiable++;
			return true;
		} else {
			return false;
		}
	}
	
	public void setUnNotifiable() {
		if(!isNotifiable)
			return ;
		
		isNotifiable = false;
		AppShuttleApplication.numFavoriteNotifiable--;
	}
	
	@Override
	public String getViewMsg() {
//		long blockedTime = ((BlockedUserBhv)_uBhv).getBlockedTime();
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		
		msg.append(DateUtils.getRelativeTimeSpanString(setTime, 
				System.currentTimeMillis(), 
				DateUtils.MINUTE_IN_MILLIS, 
				0
				));
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public Integer getNotibarContainerId() {
		if(isNotifiable())
			return R.id.noti_favorite_container;
		else
			return R.id.noti_present_container;
	}

	@Override
	public int compareTo(FavoriteBhv uBhv) {
		if(!isNotifiable() && uBhv.isNotifiable())
			return 1;
		else if(isNotifiable() && !uBhv.isNotifiable())
			return -1;
			
		if(setTime > uBhv.setTime)
			return 1;
		else if(setTime == uBhv.setTime)
			return 0;
		else
			return -1;
	}
	
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