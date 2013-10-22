package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserLoc;

/**
 * K-NN based algorithm
 * @author andabi
 *
 */
public class LocContextMatcher extends TemplateContextMatcher {
	int _toleranceInMeter;

	public LocContextMatcher(Date time, long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, int toleranceInMeter) {
		super(time, duration, minLikelihood, minInverseEntropy, minNumCxt);
		_toleranceInMeter = toleranceInMeter;
		_matcherType = MatcherType.LOCATION;
	}
	
//	@Override
//	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList) {
//		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//
//		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
//		for(DurationUserBhv rfdUCxt : rfdUCxtList){
//			mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//			mergedRfdUCxtBuilder.addRfdUserCxtList(rfdUCxt);
//			res.add(mergedRfdUCxtBuilder.build());
//		}
//		return res;
//	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();

		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		UserLoc lastKnownUserLoc = null;

		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			for(DurationUserEnv durationUserEnv : durationUserEnvManager.retrieve(rfdUCxt.getTimeDate()
					, rfdUCxt.getEndTimeDate(), EnvType.LOCATION)){
//				try {
					UserLoc userLoc = (UserLoc)durationUserEnv.getUserEnv();
					long duration = durationUserEnv.getDuration();
					if(lastKnownUserLoc == null) {
						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
						mergedRfdUCxtBuilder.setProperty("loc", userLoc);
						mergedRfdUCxtBuilder.setProperty("duration", duration);
					} else {
						if(!userLoc.equals(lastKnownUserLoc)){
							res.add(mergedRfdUCxtBuilder.build());
							mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getUserBhv());
							mergedRfdUCxtBuilder.setProperty("loc", userLoc);
							mergedRfdUCxtBuilder.setProperty("duration", duration);
						}
					}
					lastKnownUserLoc = userLoc;
//				} catch (InvalidUserEnvException e) {
//					;
//				}
			}
		}
		if(mergedRfdUCxtBuilder != null)
			res.add(mergedRfdUCxtBuilder.build());
//		try {
//			UserLoc currULoc = ((LocUserEnv)uCxt.getUserEnv(EnvType.LOCATION)).getLoc();
//			if(lastKnownUserLoc != null && lastKnownUserLoc.isSame(currULoc)){
//				res.remove(res.size()-1);
//			}
//		} catch (InvalidUserEnvException e) {
//			;
//		}
		return res;
	}
	
//		@Override
//		protected List<MatcherCountUnit> mergeCxtByCountUnit(List<RfdUserCxt> rfdUCxtList) {
//			List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//			
//			MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
//			Entry<Date, UserLoc> lastKnownTimeAndLoc = null;
//			for(RfdUserCxt rfdUCxt : rfdUCxtList){
//				for(Entry<Date, UserLoc> timeAndLoc : rfdUCxt.getLocs().entrySet()){
//					if(lastKnownTimeAndLoc == null) {
//						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//						mergedRfdUCxtBuilder.setLoc(timeAndLoc.getValue());
//					} else {
//						if(timeAndLoc.getValue().equals(lastKnownTimeAndLoc.getValue())
//								&& !moved(lastKnownTimeAndLoc, timeAndLoc)){
//							;
//						} else {
//							res.add(mergedRfdUCxtBuilder.build());
//							mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//							mergedRfdUCxtBuilder.setLoc(timeAndLoc.getValue());
//						}
//					}
//					lastKnownTimeAndLoc = timeAndLoc;
//				}
//			}
//			if(mergedRfdUCxtBuilder != null)
//				res.add(mergedRfdUCxtBuilder.build());
//			return res;
			
//		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
//		
//		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
//		UserLoc lastKnownLoc = null;
//		for(RfdUserCxt rfdUCxt : rfdUCxtList){
//			for(UserLoc Loc : rfdUCxt.getLocs().values()){
//				if(lastKnownLoc == null) {
//					mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//					mergedRfdUCxtBuilder.setLoc(Loc);
//				} else {
//					if(Loc.equals(lastKnownLoc)) {
//						;
//					} else {
//						res.add(mergedRfdUCxtBuilder.build());
//						mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
//						mergedRfdUCxtBuilder.setLoc(Loc);
//					}
//				}
//				lastKnownLoc = Loc;
//			}
//		}
//		if(mergedRfdUCxtBuilder != null)
//			res.add(mergedRfdUCxtBuilder.build());
//		return res;

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		return 1;
	}
	
	@Override
	protected double computeLikelihood(int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		long totalSpentTime = 0, validSpentTime = 0;
		
		for(MatcherCountUnit unit : relatedCxtMap.keySet()){
			UserLoc userLoc = ((UserLoc) unit.getProperty("loc"));
			long duration = ((Long) unit.getProperty("duration"));
			totalSpentTime += duration;
			try{
				if(userLoc.proximity((UserLoc)uCxt.getUserEnv(EnvType.LOCATION), _toleranceInMeter)){
					validSpentTime += duration;
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		
		double likelihood = 0;
		if(totalSpentTime > 0)
			likelihood = 1.0 * validSpentTime / totalSpentTime;
		return likelihood;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
		double inverseEntropy = 0;
		Set<UserLoc> uniqueLoc = new HashSet<UserLoc>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			UserLoc uLoc = ((UserLoc) unit.getProperty("loc"));
			Iterator<UserLoc> it = uniqueLoc.iterator();
			boolean unique = true;
			if(!uniqueLoc.isEmpty()){
				while(it.hasNext()){
					UserLoc uniqueLocElem = it.next();
					try {
						if(uLoc.proximity(uniqueLocElem, _toleranceInMeter)){
							unique = false;
							break;
						}
					} catch (InvalidUserEnvException e) {
						unique = false;
					}
				}
			}
			if(unique)
				uniqueLoc.add(uLoc);
		}
		int entropy = uniqueLoc.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
	
	protected double computeScore(MatchedResult matchedResult) {
		double likelihood = matchedResult.getLikelihood();
		double inverseEntropy = matchedResult.getInverseEntropy();
		
		double score = (1 + 0.5 * inverseEntropy + 0.1 * likelihood);
		
		return score;
	}
	
//	private boolean moved(Date fromTime, Date toTime){
//		ChangeUserEnvDao changedUserEnvDao = ChangeUserEnvDao.getInstance(cxt);
//		if(changedUserEnvDao.retrieveChangedUserEnv(fromTime, toTime, EnvType.LOCATION).size() > 0){
//			Log.d("LocContextMatcher", "moved");
//			return true;
//		} else
//			return false;
//	}
	
//	public List<DurationUserEnv> getDurationUserEnv(Date fromTime, Date toTime, EnvType envType){
//		List<DurationUserEnv> res = new ArrayList<DurationUserEnv>();
//		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(cxt);
//		DurationUserEnv headPiece = durationUserEnvDao.retrieveDurationUserEnv(fromTime, envType);
//		res.add(headPiece);
//		List<DurationUserEnv> middlePieces = durationUserEnvDao.retrieveDurationUserEnvBetween(fromTime, toTime, envType);
//		res.addAll(middlePieces);
//		DurationUserEnv tailPiece = durationUserEnvDao.retrieveDurationUserEnv(toTime, envType);
//		if(!headPiece.equals(tailPiece))
//			res.add(tailPiece);
//		return res;
//	}
}
