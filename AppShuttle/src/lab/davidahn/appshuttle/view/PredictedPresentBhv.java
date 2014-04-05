package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.predict.matcher.MatcherTypeComparator;

public class PredictedPresentBhv extends PresentBhv implements
		Comparable<PredictedPresentBhv> {
	public static List<PredictedPresentBhv> predictedPresentBhvList = new ArrayList<PredictedPresentBhv>();
	
	private EnumMap<MatcherType, PredictedBhv> matchersWithPredictionInfos;

	public PredictedPresentBhv(UserBhv uBhv) {
		super(uBhv);
		matchersWithPredictionInfos = new EnumMap<MatcherType, PredictedBhv>(
				MatcherType.class);
	}

	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.PREDICTED;
	}

	public EnumMap<MatcherType, PredictedBhv> getMatchersWithPredictionInfos() {
		return matchersWithPredictionInfos;
	}
	
	public void setMatchersWithPredictionInfos(
			EnumMap<MatcherType, PredictedBhv> _predictedBhvByMatcherType) {
		matchersWithPredictionInfos = _predictedBhvByMatcherType;
	}
	
	public List<MatcherType> getFinalMatchers(){
		PredictedBhv predictedBhv = PredictedBhv.getPredictedBhv(uBhv);
		if(predictedBhv == null)
			return Collections.emptyList();

		List<MatcherType> res = new ArrayList<MatcherType>();
		for(MatcherResultElem resultElem : predictedBhv.getMatchersWithResult().values())
			res.add(resultElem.getFinalMatcher());
		return res;
	}
	
	public PredictedBhv getPredictionInfos(MatcherType matcherType) {
		return matchersWithPredictionInfos.get(matcherType);
	}

	public void setPredictionInfos(MatcherType matcherType,
			PredictedBhv predictedBhv) {
		matchersWithPredictionInfos.put(matcherType, predictedBhv);
	}

	public PredictedBhv getRecentPredictedBhv() {
		if (matchersWithPredictionInfos.isEmpty())
			return null;
		return Collections.max(matchersWithPredictionInfos.values());
	}
	
	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();

		List<MatcherType> matchers = getFinalMatchers();
		Collections.sort(matchers, new MatcherTypeComparator());
		Collections.reverse(matchers);
		
		for (MatcherType matcher : matchers) {
			msg.append(matcher.viewMsg).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public int compareTo(PredictedPresentBhv _uBhv) {
		long recentPredictedBhvTime = getRecentPredictedBhv().getTime();
		long _recentPredictedBhvTime = _uBhv.getRecentPredictedBhv()
				.getTime();
		if(recentPredictedBhvTime > _recentPredictedBhvTime) return 1;
		else if(recentPredictedBhvTime < _recentPredictedBhvTime) return -1;
		else {
			double score = getRecentPredictedBhv().getScore();
			double _score = _uBhv.getRecentPredictedBhv().getScore();
			if (score > _score) return 1;
			else if (score == _score) return 0;
			else return -1;
		}
	}

	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer();
		msg.append("PredictedBhvByMatcherType: ").append(
				matchersWithPredictionInfos.toString());
		return msg.toString();
	}
	
	public static PredictedPresentBhv getPredictedPresentBhv(UserBhv bhv) {
		return AppShuttleApplication.predictedPresentBhvMap.get(bhv);
	}

	public static List<PredictedPresentBhv> getPredictedPresentBhvList() {
		return new ArrayList<PredictedPresentBhv>(AppShuttleApplication.predictedPresentBhvMap.values());
	}

	public static void updatePredictedPresentBhvList(List<PredictedPresentBhv> list) {
		Map<UserBhv, PredictedPresentBhv> map = new HashMap<UserBhv, PredictedPresentBhv>();
		for (PredictedPresentBhv bhv : list)
			map.put(bhv.getUserBhv(), bhv);
		AppShuttleApplication.predictedPresentBhvMap = map;
	}
	
//	public static void updatePredictedPresentBhv(PredictedPresentBhv bhv) {
//		AppShuttleApplication.predictedPresentBhvMap.put(bhv.getUserBhv(), bhv);
//	}

	public static List<PredictedPresentBhv> getPredictedPresentBhvListSorted() {
		Collections.sort(predictedPresentBhvList, Collections.reverseOrder());
		return predictedPresentBhvList;
	}

	public static void extractPredictedPresentBhvList() {
		if (PredictedBhv.getPredictedBhvList().isEmpty())
			return ;

		List<PredictedPresentBhv> extractedPredictedPresentBhvList = new ArrayList<PredictedPresentBhv>();
		for (PredictedBhv predictedBhv : PredictedBhv.getPredictedBhvList()) {
			PredictedPresentBhv prevPresentBhv = getPredictedPresentBhv(predictedBhv);
			PredictedPresentBhv currPresentBhv = new PredictedPresentBhv(predictedBhv);
			for (MatcherType matcher : predictedBhv.getMatchersWithResult().keySet()) {
				if (prevPresentBhv == null) {
					currPresentBhv.setPredictionInfos(matcher, predictedBhv);
				} else {
					PredictedBhv prevPredictedBhv = prevPresentBhv.getPredictionInfos(matcher);
					if (prevPredictedBhv == null) {
						currPresentBhv.setPredictionInfos(matcher, predictedBhv);
					} else {
						MatcherType finalMatcher = 
								predictedBhv.getMatchersWithResult().get(matcher).getFinalMatcher();
						if (finalMatcher.isOverwritableForNewPrediction)
							currPresentBhv.setPredictionInfos(matcher, predictedBhv);
						else
							currPresentBhv.setPredictionInfos(matcher, prevPredictedBhv);
					}
				}
			}
			extractedPredictedPresentBhvList.add(currPresentBhv);
		}
		predictedPresentBhvList = extractedPredictedPresentBhvList;
	}
}