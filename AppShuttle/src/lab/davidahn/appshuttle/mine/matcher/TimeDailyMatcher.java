package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.commons.Time;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

import org.apache.commons.math3.distribution.NormalDistribution;

import android.app.AlarmManager;

public class TimeDailyMatcher extends BaseMatcher {
	protected long _period;
	protected long _tolerance;
	protected long _acceptanceDelay;

	public TimeDailyMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumCxt, long period, long tolerance, long acceptanceDelay) {
		super(duration, minLikelihood, minInverseEntropy, minNumCxt);
		_period = period;
		
		if(tolerance > AlarmManager.INTERVAL_DAY)
			throw new IllegalArgumentException("tolerance should not exceed 24 hours");
		
		_tolerance = tolerance;
		_acceptanceDelay = acceptanceDelay;
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY;
	}
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		DurationUserBhvDao rfdUserCxtDao = DurationUserBhvDao.getInstance();

		Date toTime = new Date(currUCxt.getTimeDate().getTime() - _tolerance);
		Date fromTime = new Date(toTime.getTime() - _duration);
		
		List<DurationUserBhv> rfdUCxtList = rfdUserCxtDao.retrieveByBhv(fromTime, toTime, uBhv);
		List<DurationUserBhv> pureRfdUCxtList = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv rfdUCxt : rfdUCxtList){
//			if(rfdUCxt.getEndTimeDate().getTime() - rfdUCxt.getTimeDate().getTime()	< noiseTimeTolerance)
//				continue;
			pureRfdUCxtList.add(rfdUCxt);
		}
		return pureRfdUCxtList;
	}
	
	@Override
	protected List<MatcherCountUnit> mergeCxtByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
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
						< _acceptanceDelay){
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
		long currTimePeriodic = currTime % _period;
		long targetTime = ((Date) unit.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % _period;
		
		long mean = currTimePeriodic;
		long std = _tolerance / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		long start = (currTimePeriodic - _tolerance) % _period;
		long end = (currTimePeriodic + _tolerance) % _period;
				
		if(Time.isBetween(start, targetTimePeriodic, end, _period)){
			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		assert(matcherCountUnitList.size() >= _minNumCxt);
		
		double inverseEntropy = 0;
		Set<Long> uniqueTime = new HashSet<Long>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			long time = ((Date) unit.getProperty("time")).getTime();
			long timePeriodic = time % _period;
			Iterator<Long> it = uniqueTime.iterator();
			boolean unique = true;
			if(!uniqueTime.isEmpty()){
				while(it.hasNext()){
					Long uniqueTimeElem = it.next();
					if(Time.isBetween((uniqueTimeElem - _tolerance) % _period, timePeriodic, (uniqueTimeElem + _tolerance) % _period, _period)){
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
	protected double computeScore(MatcherResult matchedResult) {
		double likelihood = matchedResult.getLikelihood();
		double inverseEntropy = matchedResult.getInverseEntropy();
		double numRelatedCxt = 1.0 * matchedResult.getNumRelatedCxt();
		
		double score = 1 + 0.5 * (inverseEntropy * (numRelatedCxt / _duration)) + 0.1 * likelihood;

		assert(1 <= score && score <=2);
		return score;
	}
}
