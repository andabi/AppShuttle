package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.Predictor;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;
import android.content.SharedPreferences;


public class FavoratesUserBhv extends ViewableUserBhv implements Comparable<FavoratesUserBhv> {
	private long _setTime;
	private boolean _isNotifiable;
	
//	public FavoratesUserBhv(UserBhv uBhv, long setTime){
//		super(uBhv);
//		_setTime = setTime;
//		_isNotifiable = false;
//	}

	public FavoratesUserBhv(UserBhv uBhv, long setTime, boolean isNotifiable){
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
		
		int notiMaxNumFavorates = AppShuttleApplication.getContext().getPreferences().getInt("viewer.noti.max_num_favorates", 3);
		if(AppShuttleApplication.numFavoratesNotifiable < notiMaxNumFavorates) {
			_isNotifiable = true;
			AppShuttleApplication.numFavoratesNotifiable++;
			return true;
		} else {
			return false;
		}
	}
	
	public void setUnNotifiable() {
		if(!_isNotifiable)
			return ;
		
		_isNotifiable = false;
		AppShuttleApplication.numFavoratesNotifiable--;
	}

	@Override
	public int compareTo(FavoratesUserBhv uBhv) {
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
		_viewMsg = msg.toString();

		Predictor predictor = Predictor.getInstance();
		PredictionInfo predictionInfo = predictor.getRecentPredictionInfo(_uBhv);
		
		if(predictionInfo == null) {
			return _viewMsg;
		}
		
		Map<MatcherGroupType, MatcherGroupResult> macherGroupResults = predictionInfo.getMatcherGroupResultMap();
		List<MatcherGroupType> matcherGroupTypeList = new ArrayList<MatcherGroupType>(macherGroupResults.keySet());
		Collections.sort(matcherGroupTypeList, new MatcherGroupTypeComparator());
		
		for (MatcherGroupType matcherGroupType : matcherGroupTypeList) {
			msg.append(macherGroupResults.get(matcherGroupType).getViewMsg()).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		_viewMsg = msg.toString();
		
		return _viewMsg;
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_favorates_container;
	}
	
	public static List<FavoratesUserBhv> getNotifiableFavoratesBhvList() {
		List<FavoratesUserBhv> res = new ArrayList<FavoratesUserBhv>();
		for(FavoratesUserBhv uBhv : UserBhvManager.getInstance().getFavoratesBhvSetSorted()) {
			if(uBhv.isNotifiable())
				res.add(uBhv);
		}
		return res;
	}
	
	public synchronized static boolean trySetNotifiable(FavoratesUserBhv favoratesUserBhv) {
		if(favoratesUserBhv.trySetNotifiable()) {
			UserBhvDao.getInstance().updateNotifiable(favoratesUserBhv);
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized static void setUnNotifiable(FavoratesUserBhv favoratesUserBhv) {
		favoratesUserBhv.setUnNotifiable();
		UserBhvDao.getInstance().updateUnNotifiable(favoratesUserBhv);
	}

	public static boolean isFullProperNumFavorates() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		int properNumFavorates = preferences.getInt("viewer.noti.proper_num_favorates", 3);

		if(AppShuttleApplication.numFavoratesNotifiable >= properNumFavorates)
			return true;
		
		return false;
	}

	public static int getProperNumFavorates() {
		SharedPreferences preferences = AppShuttleApplication.getContext().getPreferences();
		return preferences.getInt("viewer.noti.proper_num_favorates", 3);
	}
}