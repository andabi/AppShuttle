package lab.davidahn.appshuttle.context.env;

public class ZeroUserSpeed extends UserSpeed {
	private static ZeroUserSpeed zeroUserSpeed = new ZeroUserSpeed();
	
	private ZeroUserSpeed() {
		super(0.0);
	}
	
	public static ZeroUserSpeed getInstance(){
		return zeroUserSpeed;
	}
		
	@Override
	public EnvType getEnvType(){
		return EnvType.ZERO_SPEED;
	}
	
	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("zero");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ZeroUserSpeed)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}