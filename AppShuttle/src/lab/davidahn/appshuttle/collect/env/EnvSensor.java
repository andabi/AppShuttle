package lab.davidahn.appshuttle.collect.env;

import java.util.List;
import java.util.TimeZone;


public interface EnvSensor {
	public UserEnv senseAndGet(long currTime, TimeZone currTimeZone);	
	public boolean isChanged();
	public List<DurationUserEnv> preExtractDurationUserEnv(long currTime, TimeZone currTimeZone);
	public DurationUserEnv extractDurationUserEnv(long currTime, TimeZone currTimeZone, UserEnv uEnv);
	public DurationUserEnv postExtractDurationUserEnv(long currTime, TimeZone currTimeZone);
}