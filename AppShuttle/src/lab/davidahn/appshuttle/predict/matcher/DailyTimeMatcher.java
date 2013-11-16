package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;


public class DailyTimeMatcher extends TimeMatcher {

	public DailyTimeMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getMatcherType(){
		return MatcherType.TIME_DAILY;
	}
}
