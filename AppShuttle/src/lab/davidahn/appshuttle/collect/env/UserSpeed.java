package lab.davidahn.appshuttle.collect.env;


public class UserSpeed extends UserEnv {
	protected double speed;
	protected Level level;

	public UserSpeed(double _speed) {
		speed = _speed;
		level = getLevel(_speed);
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
	
	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public EnvType getEnvType(){
		return EnvType.SPEED;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("(").append(speed).append(", ").append(level.name()).append(")");
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
		return Double.valueOf(level.name()).hashCode();
	}
	
	private Level getLevel(double speed) {
		double speedKmh = speed * 3.6;
		if(speedKmh < 0)
			return Level.INVALID;
		else if(speedKmh < 3.0)
			return Level.STAY;
		else if(speedKmh < 5.0)
			return Level.WALK;
		else
			return Level.VEHICLE;
	}
	
	public enum Level {
		STAY,
		WALK,
		VEHICLE,
		INVALID,
	}
}