package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.bhv.FavoriteBhv;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

public class PresentBhvManager {
	public static PresentBhv getPresentBhv(UserBhv bhv) {
		return AppShuttleApplication.presentBhvMap.get(bhv);
	}
	
	public static List<PresentBhv> getPresentBhvListFilteredSorted(int topN) {
		if(topN < 0)
			throw new IllegalArgumentException("the number of presentBhv < 0");
	
		List<PresentBhv> currPresentBhvList = extractCurrPresentBhvs(AppShuttleApplication.predictedBhvInfoMap);
		List<PrevPresentBhv> prevPresentBhvList = extractPrevPresentBhvs(AppShuttleApplication.predictedBhvInfoMap);

		//storing state
		Map<UserBhv, PresentBhv> presentBhvMap = new HashMap<UserBhv, PresentBhv>();
		for(PresentBhv presentBhv : currPresentBhvList)
			presentBhvMap.put(presentBhv.getUserBhv(), presentBhv);
		for(PrevPresentBhv presentBhv : prevPresentBhvList)
			presentBhvMap.put(presentBhv.getUserBhv(), presentBhv);
		AppShuttleApplication.presentBhvMap = presentBhvMap;
		
		//filtering not eligible
		List<PresentBhv> filteredCurrPresentBhvList = new ArrayList<PresentBhv>();
		for(PresentBhv bhv : currPresentBhvList)
			if (isEligible(bhv))
				filteredCurrPresentBhvList.add(bhv);
		List<PrevPresentBhv> filteredPrevPresentBhvList = new ArrayList<PrevPresentBhv>();
		for(PrevPresentBhv bhv : prevPresentBhvList)
			if (isEligible(bhv))
				filteredPrevPresentBhvList.add(bhv);
		
		//sort
		Collections.sort(filteredCurrPresentBhvList, Collections.reverseOrder());
		Collections.sort(filteredPrevPresentBhvList, Collections.reverseOrder());

		//adjust # of prevPresentBhv
		List<PrevPresentBhv> resizedFilteredPrevPresentBhvList = new ArrayList<PrevPresentBhv>();
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		int minNumPresentBhv = preferenceSettings.getInt("viewer.min_num_present_bhv", 6);
		int numPrevPresentBhvs = Math.max(minNumPresentBhv - filteredCurrPresentBhvList.size(), 0);
		resizedFilteredPrevPresentBhvList = filteredPrevPresentBhvList
				.subList(0, Math.min(filteredPrevPresentBhvList.size(), numPrevPresentBhvs));

		List<PresentBhv> presentBhvList = new ArrayList<PresentBhv>();
		presentBhvList.addAll(filteredCurrPresentBhvList);
		presentBhvList.addAll(resizedFilteredPrevPresentBhvList);

		return presentBhvList.subList(0, Math.min(presentBhvList.size(), topN));
	}

	private static List<PrevPresentBhv> extractPrevPresentBhvs(Map<UserBhv, PredictedBhvInfo> currPredictionInfos) {
		List<PrevPresentBhv> res = new ArrayList<PrevPresentBhv>();
		for (UserBhv bhv : AppShuttleApplication.presentBhvMap.keySet()) {
			if (currPredictionInfos.containsKey(bhv))
				continue;
			PresentBhv presentBhv = getPresentBhv(bhv);
			PrevPresentBhv prevPresentBhv = new PrevPresentBhv(bhv);
			prevPresentBhv.setFirstPredictionInfo(presentBhv.getFirstPredictionInfo());
			res.add(prevPresentBhv);
		}
		return res;
	}

	private static List<PresentBhv> extractCurrPresentBhvs(Map<UserBhv, PredictedBhvInfo> currPredictionInfos) {
		if (currPredictionInfos == null || currPredictionInfos.isEmpty())
			return Collections.emptyList();

		List<PresentBhv> res = new ArrayList<PresentBhv>();
		for (UserBhv uBhv : currPredictionInfos.keySet()) {
			PredictedBhvInfo currPredictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv prevPresentBhv = getPresentBhv(uBhv);
			PresentBhv currPresentBhv = new PresentBhv(uBhv);
			if (prevPresentBhv == null || !prevPresentBhv.isAlive()) {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet())
					currPresentBhv.setFirstPredictionInfoByMatcherType(
							matcherType, currPredictionInfo);
			} else {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet()) {
					PredictedBhvInfo firstPredictionInfo = prevPresentBhv
							.getFirstPredictionInfoByMatcherType(matcherType);
					if (firstPredictionInfo == null){
						currPresentBhv.setFirstPredictionInfoByMatcherType(
								matcherType, currPredictionInfo);
					}
					else {
						if (matcherType.isOverwritableForNewPrediction)
							currPresentBhv.setFirstPredictionInfoByMatcherType(
									matcherType, currPredictionInfo);
						else {
							currPresentBhv.setFirstPredictionInfoByMatcherType(
									matcherType, firstPredictionInfo);
						}
					}
				}
				// if(uBhv.getBhvName().equals("com.android.chrome")){
				// Log.d("test", currPresentBhv.hashCode() + "");
				// Log.d("test",
				// currPresentBhv.getRecentPredictionInfo().getMatcherResultMap().keySet()
				// + "");
				// }
			}
			res.add(currPresentBhv);
		}
		return res;
	}

	private static boolean isEligible(UserBhv uBhv) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		if (userBhvManager.getBlockedBhvSet().contains(uBhv))
			return false;
		else if (userBhvManager.getFavoriteBhvSet().contains(uBhv)
				&& ((FavoriteBhv) userBhvManager.getFavoriteBhv(uBhv))
						.isNotifiable())
			return false;
		else if (uBhv.getBhvType() == UserBhvType.APP
				&& uBhv.getBhvName().equals(
						AppBhvCollector.getInstance().getPresentApp(1, true)
								.get(0))) {
			return false;
		} else if (uBhv.getBhvType() == UserBhvType.SENSOR_ON
				&& uBhv.getBhvName().equals(SensorType.WIFI.name())) {
			WifiManager wifi = (WifiManager) AppShuttleApplication.getContext()
					.getSystemService(Context.WIFI_SERVICE);
			if (wifi.isWifiEnabled())
				return false;
		}
		return true;
	}
}
