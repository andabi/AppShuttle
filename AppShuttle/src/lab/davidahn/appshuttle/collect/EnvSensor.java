package lab.davidahn.appshuttle.collect;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;

public interface EnvSensor {

	public <T extends UserEnv> T sense();
	
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv);
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone);
	
}
