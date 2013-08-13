package lab.davidahn.appshuttle.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.bean.RfdUserCxt;
import lab.davidahn.appshuttle.bean.UserCxt;
import lab.davidahn.appshuttle.bean.UserEnv;
import lab.davidahn.appshuttle.bhv.UserBhv;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class ContextRefiner {
	Map<UserBhv, RfdUserCxt.Builder> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt.Builder>();
	private SharedPreferences settings;
	
	public ContextRefiner(Context cxt){
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}

	private RfdUserCxt.Builder convertToRfdUCxtBuilder(UserCxt uCxt, UserBhv bhv) {
		UserEnv uEnv = uCxt.getUserEnv();
		return new RfdUserCxt.Builder()
			.setStartTime(uEnv.getTime())
			.setEndTime(uEnv.getTime())
			.setTimeZone(uEnv.getTimeZone())
			.setBhv(bhv)
			.appendLoc(uEnv.getLoc(), uEnv.getTime())
			.appendPlace(uEnv.getPlace(), uEnv.getTime());
	}
	
	public List<RfdUserCxt> refineCxt(UserCxt uCxt) {
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
		UserEnv uEnv = uCxt.getUserEnv();
		if(ongoingBhvMap.isEmpty()) {
			for(UserBhv uBhv : uCxt.getUserBhvs()){
				ongoingBhvMap.put(uBhv, convertToRfdUCxtBuilder(uCxt, uBhv));
			}
		} else {
			for(UserBhv uBhv : uCxt.getUserBhvs()){
				if(ongoingBhvMap.containsKey(uBhv)){
					RfdUserCxt.Builder rfdUCxtBuilder = ongoingBhvMap.get(uBhv);
					rfdUCxtBuilder.setEndTime(uEnv.getTime());
					//add env
					rfdUCxtBuilder.appendLoc(uEnv.getLoc(), uEnv.getTime());
					rfdUCxtBuilder.appendPlace(uEnv.getPlace(), uEnv.getTime());
				} else {
					ongoingBhvMap.put(uBhv, convertToRfdUCxtBuilder(uCxt, uBhv));
				}
			}
			Set<UserBhv> ongoingBhvList = new HashSet<UserBhv>(ongoingBhvMap.keySet());
			for(UserBhv ongoingBhv : ongoingBhvList){
				RfdUserCxt.Builder ongoingRfdUCxtBuilder = ongoingBhvMap.get(ongoingBhv);
				if(uEnv.getTime().getTime() - ongoingRfdUCxtBuilder.getEndTime().getTime() 
						> settings.getLong("service.collection.period", 6000) * 1.5){
					res.add(ongoingRfdUCxtBuilder.build());
					ongoingBhvMap.remove(ongoingBhv);
				}
			}
		}
		return res;
	}
	
	public List<RfdUserCxt> filter(Context cxt, List<RfdUserCxt> rfdUCxtList) {
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
		PackageManager packageManager = cxt.getPackageManager();
		for(RfdUserCxt rfdUCxt : rfdUCxtList){
			String bhvName = rfdUCxt.getBhv().getBhvName();
			Intent launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
			
			if(launchIntent == null) continue;
			if(bhvName.equals(cxt.getApplicationInfo().packageName)) continue;
			res.add(rfdUCxt);
		}
		return res;
	}

	
//	public List<RfdUserCxt> refineCxt(UserCxt uCxt) {
//		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
//		UserEnv uEnv = uCxt.getUserEnv();
//		if(ongoingBhvMap.isEmpty()) {
//			for(UserBhv uBhv : uCxt.getUserBhvs()){
//				ongoingBhvMap.put(uBhv, convertToRfdUCxt(uCxt, uBhv));
//			}
//		} else {
//			for(UserBhv uBhv : uCxt.getUserBhvs()){
//				if(ongoingBhvMap.containsKey(uBhv)){
//					RfdUserCxt rfdUCxt = ongoingBhvMap.get(uBhv);
//					rfdUCxt.setEndTime(uEnv.getTime());
//					//add env
//					rfdUCxt.appendLoc(uEnv.getLoc(), uEnv.getTime());
//					rfdUCxt.appendPlace(uEnv.getPlace(), uEnv.getTime());
//				} else {
//					ongoingBhvMap.put(uBhv, convertToRfdUCxt(uCxt, uBhv));
//				}
//			}
//			Set<UserBhv> ongoingBhvList = new HashSet<UserBhv>(ongoingBhvMap.keySet());
//			for(UserBhv ongoingBhv : ongoingBhvList){
//				RfdUserCxt ongoingRfdUCxt = ongoingBhvMap.get(ongoingBhv);
//				if(uEnv.getTime().getTime() - ongoingRfdUCxt.getEndTime().getTime() 
//						> settings.getLong("service.collection.period", 6000) * 1.5){
//					res.add(ongoingRfdUCxt);
//					ongoingBhvMap.remove(ongoingBhv);
//				}
//			}
//		}
//		return res;
//	}
	
//	private boolean hasBhvNameEqual(List<UserBhv> uBhvList, String bhvName){
//		for(UserBhv uBhv : uBhvList){
//			if(uBhv.getBhvName().equals(bhvName)) return true;
//		}
//		return false;
//	}
	
//	public List<RfdUserCxt> refineCxt(UserCxt uCxt, long acceptanceDelay) {
//		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
//		UserEnv uEnv = uCxt.getUserEnv();
//		if(ongoingBhvMap.isEmpty()) {
//			for(UserBhv bhv : uCxt.getUserBhv()){
//				String bhvName = bhv.getBhvName();
//				ongoingBhvMap.put(bhvName, convertToRfdUCxt(uCxt, bhv));
//			}
//		} else {
//			for(UserBhv bhv : uCxt.getUserBhv()){
//				String bhvName = bhv.getBhvName();
//				if(ongoingBhvMap.containsKey(bhvName)){
//					RfdUserCxt rfdUCxt = ongoingBhvMap.get(bhvName);
//					rfdUCxt.setEndTime(uEnv.getTime());
//					rfdUCxt.addLoc(uEnv.getLoc());
//				} else {
//					ongoingBhvMap.put(bhvName, convertToRfdUCxt(uCxt, bhv));
//				}
//			}
//			Set<String> ongoingBhvNameList = new HashSet<String>(ongoingBhvMap.keySet());
//			for(String ongoingBhvName : ongoingBhvNameList){
//				RfdUserCxt ongoingRfdUCxt = ongoingBhvMap.get(ongoingBhvName);
//				if(uEnv.getTime().getTime() - ongoingRfdUCxt.getEndTime().getTime() > acceptanceDelay){
//					res.add(ongoingRfdUCxt);
//					ongoingBhvMap.remove(ongoingBhvName);
//				}
//			}
//		}
//		return res;
//	}
	
//	public List<RfdUserCxt> refineCxt(UserCxt uCxt) {
//	List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
//	UserEnv uEvn = uCxt.getUserEnv();
//	if(prevCxt == null) {
//		for(UserBhv bhv : uCxt.getUserBhvList()){
//			String bhvName = bhv.getBhvName();
//			rfdUCxtMap.put(bhvName, convertToRfdUCxt(uCxt, bhv));
//		}
//		prevCxt = uCxt;
////	} else if(uEvn.getTime().getTime() - prevCxt.getUserEnv().getTime().getTime() > 60000) {
////		for(UserBhv prevBhv : prevCxt.getUserBhvList()){
////			String prevBhvName = prevBhv.getBhvName();
////			RfdUserCxt prevRfdUCxt = rfdUCxtMap.get(prevBhvName);
////			storeRfdCxt(prevRfdUCxt);
////			rfdUCxtMap.remove(prevBhvName);
////		}
////		prevCxt = uCxt;
//	} else {
//		for(UserBhv bhv : uCxt.getUserBhvList()){
//			String bhvName = bhv.getBhvName();
//			if(rfdUCxtMap.containsKey(bhvName)) {
//				rfdUCxtMap.get(bhvName).setEndTime(uEvn.getTime());
//				rfdUCxtMap.get(bhvName).getLocList().add(uEvn.getLoc());
//			} else {
//				rfdUCxtMap.put(bhvName, convertToRfdUCxt(uCxt, bhv));
//			}
//		}
//		for(UserBhv prevBhv : prevCxt.getUserBhvList()){
//			String prevBhvName = prevBhv.getBhvName();
//			RfdUserCxt prevRfdUCxt = rfdUCxtMap.get(prevBhvName);
//			if(!hasBhvNameEqual(uCxt.getUserBhvList(), prevBhvName)){
////				if(!prevRfdUCxt.getBhv().getBhvName().equals("screen.off")
//						res.add(prevRfdUCxt);
//				rfdUCxtMap.remove(prevBhvName);
//			}
//		}
//		prevCxt = uCxt;
//	}
//	return res;
//}
}