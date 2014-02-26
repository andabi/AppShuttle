package lab.davidahn.appshuttle.predict.matcher;

import lab.davidahn.appshuttle.predict.matcher.conf.TimeMatcherConf;


public class DailyTimeMatcher extends TimeMatcher {

	public DailyTimeMatcher(TimeMatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.TIME_DAILY;
	}

	@Override
	public int getPriority() {
		return MatcherType.TIME_DAILY.priority;
	}
}
