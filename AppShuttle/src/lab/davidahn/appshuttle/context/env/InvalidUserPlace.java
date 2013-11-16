package lab.davidahn.appshuttle.context.env;


public class InvalidUserPlace extends UserPlace {
	private static InvalidUserPlace invalidUserPlace = new InvalidUserPlace();
	
	private InvalidUserPlace() {
		super("", InvalidUserLoc.getInstance());
	}
	
	public static InvalidUserPlace getInstance(){
		return invalidUserPlace;
	}
	
	@Override
	public String getName() throws InvalidUserEnvException {
		throw new InvalidUserEnvException(EnvType.INVALID_PLACE, this);
	}
	
	@Override
	public boolean isValid(){
		return false;
	}
	
//	@Override
//	public boolean isSame(UserPlace uPlace) throws InvalidUserEnvException {
//		throw new InvalidUserEnvException(EnvType.INVALID_PLACE, this);
//	}
	
	@Override
	public EnvType getEnvType(){
		return EnvType.INVALID_PLACE;
	}
	
	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof InvalidUserPlace)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}