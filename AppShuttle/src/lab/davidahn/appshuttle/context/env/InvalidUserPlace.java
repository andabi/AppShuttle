package lab.davidahn.appshuttle.context.env;


public class InvalidUserPlace extends UserPlace {
	public InvalidUserPlace() {
		super("", new InvalidUserLoc());
	}
	
	public String getName() throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}

	public boolean isValid(){
		return false;
	}
	
	public boolean isSame(UserPlace uPlace) throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}
	
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