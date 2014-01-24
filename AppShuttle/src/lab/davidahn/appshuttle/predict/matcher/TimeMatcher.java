package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit.Builder;
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
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevDurationUserBhv = null;
		MatcherCountUnit.Builder matcherCountUnitBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevDurationUserBhv == null){
				matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv);
			} else {
				if(durationUserBhv.getTimeDate().getTime() - prevDurationUserBhv.getEndTimeDate().getTime()
						< conf.getAcceptanceDelay()){
					matcherCountUnitBuilder.setProperty("endTime", durationUserBhv.getEndTimeDate());
				} else {
					res.add(matcherCountUnitBuilder.build());
					matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv);
				}
			}
			prevDurationUserBhv = durationUserBhv;
		}
		
		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		
		return res;
	}
	
	private Builder makeMatcherCountUnitBuilder(DurationUserBhv durationUserBhv) {
		return new MatcherCountUnit.Builder(durationUserBhv.getUserBhv())
		.setProperty("time", durationUserBhv.getTimeDate())
		.setProperty("endTime", durationUserBhv.getEndTimeDate())
		.setProperty("timeZone", durationUserBhv.getTimeZone());
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
					long from = (uniqueTimeElem - conf.getTolerance() + conf.getPeriod()) % conf.getPeriod();
					long to = (uniqueTimeElem + conf.getTolerance()) % conf.getPeriod();
					if(Time.isIncludedIn(from, timePeriodic, to, conf.getPeriod())){
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
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		double relatedness = 0;
		
		long currTime = uCxt.getTimeDate().getTime();
		long currTimePeriodic = currTime % conf.getPeriod();
		long targetTime = ((Date) unit.getProperty("time")).getTime();
		long targetTimePeriodic = targetTime % conf.getPeriod();
		
		long mean = currTimePeriodic;
		long std = conf.getTolerance() / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		long from = (currTimePeriodic - conf.getTolerance() + conf.getPeriod()) % conf.getPeriod();
		long to = (currTimePeriodic + conf.getTolerance()) % conf.getPeriod();
				
		if(Time.isIncludedIn(from, targetTimePeriodic, to, conf.getPeriod())){
			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory,
		Map<MatcherCountUnit, Double> relatedHistoryMap,
		SnapshotUserCxt uCxt) {
		return 1;
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
	
	protected boolean isWeekDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return false;
		
		return true;
	}
}
