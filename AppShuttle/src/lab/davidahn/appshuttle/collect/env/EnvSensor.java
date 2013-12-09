package lab.davidahn.appshuttle.collect.env;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public interface EnvSensor {
	public UserEnv sense(Date currTimeDate, TimeZone currTimeZone);	
	public boolean isChanged();
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone);
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv);
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone);
}