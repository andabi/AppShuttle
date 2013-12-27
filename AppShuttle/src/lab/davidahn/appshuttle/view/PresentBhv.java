package lab.davidahn.appshuttle.view;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhv extends NormalBhv implements Comparable<PresentBhv> {

	private EnumMap<MatcherType, PredictionInfo> initialPredictionInfoByMatcherType;

	public PresentBhv(UserBhv uBhv) {
		super(uBhv);
		initialPredictionInfoByMatcherType = new EnumMap<MatcherType, PredictionInfo>(
				MatcherType.class);
	}

	public PredictionInfo getRecentPredictionInfo() {
		return Collections.max(initialPredictionInfoByMatcherType.values());
	}

	public static Map<UserBhv, PresentBhv> getRecentPresentBhvs() {
		return AppShuttleApplication.recentPresentBhvs;
	}

	public static Map<UserBhv, PresentBhv> extractPresentBhvs(
			Map<UserBhv, PredictionInfo> currPredictionInfos) {
		if (currPredictionInfos == null || currPredictionInfos.isEmpty())
			return Collections.emptyMap();

		Map<UserBhv, PresentBhv> res = new HashMap<UserBhv, PresentBhv>();

		for (UserBhv uBhv : currPredictionInfos.keySet()) {
			PredictionInfo predictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv recentPresentBhv = getRecentPresentBhvs().get(uBhv);
			if (recentPresentBhv == null) {
				PresentBhv presentBhv = new PresentBhv(uBhv);
				for (MatcherType matcherType : predictionInfo
						.getMatcherResultMap().keySet())
					presentBhv.setInitialPredictionInfoByMatcherType(
							matcherType, predictionInfo);
				res.put(uBhv, presentBhv);
			} else {
				for (MatcherType matcherType : predictionInfo
						.getMatcherResultMap().keySet()) {
					PredictionInfo initialPredictionInfo = recentPresentBhv
							.getInitialPredictionInfoByMatcherType(matcherType);
					if (initialPredictionInfo == null)
						recentPresentBhv.setInitialPredictionInfoByMatcherType(
								matcherType, predictionInfo);
					else {
						if (matcherType.isOverwritableForNewPrediction)
							recentPresentBhv.setInitialPredictionInfoByMatcherType(
									matcherType, predictionInfo);
					}
				}
				res.put(uBhv, recentPresentBhv);
			}
		}

		AppShuttleApplication.recentPresentBhvs = res;

		return res;
	}

	private PredictionInfo getInitialPredictionInfoByMatcherType(
			MatcherType matcherType) {
		return initialPredictionInfoByMatcherType.get(matcherType);
	}

	private void setInitialPredictionInfoByMatcherType(MatcherType matcherType,
			PredictionInfo info) {
		initialPredictionInfoByMatcherType.put(matcherType, info);
	}

	@Override
	public int compareTo(PresentBhv _uBhv) {
		Date recentPredictionTime = getRecentPredictionInfo().getTimeDate();
		Date _recentPredictionTime = _uBhv.getRecentPredictionInfo()
				.getTimeDate();

		int comp = recentPredictionTime.compareTo(_recentPredictionTime);
		if(comp != 0)
			return comp;
		else {
			double score = getRecentPredictionInfo().getScore();
			double _score = _uBhv.getRecentPredictionInfo().getScore();
			if (score > _score)
				return 1;
			else if (score == _score)
				return 0;
			else
				return -1;
		}
	}
}