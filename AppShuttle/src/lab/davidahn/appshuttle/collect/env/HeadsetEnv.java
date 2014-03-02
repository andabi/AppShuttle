package lab.davidahn.appshuttle.collect.env;


public class HeadsetEnv extends UserEnv {
	private static HeadsetEnv pluggedOn = new HeadsetEnv(true);
	private static HeadsetEnv pluggedOff = new HeadsetEnv(false);
	
	private final boolean isPlugged;
	
	private HeadsetEnv(boolean _isPlugged) {
		isPlugged = _isPlugged;
	}
	
	public static HeadsetEnv getInstance(boolean _isPlugged) {
		if(_isPlugged) return pluggedOn;
		else return pluggedOff;
	}

	public boolean isPlugged() {
		return isPlugged;
	}

	public EnvType getEnvType(){
		return EnvType.HEADSET;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("isPlugged: ").append(isPlugged);
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof HeadsetEnv && isPlugged == ((HeadsetEnv)o).isPlugged)
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		if(isPlugged) return 1;
		else return 0;
	}
}