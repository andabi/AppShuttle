package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Collection;
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
import android.net.wifi.WifiManager;

public class PresentBhvManager {
	
	public static Map<UserBhv, PresentBhv> getPresentBhvs() {
		return AppShuttleApplication.presentBhvMap;
	}
	
	public static Map<UserBhv, PresentBhv> extractPresentBhvs(
			Map<UserBhv, PredictedBhvInfo> currPredictionInfos) {
		if (currPredictionInfos == null || currPredictionInfos.isEmpty())
			return Collections.emptyMap();

		Map<UserBhv, PresentBhv> currPresentBhvs = new HashMap<UserBhv, PresentBhv>();
		for (UserBhv uBhv : currPredictionInfos.keySet()) {
			PredictedBhvInfo currPredictionInfo = currPredictionInfos.get(uBhv);
			PresentBhv recentPresentBhv = getPresentBhvs().get(uBhv);
			PresentBhv currPresentBhv = new PresentBhv(uBhv);
			if (recentPresentBhv == null) {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet())
					currPresentBhv.setStartPredictionInfoByMatcherType(
							matcherType, currPredictionInfo);
			} else {
				for (MatcherType matcherType : currPredictionInfo
						.getMatcherResultMap().keySet()) {
					PredictedBhvInfo startPredictionInfo = recentPresentBhv
							.getStartPredictionInfoByMatcherType(matcherType);
					if (startPredictionInfo == null)
						currPresentBhv.setStartPredictionInfoByMatcherType(
								matcherType, currPredictionInfo);
					else {
						if (matcherType.isOverwritableForNewPrediction)
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, currPredictionInfo);
						else {
							currPresentBhv.setStartPredictionInfoByMatcherType(
									matcherType, startPredictionInfo);
						}
					}
				}
//				if(uBhv.getBhvName().equals("com.android.chrome")){
//					Log.d("test", currPresentBhv.hashCode() + "");
//					Log.d("test", currPresentBhv.getRecentPredictionInfo().getMatcherResultMap().keySet() + "");
//				}
			}
			currPresentBhvs.put(uBhv, currPresentBhv);
		}
		AppShuttleApplication.presentBhvMap = currPresentBhvs;
		return currPresentBhvs;
	}

	public static List<PresentBhv> getPresentBhvListFilteredSorted(int topN) {
		List<PresentBhv> filteredPresentBhvList = new ArrayList<PresentBhv>();
		filteredPresentBhvList = getBlockedBhvFilteredList(getPresentBhvs().values());
		filteredPresentBhvList = getFavoriteBhvFilteredList(filteredPresentBhvList);
		filteredPresentBhvList = getCurrentBhvFilteredList(filteredPresentBhvList);
		filteredPresentBhvList = getSensorOnBhvFilteredList(filteredPresentBhvList);
			
		Collections.sort(filteredPresentBhvList, Collections.reverseOrder());
		
		return filteredPresentBhvList.subList(0, Math.min(filteredPresentBhvList.size(), topN));
	}

	private static List<PresentBhv> getBlockedBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		for(PresentBhv bhv : presentBhvList){
			if(UserBhvManager.getInstance().getBlockedBhvSet().contains(bhv))
				continue;
			res.add(bhv);
		}
		return res;
	}
	
	private static List<PresentBhv> getFavoriteBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(PresentBhv bhv : presentBhvList){
			if(userBhvManager.getFavoriteBhvSet().contains(bhv)
					&& ((FavoriteBhv)userBhvManager.getViewableUserBhv(bhv)).isNotifiable())
				continue;
			res.add(bhv);
		}
		return res;
	}
	
	private static List<PresentBhv> getCurrentBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		for(PresentBhv bhv : presentBhvList){
			if(bhv.getBhvType() == UserBhvType.APP
					&& bhv.getBhvName().equals(AppBhvCollector.getInstance().getPresentApp(1, true).get(0)))
				continue;
			res.add(bhv);
		}
		return res;
	}
	
	private static List<PresentBhv> getSensorOnBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		for(PresentBhv bhv : presentBhvList){
			if(bhv.getBhvType() == UserBhvType.SENSOR_ON && bhv.getBhvName().equals(SensorType.WIFI.name())){
				WifiManager wifi = (WifiManager)AppShuttleApplication.getContext().getSystemService(Context.WIFI_SERVICE);
				if(wifi.isWifiEnabled()) continue;
			}
			res.add(bhv);
		}
		return res;
	}
	
/*	private static boolean isEligible(UserBhv uBhv){
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		if(userBhvManager.getBlockedBhvSet().contains(uBhv))
			return false;
		
		if(userBhvManager.getFavoriteBhvSet().contains(uBhv)
				&& ((FavoriteBhv)userBhvManager.getViewableUserBhv(uBhv)).isNotifiable())
			return false;
		
		if(uBhv.getBhvType() == UserBhvType.APP
				&& uBhv.getBhvName().equals(AppBhvCollector.getInstance().getPresentApp(1, true).get(0)))
			return false;
		
		if(uBhv.getBhvType() == UserBhvType.SENSOR_ON 
				&& uBhv.getBhvName().equals(SensorType.WIFI.name())){
			WifiManager wifi = (WifiManager)AppShuttleApplication.getContext().getSystemService(Context.WIFI_SERVICE);
			if(wifi.isWifiEnabled())
				return false;
		}
		return true;
	}*/
}