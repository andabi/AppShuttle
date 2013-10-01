package lab.davidahn.appshuttle.context.env;


public class UserPlace implements UserEnv {
	protected String _name;
	protected UserLoc _coordinates;
//	protected Validity _validity;

//	public UserPlace() {}

	public UserPlace(String name, UserLoc coordinates) {
		this._name = name;
		_coordinates = coordinates;
	}
	
	public static UserPlace create(String name, UserLoc coordinates) {
		if(name == null || name.equals(""))
			return new InvalidUserPlace();
		else
			return new UserPlace(name, coordinates);
	}
//	public UserPlace(double longitude, double latitude, String name, Validity validity) {
//		coordinates = new UserLoc(longitude, latitude, validity);
//		this.name = name;
//	}
	public String getName() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return _name;
	}
	public void setName(String name) {
		this._name = name;
	}
	public UserLoc getCoordinates() {
		return _coordinates;
	}
	public void setCoordinates(UserLoc coordinates) {
		this._coordinates = coordinates;
	}
	public boolean isValid(){
//		if(_validity == Validity.VALID) 
			return true;
//		else 
//			return false;
	}
	
	public boolean isSame(UserPlace uPlace) throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID || !uPlace.isValid()) 
//			throw new InvalidUserEnvException();
		if(_name.equals(uPlace.getName())) 
			return true;
		else 
			return false;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
//		if(_validity == Validity.VALID)
		msg.append(_name).append(" (").append(_coordinates).append(") ");
//		else
//			msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UserPlace && _name.equals(((UserPlace)o)._name))
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
//		if(_name == null)
//			return 0;
					
		return _name.hashCode();
	}
}