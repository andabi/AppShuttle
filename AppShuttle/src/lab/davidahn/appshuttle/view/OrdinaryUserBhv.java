package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.mine.matcher.MatcherGroupResult;
import lab.davidahn.appshuttle.mine.matcher.MatcherGroupType;
import lab.davidahn.appshuttle.mine.matcher.MatcherGroupTypeComparator;
import lab.davidahn.appshuttle.mine.matcher.PredictionInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;


public class OrdinaryUserBhv extends ViewableUserBhv implements Comparable<OrdinaryUserBhv> {

	private static List<OrdinaryUserBhv> extractedViewListSorted;
	
	public OrdinaryUserBhv(UserBhv uBhv){
		super(uBhv);
	}
	
	@Override
	public int compareTo(OrdinaryUserBhv uBhv) {
		Predictor predictor = Predictor.getInstance();
		double _score = predictor.getPredictionInfo(_uBhv).getScore();
		double score = predictor.getPredictionInfo(uBhv).getScore();

		if(_score < score)
			return 1;
		else if(_score == score)
			return 0;
		else 
			return -1;
	}
	
	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		_viewMsg = msg.toString();

		Predictor predictor = Predictor.getInstance();
		PredictionInfo predictionInfo = predictor.getPredictionInfo(_uBhv);
		
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
	
	public static synchronized void extractViewListSorted() {
		Map<UserBhv, PredictionInfo> predictionInfoMap = Predictor.getInstance().getRecentPredictionInfoMap();
		extractedViewListSorted = new ArrayList<OrdinaryUserBhv>();
		
		if(predictionInfoMap == null)
			return;
		
		Set<OrdinaryUserBhv> ordinaryUserBhvSet = UserBhvManager.getInstance().getOrdinaryBhvSet();
		for(OrdinaryUserBhv ordinaryUserBhv : ordinaryUserBhvSet){
			if(predictionInfoMap.keySet().contains(ordinaryUserBhv))
				extractedViewListSorted.add(ordinaryUserBhv);
		}
		
		Collections.sort(extractedViewListSorted);
	}

	public static synchronized List<OrdinaryUserBhv> getExtractedViewListSorted(int topN) {
		if(extractedViewListSorted == null)
			extractViewListSorted();
		
		return extractedViewListSorted.subList(0, Math.min(extractedViewListSorted.size(), topN));
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_ordinary_container;
	}
}
