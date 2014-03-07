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
	private EnumMap<MatcherType, PredictedBhv> predictedBhvByMatcherType;

	public PredictedPresentBhv(UserBhv uBhv) {
		super(uBhv);
		predictedBhvByMatcherType = new EnumMap<MatcherType, PredictedBhv>(
				MatcherType.class);
	}

	@Override
	public PresentBhvType getType() {
		return PresentBhvType.PREDICTED;
	}

	public EnumMap<MatcherType, PredictedBhv> getPredictedBhv() {
		return predictedBhvByMatcherType;
	}

	public void setPredictedBhv(
			EnumMap<MatcherType, PredictedBhv> _predictedBhvByMatcherType) {
		predictedBhvByMatcherType = _predictedBhvByMatcherType;
	}

	public PredictedBhv getPredictedBhvByMatcherType(MatcherType matcherType) {
		return predictedBhvByMatcherType.get(matcherType);
	}

	public void setPredictedBhvByMatcherType(MatcherType matcherType,
			PredictedBhv predictedBhv) {
		predictedBhvByMatcherType.put(matcherType, predictedBhv);
	}

	public PredictedBhv getRecentOfPredictedBhv() {
		if (predictedBhvByMatcherType.isEmpty())
			return null;
		return Collections.max(predictedBhvByMatcherType.values());
	}
	
	/**
	 * Always returns a list (possible to be an empty list)
	 * @return
	 */
	public List<MatcherType> getMatcherList(){
		Set<MatcherType> matcherSet = predictedBhvByMatcherType.keySet();
		return new ArrayList<MatcherType>(matcherSet);
	}

	@Override
	public int compareTo(PredictedPresentBhv _uBhv) {
		long recentPredictedBhvTime = getRecentOfPredictedBhv().getTime();
		long _recentPredictedBhvTime = _uBhv.getRecentOfPredictedBhv()
				.getTime();
		if(recentPredictedBhvTime > _recentPredictedBhvTime) return 1;
		else if(recentPredictedBhvTime < _recentPredictedBhvTime) return -1;
		else {
			double score = getRecentOfPredictedBhv().getScore();
			double _score = _uBhv.getRecentOfPredictedBhv().getScore();
			if (score > _score)
				return 1;
			else if (score == _score)
				return 0;
			else
				return -1;
		}
	}

	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer();
		msg.append("PredictedBhvByMatcherType: ").append(
				predictedBhvByMatcherType.toString());
		return msg.toString();
	}

	public static PredictedPresentBhv getPredictedPresentBhv(UserBhv bhv) {
		return AppShuttleApplication.predictedPresentBhvMap.get(bhv);
	}

	public static List<PredictedPresentBhv> getPredictedPresentBhvList() {
		return new ArrayList<PredictedPresentBhv>(
				AppShuttleApplication.predictedPresentBhvMap.values());
	}

	public static void updatePredictedPresentBhv(PredictedPresentBhv bhv) {
		AppShuttleApplication.predictedPresentBhvMap.put(bhv.getUserBhv(), bhv);
	}

	public static void updatePredictedPresentBhvList(
			List<PredictedPresentBhv> list) {
		Map<UserBhv, PredictedPresentBhv> map = new HashMap<UserBhv, PredictedPresentBhv>();
		for (PredictedPresentBhv bhv : list)
			map.put(bhv.getUserBhv(), bhv);
		AppShuttleApplication.predictedPresentBhvMap = map;
	}

	public static List<PredictedPresentBhv> getPredictedPresentBhvListSorted() {
		List<PredictedPresentBhv> predictedPresentBhvList = extractPredictedPresentBhvList();
		Collections.sort(predictedPresentBhvList, Collections.reverseOrder());
		return predictedPresentBhvList;
	}

	public static List<PredictedPresentBhv> extractPredictedPresentBhvList() {
		if (PredictedBhv.getRecentPredictedBhvList().isEmpty())
			return Collections.emptyList();

		List<PredictedPresentBhv> res = new ArrayList<PredictedPresentBhv>();
		for (PredictedBhv predictedBhv : PredictedBhv.getRecentPredictedBhvList()) {
			PredictedPresentBhv presentBhv = getPredictedPresentBhv(predictedBhv);
			if (presentBhv == null) {
				presentBhv = new PredictedPresentBhv(predictedBhv);
				for (MatcherType matcherType : predictedBhv.getAllMatcherWithResult().keySet())
					presentBhv.setPredictedBhvByMatcherType(matcherType,
							predictedBhv);
			} else {
				for (MatcherType matcherType : predictedBhv.getAllMatcherWithResult().keySet()) {
					PredictedBhv oldPredictedBhv = presentBhv
							.getPredictedBhvByMatcherType(matcherType);
					if (oldPredictedBhv == null) {
						presentBhv.setPredictedBhvByMatcherType(matcherType, predictedBhv);
					} else {
						if (matcherType.isOverwritableForNewPrediction)
							presentBhv.setPredictedBhvByMatcherType(
									matcherType, predictedBhv);
					}
				}
			}
			res.add(presentBhv);
		}
		return res;
	}
}