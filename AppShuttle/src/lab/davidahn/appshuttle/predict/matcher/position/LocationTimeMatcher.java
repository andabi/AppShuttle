package lab.davidahn.appshuttle.predict.matcher.position;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnv;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.collect.env.UserLoc;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.utils.Time;

public class LocationTimeMatcher extends LocationMatcher {

	public LocationTimeMatcher(MatcherConf conf) {
		super(conf);
	}

	@Override
	public MatcherType getType() {
		return MatcherType.LOCATION_TIME;
	}
	
	@Override
	protected List<DurationUserBhv> rejectNotUsedHistory(List<DurationUserBhv> durationUserBhvs, SnapshotUserCxt currUCxt) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvs){
			long timePast = currUCxt.getTime() - durationUserBhv.getTime();
			if(timePast < conf.getDuration() && timePast > conf.getTimeTolerance())
				res.add(durationUserBhv);
		}
		return res;
	}

	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(
			List<DurationUserBhv> durationUserBhvs, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager
				.getInstance();

		MatcherCountUnit matcherCountUnit = null;

		UserLoc lastKnownUserLoc = null;
		long accumulativeDuration = 0;
		for (DurationUserBhv durationUserBhv : durationUserBhvs) {
			for (DurationUserEnv durationUserEnv : durationUserEnvManager
					.retrieve(durationUserBhv.getTime(),
							durationUserBhv.getEndTime(), EnvType.LOCATION)) {
				UserLoc userLoc = (UserLoc) durationUserEnv.getUserEnv();
				long time = durationUserEnv.getTime();
				long duration = durationUserEnv.getDuration();
				if (lastKnownUserLoc == null) {
					matcherCountUnit = new MatcherCountUnit(
							durationUserBhv.getUserBhv());
					matcherCountUnit.setProperty("time", time);
					matcherCountUnit.setProperty("location", userLoc);
					matcherCountUnit.setProperty("duration", duration);
				} else {
					if (!userLoc.equals(lastKnownUserLoc)) {
						res.add(matcherCountUnit);
						matcherCountUnit = new MatcherCountUnit(
								durationUserBhv.getUserBhv());
						matcherCountUnit.setProperty("time", time);
						matcherCountUnit
								.setProperty("location", userLoc);
						matcherCountUnit.setProperty("duration",
								accumulativeDuration);
					}
				}
				accumulativeDuration += duration;
				lastKnownUserLoc = userLoc;
			}
		}
		
		if (matcherCountUnit != null)
			res.add(matcherCountUnit);

		return res;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit,
			SnapshotUserCxt uCxt) {
		long currTime = uCxt.getTime();
		long currTimePeriodic = currTime % conf.getTimePeriod();
		long targetTime = (Long) unit.getProperty("time");
		long targetTimePeriodic = targetTime % conf.getTimePeriod();

		long from = (currTimePeriodic - conf.getTimeTolerance() + conf
				.getTimePeriod()) % conf.getTimePeriod();
		long to = (currTimePeriodic + conf.getTimeTolerance())
				% conf.getTimePeriod();

		if (Time.isIncludedIn(from, targetTimePeriodic, to,
				conf.getTimePeriod())) {
			UserLoc userLoc = ((UserLoc) unit.getProperty("location"));
			try {
				UserLoc currLoc = (UserLoc) uCxt.getUserEnv(EnvType.LOCATION);
				int toleranceInMeter = conf.getPositionToleranceInMeter();
				if (userLoc.proximity(currLoc, toleranceInMeter))
					return userLoc.distanceTo(currLoc) / toleranceInMeter;
				else
					return 0;
			} catch (InvalidUserEnvException e) {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
