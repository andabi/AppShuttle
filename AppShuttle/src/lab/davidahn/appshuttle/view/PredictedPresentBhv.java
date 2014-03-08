package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

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
	public PresentBhvType getType() {
		return PresentBhvType.PREDICTED;
	}

	public EnumMap<MatcherType, PredictedBhv> getMatchersWithPredictionInfos() {
		return matchersWithPredictionInfos;
	}
	
	public void setMatchersWithPredictionInfos(
			EnumMap<MatcherType, PredictedBhv> _predictedBhvByMatcherType) {
		matchersWithPredictionInfos = _predictedBhvByMatcherType;
	}
	
	public List<MatcherType> getMatchers(){
		Set<MatcherType> matcherSet = matchersWithPredictionInfos.keySet();
		return new ArrayList<MatcherType>(matcherSet);
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
			PredictedPresentBhv currPresentBhv = new PredictedPresentBhv(predictedBhv);
			PredictedPresentBhv prevPresentBhv = getPredictedPresentBhv(predictedBhv);
			for (MatcherType matcherType : predictedBhv.getAllMatcherWithResult().keySet()) {
				if (prevPresentBhv == null) {
					currPresentBhv.setPredictionInfos(matcherType,
							predictedBhv);
				} else {
					PredictedBhv prevPredictedBhv = prevPresentBhv
							.getPredictionInfos(matcherType);
					if (prevPredictedBhv == null) {
						currPresentBhv.setPredictionInfos(matcherType, predictedBhv);
					} else {
						if (matcherType.isOverwritableForNewPrediction)
							currPresentBhv.setPredictionInfos(
									matcherType, predictedBhv);
						else {
							currPresentBhv.setPredictionInfos(
									matcherType, prevPredictedBhv);
						}
					}
				}
			}
			extractedPredictedPresentBhvList.add(currPresentBhv);
		}
		predictedPresentBhvList = extractedPredictedPresentBhvList;
	}
}