package lab.davidahn.appshuttle.collect;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;

public interface EnvSensor {

	public <T extends UserEnv> T sense();
	
	public DurationUserEnv refineDurationUserEnv(SnapshotUserCxt uCxt);
}
