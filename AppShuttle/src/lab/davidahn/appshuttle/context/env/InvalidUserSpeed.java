package lab.davidahn.appshuttle.context.env;

public class InvalidUserSpeed extends UserSpeed {
	private static InvalidUserSpeed invalidUserSpeed = new InvalidUserSpeed();
	
	private InvalidUserSpeed() {
		super(0.0);
	}
	
	public static InvalidUserSpeed getInstance(){
		return invalidUserSpeed;
	}
		
	@Override
	public boolean isValid(){
		return false;
	}
	
	@Override
	public EnvType getEnvType(){
		return EnvType.INVALID_SPEED;
	}
	
	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof InvalidUserSpeed)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}