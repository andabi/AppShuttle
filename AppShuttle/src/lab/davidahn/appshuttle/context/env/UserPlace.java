package lab.davidahn.appshuttle.context.env;

import lab.davidahn.appshuttle.context.env.UserLoc.Validity;


public class UserPlace implements UserEnv {
	private String name;
	private UserLoc coordinates;
	private Validity validity;

	public UserPlace(double longitude, double latitude, String name) {
		coordinates = new UserLoc(longitude, latitude);
		this.name = name;
	}
	public UserPlace(double longitude, double latitude, String name, Validity validity) {
		coordinates = new UserLoc(longitude, latitude, validity);
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
	public UserLoc getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(UserLoc coordinates) {
		this.coordinates = coordinates;
	}
	public boolean isValid(){
		if(validity == Validity.VALID) 
			return true;
		else 
			return false;
	}
	public void setValidity(Validity validity) {
		this.validity = validity;
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
			msg.append(name).append(" (").append(coordinates).append(") ");
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