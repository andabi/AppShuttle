package lab.davidahn.appshuttle.context.env;


public class UserPlace extends UserLoc {
	private String name;

	public UserPlace(double longitude, double latitude, String name) {
		super(longitude, latitude);
		this.name = name;
	}
	public UserPlace(double longitude, double latitude, String name, Validity validity) {
		super(longitude, latitude, validity);
		this.name = name;
	}
	public String getName() throws InvalidUserEnvException {
		if(validity == Validity.INVALID)
			throw new InvalidUserEnvException();
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isSame(UserPlace uPlace) throws InvalidUserEnvException {
		if(validity == Validity.INVALID || !uPlace.isValid()) 
			throw new InvalidUserEnvException();
		if(name.equals(uPlace.getName())) 
			return true;
		else 
			return false;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		if(validity == Validity.VALID)
			msg.append(name).append(" (").append(longitude).append(", ").append(latitude).append(") ");
		else
			msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(!super.equals(o))
			return false;
		else {
			if(name.equals(((UserPlace)o).name))
				return true;
			else return false;
		}
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}