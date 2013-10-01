package lab.davidahn.appshuttle.context.env;


public class InvalidUserLoc extends UserLoc {
	public InvalidUserLoc() {
		super(0,0);
	}
	
	public double getLongitude() throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}
	public double getLatitude() throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}
	public void setLongitude(double longitude) {
		this._longitude = longitude;
	}
	public void setLatitude(double latitude) {
		this._latitude = latitude;
	}
	public boolean isValid(){
		return false;
	}
	
	public boolean isSame(UserLoc uLoc) throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}
	
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
		throw new InvalidUserEnvException();
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();

		msg.append("invalid");
		
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof InvalidUserLoc)
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}

//	public enum Validity{
//		VALID, 
//		INVALID
//	}
}