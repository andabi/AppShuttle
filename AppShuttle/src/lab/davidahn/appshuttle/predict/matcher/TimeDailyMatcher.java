package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;


public class TimeDailyMatcher extends TimeMatcher {

	public TimeDailyMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY;
	}
}
