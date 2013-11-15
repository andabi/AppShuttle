package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;
import lab.davidahn.appshuttle.utils.Time;

import org.apache.commons.math3.distribution.NormalDistribution;

public abstract class TimeMatcher extends BaseMatcher<TimeMatcherConf> {

	public TimeMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public abstract MatcherType getMatcherType();
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		Date toTime = new Date(currUCxt.getTimeDate().getTime() - conf.getTolerance());
		Date fromTime = new Date(toTime.getTime() - conf.getDuration());
		
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
//			if(durationUserBhv.getEndTimeDate().getTime() - durationUserBhv.getTimeDate().getTime()	< noiseTimeTolerance)
//				continue;
			pureDurationUserBhvList.add(durationUserBhv);
		}
		return pureDurationUserBhvList;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeHistoryByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevDurationUserBhv = null;
		MatcherCountUnit.Builder matcherCountUnitBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevDurationUserBhv == null){
				matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
				matcherCountUnitBuilder.setProperty("time", durationUserBhv.getTimeDate());
				matcherCountUnitBuilder.setProperty("endTime", durationUserBhv.getEndTimeDate());
				matcherCountUnitBuilder.setProperty("timeZone", durationUserBhv.getTimeZone());
			} else {
				if(durationUserBhv.getTimeDate().getTime() - prevDurationUserBhv.getEndTimeDate().getTime()
						< conf.getAcceptanceDelay()){
					matcherCountUnitBuilder.setProperty("endTime", durationUserBhv.getEndTimeDate());
				} else {
					res.add(matcherCountUnitBuilder.build());
					matcherCountUnitBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
					matcherCountUnitBuilder.setProperty("time", durationUserBhv.getTimeDate());
					matcherCountUnitBuilder.setProperty("endTime", durationUserBhv.getEndTimeDate());
					matcherCountUnitBuilder.setProperty("timeZone", durationUserBhv.getTimeZone());
				}
			}
			prevDurationUserBhv = durationUserBhv;
		}
		
		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		double relatedness = 0;
		
		long currTime = uCxt.getTimeDate().getTime();
		long currTimePeriodic = currTime % conf.getPeriod();
		long targetTime = ((Date) unit.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % conf.getPeriod();
		
		long mean = currTimePeriodic;
		long std = conf.getTolerance() / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		long start = (currTimePeriodic - conf.getTolerance()) % conf.getPeriod();
		long end = (currTimePeriodic + conf.getTolerance()) % conf.getPeriod();
				
		if(Time.isBetween(start, targetTimePeriodic, end, conf.getPeriod())){
			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= conf.getMinNumHistory());
		
		double inverseEntropy = 0;
		Set<Long> uniqueTime = new HashSet<Long>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			long time = ((Date) unit.getProperty("time")).getTime();
			long timePeriodic = time % conf.getPeriod();
			Iterator<Long> it = uniqueTime.iterator();
			boolean unique = true;
			if(!uniqueTime.isEmpty()){
				while(it.hasNext()){
					Long uniqueTimeElem = it.next();
					if(Time.isBetween((uniqueTimeElem - conf.getTolerance()) % conf.getPeriod(), timePeriodic, (uniqueTimeElem + conf.getTolerance()) % conf.getPeriod(), conf.getPeriod())){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniqueTime.add(timePeriodic);
		}
		int entropy = uniqueTime.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		double inverseEntropy = matcherResult.getInverseEntropy();
		double numRelatedHistory = 1.0 * matcherResult.getNumRelatedCxt();
		
		double score = 1 + 0.5 * (inverseEntropy * (numRelatedHistory / conf.getDuration())) + 0.1 * likelihood;

		assert(1 <= score && score <=2);
		return score;
	}
}
