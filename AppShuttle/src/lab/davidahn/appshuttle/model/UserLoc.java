package lab.davidahn.appshuttle.model;

import lab.davidahn.appshuttle.exception.InvalidLocationException;

public class UserLoc {
	private Validity validity;
	private double longitude;
	private double latitude;
	
	public UserLoc(double latitude, double longitude, Validity validity) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.validity = validity;
	}
	public double getLongitude() throws InvalidLocationException{
		if(validity == Validity.INVALID)
			throw new InvalidLocationException();
		return longitude;
	}
//	public void setLongitude(double longitude) {
//		this.longitude = longitude;
//	}
	public double getLatitude() throws InvalidLocationException{
		if(validity == Validity.INVALID)
			throw new InvalidLocationException();
		return latitude;
	}
//	public void setLatitude(double latitude) {
//		this.latitude = latitude;
//	}
//	public Validity getValidity() {
//		return validity;
//	}
	public boolean isValid(){
		if(validity == Validity.VALID) return true;
		else return false;
	}
		//	public void setValidity(Validity validity) {
//		this.validity = validity;
//	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		if(validity == Validity.VALID)
			msg.append(" (").append(longitude).append(", ").append(latitude).append(") ");
		else
			msg.append("invalid");
		return msg.toString();
	}
	public boolean equals(UserLoc uLoc) throws InvalidLocationException {
		if(validity == Validity.INVALID || !uLoc.isValid()) throw new InvalidLocationException();
		if(getLatitude() == uLoc.getLatitude() && getLongitude() == uLoc.getLongitude()) return true;
		else return false;
	}

	public enum Validity{
		VALID, 
		INVALID
	}
}