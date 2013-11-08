package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.mine.matcher.MatcherType;
import lab.davidahn.appshuttle.mine.matcher.MatcherTypeComparator;
import lab.davidahn.appshuttle.mine.matcher.PredictionInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;


public class FavoratesUserBhv extends ViewableUserBhv implements Comparable<FavoratesUserBhv> {
	private long _setTime;
	private boolean _isNotifiable;
	
	public FavoratesUserBhv(UserBhv uBhv, long setTime){
		super(uBhv);
		_setTime = setTime;
		_isNotifiable = false;
		
		trySetNotifiable();
	}
	
//	public FavoratesUserBhv(UserBhv uBhv, long setTime, boolean isNotifiable){
//		super(uBhv);
//		_setTime = setTime;
//		_isNotifiable = isNotifiable;
//	}

	public long getSetTime() {
		return _setTime;
	}
	
	public boolean isNotifiable() {
		return _isNotifiable;
	}

	private boolean trySetNotifiable() {
		if(_isNotifiable)
			return true;
		
		int notiMaxNumFavorates = AppShuttleApplication.getContext().getPreferences().getInt("viewer.noti.max_num_favorates", 3);
		if(AppShuttleApplication.currNumFavoratesNotifiable < notiMaxNumFavorates) {
			_isNotifiable = true;
			AppShuttleApplication.currNumFavoratesNotifiable++;
			return true;
		} else {
			return false;
		}
	}
	
	private void setNotNotifiable() {
		if(!_isNotifiable)
			return ;
		
		_isNotifiable = false;
		AppShuttleApplication.currNumFavoratesNotifiable--;
	}

	@Override
	public int compareTo(FavoratesUserBhv uBhv) {
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
		PredictionInfo predictedBhv = predictor.getPredictionInfo(_uBhv);
		
		if(predictedBhv == null) {
			return _viewMsg;
		}
		
		List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(predictedBhv.getMatchedResultMap().keySet());
		Collections.sort(matcherTypeList, new MatcherTypeComparator());
		
		for (MatcherType matcherType : matcherTypeList) {
			msg.append(matcherType.viewMsg).append(", ");
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
		return favoratesUserBhv.trySetNotifiable();
	}
	
	public synchronized static void setNotNotifiable(FavoratesUserBhv favoratesUserBhv) {
		favoratesUserBhv.setNotNotifiable();
	}
}
