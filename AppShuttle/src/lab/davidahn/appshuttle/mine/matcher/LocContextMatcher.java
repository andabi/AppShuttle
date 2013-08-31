package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidLocationException;
import lab.davidahn.appshuttle.context.env.LocUserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import android.content.Context;

/**
 * K-NN based algorithm
 * @author andabi
 *
 */
public class LocContextMatcher extends ContextMatcher {
	int toleranceInMeter;

	public LocContextMatcher(Context cxt, double minLikelihood, int minNumCxt, int toleranceInMeter) {
		super(cxt, minLikelihood, minNumCxt);
		this.toleranceInMeter = toleranceInMeter;
		matcherType = MatcherType.LOCATION;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> rfdUCxtList) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		MatcherCountUnit.Builder mergedRfdUCxtBuilder = null;
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
			mergedRfdUCxtBuilder = new MatcherCountUnit.Builder(rfdUCxt.getBhv());
			mergedRfdUCxtBuilder.addRfdUserCxtList(rfdUCxt);
			res.add(mergedRfdUCxtBuilder.build());
		}
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
	protected double calcRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		return 1;
	}
	
	@Override
	protected double calcLikelihood(int numTotalCxt, int numRelatedCxt, Map<MatcherCountUnit, Double> relatedCxtMap, SnapshotUserCxt uCxt){
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(cxt);
		long totalSpentTime = 0, validSpentTime = 0;
		
		for(MatcherCountUnit unit : relatedCxtMap.keySet()){
			DurationUserBhv rfdUCxt = unit.getRfdUserCxtList().get(0);
			for(DurationUserEnv durationUserEnv : durationUserEnvDao.retrieveDurationUserEnv(rfdUCxt.getTime(), rfdUCxt.getEndTime(), EnvType.LOCATION)){
				UserLoc userLoc = ((LocUserEnv)durationUserEnv.getUserEnv()).getLoc();
				long duration = durationUserEnv.getDuration();
				totalSpentTime += duration;
				try{
					if(userLoc.proximity(((LocUserEnv) uCxt.getUserEnv(EnvType.LOCATION)).getLoc(), toleranceInMeter)){
						validSpentTime += duration;
					}
				} catch (InvalidLocationException e) {
					;
				}

			}
		}
		
		double likelihood = 0;
		if(totalSpentTime > 0)
			likelihood = 1.0 * validSpentTime / totalSpentTime;
		return likelihood;
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
