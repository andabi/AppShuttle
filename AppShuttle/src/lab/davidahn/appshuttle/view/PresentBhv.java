package lab.davidahn.appshuttle.view;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.PredictionInfo;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;

public class PresentBhv extends BaseUserBhv implements Comparable<PresentBhv> {

	private EnumMap<MatcherType, PredictionInfo> startPredictionInfoByMatcherType;

	public PresentBhv(UserBhv uBhv) {
		super(uBhv.getBhvType(), uBhv.getBhvName());
		startPredictionInfoByMatcherType = new EnumMap<MatcherType, PredictionInfo>(
				MatcherType.class);
	}

	public PredictionInfo getRecentPredictionInfo() {
		return Collections.max(startPredictionInfoByMatcherType.values());
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
			PredictionInfo currPredictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv recentPresentBhv = getRecentPresentBhvs().get(uBhv);
			PresentBhv currPresentBhv = new PresentBhv(uBhv);
			if (recentPresentBhv == null) {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet())
					currPresentBhv.setStartPredictionInfoByMatcherType(
							matcherType, currPredictionInfo);
			} else {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet()) {
					PredictionInfo startPredictionInfo = recentPresentBhv
							.getStartPredictionInfoByMatcherType(matcherType);
					if (startPredictionInfo == null)
						currPresentBhv.setStartPredictionInfoByMatcherType(
								matcherType, currPredictionInfo);
					else {
						if (matcherType.isOverwritableForNewPrediction)
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, currPredictionInfo);
						else
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, startPredictionInfo);
					}
				}
			}
			res.put(uBhv, currPresentBhv);
		}

		AppShuttleApplication.recentPresentBhvs = res;

		return res;
	}

	private PredictionInfo getStartPredictionInfoByMatcherType(
			MatcherType matcherType) {
		return startPredictionInfoByMatcherType.get(matcherType);
	}

	private void setStartPredictionInfoByMatcherType(MatcherType matcherType,
			PredictionInfo info) {
		startPredictionInfoByMatcherType.put(matcherType, info);
	}

	@Override
	public int compareTo(PresentBhv _uBhv) {
		Date recentPredictionTime = getRecentPredictionInfo().getTimeDate();
		Date _recentPredictionTime = _uBhv.getRecentPredictionInfo()
				.getTimeDate();

		int comp = recentPredictionTime.compareTo(_recentPredictionTime);
		if (comp != 0)
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