package lab.davidahn.appshuttle.collect.bhv;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public interface BhvCollector {
	public <T extends UserBhv> List<T> collect();
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone);
	public List<DurationUserBhv> extractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone, List<UserBhv> userBhvList);
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone);
}