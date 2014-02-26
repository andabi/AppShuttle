package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserPlace;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit.Builder;
import lab.davidahn.appshuttle.predict.matcher.conf.PositionMatcherConf;

public class PlacePositionMatcher extends PositionMatcher {

	public PlacePositionMatcher(PositionMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.PLACE;
	}
	
	@Override
	public int getPriority() {
		return MatcherType.PLACE.priority;
	}
	
	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder matcherCountUnitBuilder = null;
		
		DurationUserBhv prevDurationUserBhv = null;
		UserPlace lastKnownUserPlace = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(durationUserBhv.getTimeDate()
					, durationUserBhv.getEndTimeDate(), EnvType.PLACE)){
				UserPlace userPlace = (UserPlace)durationUserEnv.getUserEnv();
				if(prevDurationUserBhv == null) {
					matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userPlace);
				} else {
					if(!userPlace.equals(lastKnownUserPlace)){
						res.add(matcherCountUnitBuilder.build());
						matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userPlace);
					} else {
						long time = durationUserBhv.getTimeDate().getTime();
						long lastEndTime = prevDurationUserBhv.getEndTimeDate().getTime();
						if(time - lastEndTime >= conf.getAcceptanceDelay()){
							res.add(matcherCountUnitBuilder.build());
							matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv, userPlace);
						}
					}
				}
				
				prevDurationUserBhv = durationUserBhv;
				lastKnownUserPlace = userPlace;
			}
		}

		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		
		return res;
	}

	private Builder makeMatcherCountUnitBuilder(DurationUserBhv durationUserBhv, UserPlace userPlace) {
		return new MatcherCountUnit.Builder(durationUserBhv.getUserBhv()).setProperty("place", userPlace);
	}

	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= conf.getMinNumHistory());
		
		double inverseEntropy = 0;
		Set<UserPlace> uniquePlace = new HashSet<UserPlace>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserPlace uPlace = ((UserPlace) unit.getProperty("place"));

			if(!uPlace.isValid())
				continue;
			
			Iterator<UserPlace> it = uniquePlace.iterator();
			boolean unique = true;
			if(!uniquePlace.isEmpty()){
				while(it.hasNext()){
					UserPlace uniquePlaceElem = it.next();
					if(uPlace.equals(uniquePlaceElem)){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniquePlace.add(uPlace);
		}
		int entropy = uniquePlace.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		UserPlace uPlace = (UserPlace) uCxt.getUserEnv(EnvType.PLACE);
		if(uPlace.equals((UserPlace) unit.getProperty("place")))
			return 1;
		else
			return 0;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory,
			Map<MatcherCountUnit, Double> relatedHistoryMap,
			SnapshotUserCxt uCxt) {
		if(numTotalHistory <= 0)
			return 0;
		
		return relatedHistoryMap.size() / numTotalHistory;
	}
}

//@Override
//protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
//	List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//	MatcherCountUnit.Builder matcherCountUnitBuilder = null;
//	
//	for(DurationUserBhv durationUserBhv : durationUserBhvList){
//		for(DurationUserEnv durationUserEnv : DurationUserEnvManager.getInstance().retrieve(durationUserBhv.getTimeDate()
//				, durationUserBhv.getEndTimeDate(), EnvType.PLACE)){
//			matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv())
//				.addRelatedDurationUserBhv(durationUserBhv)
//				.setProperty("place", (UserPlace)durationUserEnv.getUserEnv());
//			res.add(matcherCountUnitBuilder.build());
//		}
//	}
//	
//	return res;
//}

//@Override
//protected List<MatcherCountUnit> mergeMatcherCountUnit(List<MatcherCountUnit> matcherCountUnitList) {
//	List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//
//	MatcherCountUnit lastUnit = null;
//	for(MatcherCountUnit unit : matcherCountUnitList){
//		if(lastUnit == null){
//			lastUnit = unit;
//			continue;
//		}
//		
//		UserPlace userPlace = (UserPlace)unit.getProperty("place");
//		UserPlace lastUserPlace = (UserPlace)lastUnit.getProperty("place");
//		if(!userPlace.equals(lastUserPlace)){
//			res.add(lastUnit);
//			lastUnit = unit;
//			continue;
//		}
//		
//		long time = unit.getDurationUserBhvList().get(0).getTimeDate().getTime();
//		long lastTime = lastUnit.getDurationUserBhvList().get(0).getTimeDate().getTime();
//		if(time - lastTime	>= conf.getAcceptanceDelay()){
//			res.add(lastUnit);
//		}
//		
//		lastUnit = unit;
//	}
//	
//	res.add(lastUnit);
//	
//	return res;
//}
