package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.Predictor;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;


public class OrdinaryUserBhv extends ViewableUserBhv {

	public OrdinaryUserBhv(UserBhv uBhv){
		super(uBhv);
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_predicted_container;
	}

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();

		Predictor predictor = Predictor.getInstance();
		PredictionInfo predictionInfo = predictor.getRecentSnapshotPredictionInfo(uBhv);
		
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
	
}
