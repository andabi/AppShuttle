package lab.davidahn.appshuttle.collect.bhv;

import java.util.List;
import java.util.TimeZone;


public interface BhvCollector {
	public <T extends UserBhv> List<T> collect();
	public List<DurationUserBhv> preExtractDurationUserBhv(long currTime, TimeZone currTimeZone);
	public List<DurationUserBhv> extractDurationUserBhv(long currTime, TimeZone currTimeZone, List<UserBhv> userBhvList);
	public List<DurationUserBhv> postExtractDurationUserBhv(long currTime, TimeZone currTimeZone);
}