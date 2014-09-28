package lab.davidahn.appshuttle.predict.matcher.time;

import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;


public class DailyTimeMatcher extends TimeMatcher {

	public DailyTimeMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public MatcherType getType(){
		return MatcherType.TIME_DAILY;
	}
}
