package lab.davidahn.appshuttle.context.env;


public class UserSpeed extends UserEnv {
	protected double speed;
	protected int level;

	public UserSpeed(double _speed) {
		speed = _speed;
		level = (int)Math.round(_speed);
	}
	
	public static UserSpeed create(double speed) {
		if(speed == 0.0)
			return ZeroUserSpeed.getInstance();
		
		return new UserSpeed(speed);
	}

	public double getSpeed() throws InvalidUserEnvException {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public EnvType getEnvType(){
		return EnvType.SPEED;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(" (").append(speed).append(", ").append(level).append(") ");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UserSpeed && level == ((UserSpeed)o).level)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return Double.valueOf(level).hashCode();
	}
}