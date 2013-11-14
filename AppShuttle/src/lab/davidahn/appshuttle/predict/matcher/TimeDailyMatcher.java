package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;


public class TimeDailyMatcher extends TimeMatcher {

//	public TimeDailyMatcher(long duration, double minLikelihood, double minInverseEntropy, int minNumHistory, long period, long tolerance, long acceptanceDelay) {
//		super(duration, minLikelihood, minInverseEntropy, minNumHistory, period, tolerance, acceptanceDelay);
//	}

	public TimeDailyMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY;
	}
}
