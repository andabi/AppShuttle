package lab.davidahn.appshuttle.context.env;

import android.location.Location;


public class UserLoc {
	protected double longitude;
	protected double latitude;
	protected Validity validity;

	public UserLoc(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.validity = Validity.VALID;
	}
	public UserLoc(double longitude, double latitude, Validity validity) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.validity = validity;
	}
	public double getLongitude() throws InvalidUserEnvException {
		if(validity == Validity.INVALID)
			throw new InvalidUserEnvException();
		return longitude;
	}
	public double getLatitude() throws InvalidUserEnvException {
		if(validity == Validity.INVALID)
			throw new InvalidUserEnvException();
		return latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
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
	
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
		if(validity == Validity.INVALID || !uLoc.isValid()) 
			throw new InvalidUserEnvException();
		Location loc1 = new Location("loc1");
		Location loc2 = new Location("loc2");
		loc1.setLongitude(longitude);
		loc1.setLatitude(latitude);
		loc2.setLongitude(uLoc.getLongitude());
		loc2.setLatitude(uLoc.getLatitude());
		
		if(loc1.distanceTo(loc2) <= toleranceInMeter) return true;
		else return false;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		if(validity == Validity.VALID)
			msg.append(" (").append(longitude).append(", ").append(latitude).append(") ");
		else
			msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		try {
			if(getLatitude() == ((UserLoc)o).getLatitude()
					&& getLongitude() == ((UserLoc)o).getLongitude()) 
				return true;
			else return false;
		} catch (InvalidUserEnvException e) {
			if(validity == Validity.INVALID && !((UserLoc)o).isValid())
				return true;
			else
				return false;
		}
	}
	
	@Override
	public int hashCode(){
		return 0;
	}

	public enum Validity{
		VALID, 
		INVALID
	}
}