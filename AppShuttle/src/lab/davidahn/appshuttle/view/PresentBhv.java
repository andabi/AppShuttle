package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiManager;
import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.PredictedBhv;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupResult;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupType;
import lab.davidahn.appshuttle.predict.matchergroup.MatcherGroupTypeComparator;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;

public abstract class PresentBhv extends ViewableUserBhv {
	private long time;
	private long endTime;
	
	public PresentBhv(UserBhv uBhv) {
		super(uBhv);
	}

	public long getTime() {
		return time;
	}
	public long getEndTime() {
		return endTime;
	}

	public abstract PresentBhvType getType();

	@Override
	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		viewMsg = msg.toString();
		PredictedPresentBhv recentPresentBhv = PredictedPresentBhv.getPredictedPresentBhv(uBhv);
		
		if(recentPresentBhv == null)
			return viewMsg;

		PredictedBhv predictionInfo = recentPresentBhv.getRecentOfPredictedBhv();
		
//		if(uBhv.getBhvName().equals("com.android.chrome")){
//			Log.d("test", recentPresentBhv.hashCode() + "");
//			Log.d("test", predictionInfo.getMatcherResultMap().keySet() + "");
//		}
		
		if(predictionInfo == null)
			return viewMsg;
		
		Map<MatcherGroupType, MatcherGroupResult> macherGroupResults = predictionInfo.getMatcherGroupResultMap();
		List<MatcherGroupType> matcherGroupTypeList = new ArrayList<MatcherGroupType>(macherGroupResults.keySet());
		Collections.sort(matcherGroupTypeList, new MatcherGroupTypeComparator());
		Collections.reverse(matcherGroupTypeList);
		
		for (MatcherGroupType matcherGroupType : matcherGroupTypeList) {
			msg.append(macherGroupResults.get(matcherGroupType).getViewMsg()).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		viewMsg = msg.toString();
		
		return viewMsg;
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_present_container;
	}
	
	public static List<PresentBhv> getPresentBhvListFilteredSorted(int topN) {
		if(topN < 0)
			throw new IllegalArgumentException("the number of presentBhv < 0");

		List<PredictedPresentBhv> predictedPresentBhvList = PredictedPresentBhv.getPredictedPresentBhvListFilteredSorted();
		int minNumPresentBhv = NotiBarNotifier.getInstance().getNumPredictedElem();
		int numHistoryPresentBhv = Math.max(minNumPresentBhv - predictedPresentBhvList.size(), 0);
		List<HistoryPresentBhv> historyPresentBhvList = HistoryPresentBhv.retrieveHistoryPresentBhvList(numHistoryPresentBhv);

		List<PresentBhv> presentBhvList = new ArrayList<PresentBhv>();
		presentBhvList.addAll(predictedPresentBhvList);
		presentBhvList.addAll(historyPresentBhvList);
		
		return presentBhvList.subList(0, Math.min(presentBhvList.size(), topN));
	}

	protected static boolean isEligible(UserBhv uBhv) {
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