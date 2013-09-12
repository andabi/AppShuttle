package lab.davidahn.appshuttle.collect;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public interface BhvCollector {

	public <T extends UserBhv> List<T> collect();

	public List<DurationUserBhv> refineDurationUserBhv(Date currTime, TimeZone timeZone, List<UserBhv> userBhvList);
}