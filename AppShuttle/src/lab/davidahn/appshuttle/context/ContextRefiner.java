package lab.davidahn.appshuttle.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

public class ContextRefiner {
	private static ContextRefiner contextRefiner;
	Map<UserBhv, RfdUserCxt.Builder> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt.Builder>();
	Map<EnvType, DurationUserEnv.Builder> ongoingEnvMap = new HashMap<EnvType, DurationUserEnv.Builder>();
	private SharedPreferences settings;
	
	private ContextRefiner(Context cxt){
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}

	public static ContextRefiner getInstance(Context cxt) {
		if(contextRefiner == null)
			contextRefiner = new ContextRefiner(cxt);
		return contextRefiner;
	}
	
	private RfdUserCxt.Builder makeAndSetRfdUCxtBuilderFrom(UserCxt uCxt, UserBhv bhv) {
//		Map<EnvType, UserEnv> uEnv = uCxt.getUserEnvs();
		return new RfdUserCxt.Builder()
			.setTime(uCxt.getTime())
			.setEndTime(uCxt.getTime())
			.setTimeZone(uCxt.getTimeZone())
			.setBhv(bhv)
			.addInitialUserEnv(((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)))
			.addInitialUserEnv(((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)));

//			.appendLoc(((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)).getLoc(), uCxt.getTime())
//			.appendPlace(((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)).getPlace(), uCxt.getTime());
	}
	
	private DurationUserEnv.Builder makeAndSetDurationUserBhvBuilderFrom(UserCxt uCxt, EnvType envType) {
		return new DurationUserEnv.Builder()
			.setTime(uCxt.getTime())
			.setEndTime(uCxt.getTime())
			.setTimeZone(uCxt.getTimeZone())
			.setUserEnv(uCxt.getUserEnv(envType));
	}
	
	public List<RfdUserCxt> refineDurationUserBhv(UserCxt uCxt) {
		List<RfdUserCxt> res = new ArrayList<RfdUserCxt>();
//		UserEnv uEnv = uCxt.getUserEnvs();
		if(ongoingBhvMap.isEmpty()) {
			for(UserBhv uBhv : uCxt.getUserBhvs()){
				ongoingBhvMap.put(uBhv, makeAndSetRfdUCxtBuilderFrom(uCxt, uBhv));
			}
		} else {
			for(UserBhv uBhv : uCxt.getUserBhvs()){
				if(ongoingBhvMap.containsKey(uBhv)){
					RfdUserCxt.Builder rfdUCxtBuilder = ongoingBhvMap.get(uBhv);
					rfdUCxtBuilder.setEndTime(uCxt.getTime());
					//add env
//					UserLoc loc = ((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
//					UserLoc place = ((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)).getPlace();
//					rfdUCxtBuilder.appendEnv(EnvType.LOCATION, ((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)), uCxt.getTime());
//					rfdUCxtBuilder.appendEnv(EnvType.PLACE, ((PlaceUserEnv)uCxt.getUserEnv(EnvType.PLACE)), uCxt.getTime());

//					rfdUCxtBuilder.appendLoc(loc, uCxt.getTime());
//					rfdUCxtBuilder.appendPlace(place, uCxt.getTime());
				} else {
					ongoingBhvMap.put(uBhv, makeAndSetRfdUCxtBuilderFrom(uCxt, uBhv));
				}
			}
			Set<UserBhv> ongoingBhvSet = new HashSet<UserBhv>(ongoingBhvMap.keySet());
			for(UserBhv ongoingBhv : ongoingBhvSet){
				RfdUserCxt.Builder ongoingRfdUCxtBuilder = ongoingBhvMap.get(ongoingBhv);
				if(uCxt.getTime().getTime() - ongoingRfdUCxtBuilder.getEndTime().getTime() 
						> settings.getLong("service.collection.period", 6000) * 1.5){
					res.add(ongoingRfdUCxtBuilder.build());
					ongoingBhvMap.remove(ongoingBhv);
				}
			}
		}
		return res;
	}
	
	//TODO test
	public List<DurationUserEnv> refineDurationUserEnv(UserCxt uCxt) {
		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
//		UserEnv uEnv = uCxt.getUserEnvs();
		if(ongoingEnvMap.isEmpty()) {
			for(EnvType envType : EnvType.values()){
				ongoingEnvMap.put(envType, makeAndSetDurationUserBhvBuilderFrom(uCxt, envType));
			}
		} else {
			for(EnvType envType : EnvType.values()){
				if(ongoingEnvMap.containsKey(envType)){
					DurationUserEnv.Builder ongoingDurationUserEnvBuilder = ongoingEnvMap.get(envType);
					if(!uCxt.getUserEnv(envType).equals(ongoingDurationUserEnvBuilder.getUserEnv())){
						res.add(ongoingDurationUserEnvBuilder.build());
						ongoingEnvMap.put(envType, makeAndSetDurationUserBhvBuilderFrom(uCxt, envType));
					} else {
						ongoingDurationUserEnvBuilder.setEndTime(uCxt.getTime());
					}
				} else {
					ongoingEnvMap.put(envType, makeAndSetDurationUserBhvBuilderFrom(uCxt, envType));
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