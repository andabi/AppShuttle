package lab.davidahn.appshuttle.bean;

import lab.davidahn.appshuttle.exception.InvalidLocationException;
import android.location.Location;

public class UserLocExtendsLocation extends Location {
	private Validity validity;
	
	public UserLocExtendsLocation(double latitude, double longitude, Validity validity) {
		super(latitude+","+longitude);
		setLongitude(longitude);
		setLatitude(latitude);
		this.validity = validity;
	}
	public double getLongitude() {
		if(validity == Validity.INVALID)
			try {
				throw new InvalidLocationException();
			} catch (InvalidLocationException e) {
				e.printStackTrace();
			}
		return super.getLongitude();
	}
//	public void setLongitude(double longitude) {
//		this.longitude = longitude;
//	}
	public double getLatitude() {
		if(validity == Validity.INVALID)
			try {
				throw new InvalidLocationException();
			} catch (InvalidLocationException e) {
				e.printStackTrace();
			}
		return super.getLatitude();
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
			msg.append(" (").append(getLongitude()).append(", ").append(getLatitude()).append(") ");
		else
			msg.append("invalid");
		return msg.toString();
	}
	public boolean equals(UserLocExtendsLocation uLoc) throws InvalidLocationException {
		if(validity == Validity.INVALID || !uLoc.isValid()) throw new InvalidLocationException();
		if(getLatitude() == uLoc.getLatitude() && getLongitude() == uLoc.getLongitude()) return true;
		else return false;
	}

	public enum Validity{
		VALID, 
		INVALID
	}
}