package lab.davidahn.appshuttle.bean.env;

public class UserEnv {
	protected EnvType envType;
	
	public UserEnv(EnvType envType) {
		this.envType = envType;
	}

	public EnvType getEnvType() {
		return envType;
	}
	
	public void setEnvType(EnvType envType) {
		this.envType = envType;
	}
}
