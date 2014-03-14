package lab.davidahn.appshuttle.collect.env;


public abstract class UserEnv {
	public boolean isValid() {
		return true;
	}
	
	public abstract EnvType getEnvType();
	
	public String toString(){
		return getEnvType().name();
	}
}
