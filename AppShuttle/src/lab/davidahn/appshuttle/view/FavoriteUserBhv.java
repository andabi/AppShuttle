package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.Predictor;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;
import android.content.SharedPreferences;


public class FavoriteUserBhv extends ViewableUserBhv implements Comparable<FavoriteUserBhv> {
	private long _setTime;
	private boolean _isNotifiable;
	
//	public FavoriteUserBhv(UserBhv uBhv, long setTime){
//		super(uBhv);
//		_setTime = setTime;
//		_isNotifiable = false;
//	}

	public FavoriteUserBhv(UserBhv uBhv, long setTime, boolean isNotifiable){
		super(uBhv);
		_setTime = setTime;
		_isNotifiable = false;

		if(isNotifiable)
			trySetNotifiable();
	}

	public long getSetTime() {
		return _setTime;
	}
	
	public boolean isNotifiable() {
		return _isNotifiable;
	}

	public boolean trySetNotifiable() {
		if(_isNotifiable)
			return true;
		
		int notiMaxNumFavorite = AppShuttleApplication.getContext().getPreferences().getInt("viewer.noti.max_num_favorite", 3);
		if(AppShuttleApplication.numFavoriteNotifiable < notiMaxNumFavorite) {
			_isNotifiable = true;
			AppShuttleApplication.numFavoriteNotifiable++;
			return true;
		} else {
			return false;
		}
	}
	
	public void setUnNotifiable() {
		if(!_isNotifiable)
			return ;
		
		_isNotifiable = false;
		AppShuttleApplication.numFavoriteNotifiable--;
	}

	@Override
	public int compareTo(FavoriteUserBhv uBhv) {
		if(!isNotifiable() && uBhv.isNotifiable())
			return 1;
		else if(isNotifiable() && !uBhv.isNotifiable())
			return -1;
			
		if(_setTime > uBhv._setTime)
			return 1;
		else if(_setTime == uBhv._setTime)
			return 0;
		else
			return -1;
	}

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();

		Predictor predictor = Predictor.getInstance();
		PredictionInfo predictionInfo = predictor.getCurrentPredictionInfo(uBhv);
		
		if(predictionInfo == null) {
			return viewMsg;
		}
		
		Map<MatcherGroupType, MatcherGroupResult> macherGroupResults = predictionInfo.getMatcherGroupResultMap();
		List<MatcherGroupType> matcherGroupTypeList = new ArrayList<MatcherGroupType>(macherGroupResults.keySet());
		Collections.sort(matcherGroupTypeList, new MatcherGroupTypeComparator());
		
		for (MatcherGroupType matcherGroupType : matcherGroupTypeList) {
			msg.append(macherGroupResults.get(matcherGroupType).getViewMsg()).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_favorite_container;
	}
	
	public static List<FavoriteUserBhv> getNotifiableFavoriteBhvList() {
		List<FavoriteUserBhv> res = new ArrayList<FavoriteUserBhv>();
		for(FavoriteUserBhv uBhv : UserBhvManager.getInstance().getFavoriteBhvSetSorted()) {
			if(uBhv.isNotifiable())
				res.add(uBhv);
		}
		return res;
	}
	
	public synchronized static boolean trySetNotifiable(FavoriteUserBhv favoriteUserBhv) {
		if(favoriteUserBhv.trySetNotifiable()) {
			UserBhvDao.getInstance().updateNotifiable(favoriteUserBhv);
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized static void setUnNotifiable(FavoriteUserBhv favoriteUserBhv) {
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