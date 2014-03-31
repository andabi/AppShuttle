package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.SensorType;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
import android.content.Context;
import android.net.wifi.WifiManager;

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
	public Integer getNotibarContainerId() {
		return R.id.noti_present_container;
	}
	
	public static List<PresentBhv> getPresentBhvListFilteredSorted(int topN) {
		return getPresentBhvListFilteredSorted(topN, true);
	}
	
	/**
	 * Return a list of present Bhvs (to notify)
	 * @param topN
	 * @param isFilteringCurrent
	 * @return
	 */
	public static List<PresentBhv> getPresentBhvListFilteredSorted(int topN, boolean isFilteringCurrent) {
		if(topN < 0)
			throw new IllegalArgumentException("the number of presentBhv < 0");

		HistoryPresentBhv.storeHistoryPresentBhvList(HistoryPresentBhv.extractHistoryPresentBhvList());
		
		
		// Build predicted list
		List<PredictedPresentBhv> predictedPresentBhvListSorted = PredictedPresentBhv.getPredictedPresentBhvListSorted();
		PredictedPresentBhv.updatePredictedPresentBhvList(predictedPresentBhvListSorted);
		
		List<PresentBhv> predictedPresentBhvListFilteredSorted = new ArrayList<PresentBhv>();
		predictedPresentBhvListFilteredSorted.addAll(predictedPresentBhvListSorted);
		predictedPresentBhvListFilteredSorted = getEligiblePresentList(predictedPresentBhvListFilteredSorted, isFilteringCurrent);
		
		// Build history list
		List<HistoryPresentBhv> historyPresentBhvListSorted = HistoryPresentBhv.retrieveHistoryPresentBhvListSorted();
		List<PresentBhv> historyPresentBhvListFilteredSorted = new ArrayList<PresentBhv>();
		for(HistoryPresentBhv historyPresentBhv : historyPresentBhvListSorted)
			if(!predictedPresentBhvListFilteredSorted.contains(historyPresentBhv))
				historyPresentBhvListFilteredSorted.add(historyPresentBhv);
		
		historyPresentBhvListFilteredSorted = getEligiblePresentList(historyPresentBhvListFilteredSorted, isFilteringCurrent);
		
		// TODO: Build initial list
		List<PresentBhv> initialPresentBhvListSorted = new ArrayList<PresentBhv>();
		
		
		// Concat. list
		List<PresentBhv> presentBhvList = new ArrayList<PresentBhv>();
		presentBhvList.addAll(predictedPresentBhvListFilteredSorted);
		presentBhvList.addAll(historyPresentBhvListFilteredSorted);
		presentBhvList.addAll(initialPresentBhvListSorted);
				
		/*
		int numHistoryPresentBhv = Math.max(minNumPresentBhv - predictedPresentBhvListFilteredSorted.size(), 0);
		historyPresentBhvListFilteredSorted = historyPresentBhvListFilteredSorted.subList(0, Math.min(historyPresentBhvListFilteredSorted.size(), numHistoryPresentBhv));
		 */
		
		int numPresentBhv;
		numPresentBhv = Math.max(predictedPresentBhvListFilteredSorted.size(), NotiBarNotifier.getInstance().getNumPresentElem());
		
		if (topN < numPresentBhv)
			numPresentBhv = topN;
				
		return presentBhvList.subList(0, Math.min(presentBhvList.size(), numPresentBhv));
	}

	public static List<PresentBhv> getEligiblePresentList(List<PresentBhv> list, boolean isFilteringCurrent) {
		List<PresentBhv> filteredPresentBhvList = new ArrayList<PresentBhv>(list);
		filteredPresentBhvList = getBlockedBhvFilteredList(filteredPresentBhvList);
		filteredPresentBhvList = getFavoriteBhvFilteredList(filteredPresentBhvList);
		filteredPresentBhvList = getSensorOnBhvFilteredList(filteredPresentBhvList);
		
		if(isFilteringCurrent)
			filteredPresentBhvList = getCurrentBhvFilteredList(filteredPresentBhvList);
		
		return filteredPresentBhvList;
	}

	private static List<PresentBhv> getBlockedBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		for(PresentBhv bhv : presentBhvList){
			if(BlockedBhvManager.getInstance().getBlockedBhvSet().contains(bhv))
				continue;
			res.add(bhv);
		}
		return res;
	}
	
	private static List<PresentBhv> getFavoriteBhvFilteredList(Collection<PresentBhv> presentBhvList) {
		List<PresentBhv> res = new ArrayList<PresentBhv>();
		FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
		for(PresentBhv bhv : presentBhvList){
			if(favoriteBhvManager.getFavoriteBhvSet().contains(bhv)
					&& favoriteBhvManager.getFavoriteBhv(bhv).isNotifiable())
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
}