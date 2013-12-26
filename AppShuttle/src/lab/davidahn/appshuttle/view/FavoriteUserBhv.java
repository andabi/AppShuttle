package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.Predictor;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;


public class FavoriteUserBhv extends ViewableUserBhv implements Comparable<FavoriteUserBhv> {
	private long setTime;
	private boolean isNotifiable;
	
	public FavoriteUserBhv(UserBhv uBhv, long _setTime, boolean _isNotifiable){
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
	public int compareTo(FavoriteUserBhv uBhv) {
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
}