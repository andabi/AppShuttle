package lab.davidahn.appshuttle.context.env;


public abstract class UserEnv {
	public boolean isValid() {
		return true;
	}
	public abstract EnvType getEnvType();
}
