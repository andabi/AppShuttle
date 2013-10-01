package lab.davidahn.appshuttle.context.env;

import android.location.Location;

public class UserLoc implements UserEnv {
	protected double _longitude;
	protected double _latitude;
//	protected Validity _validity;

//	public UserLoc() {}
	
	public UserLoc(double longitude, double latitude) {
		_longitude = longitude;
		_latitude = latitude;
//		_validity = Validity.VALID;
	}
	
	public static UserLoc create(UserLocValidity validity, double longitude, double latitude) {
		if (validity == UserLocValidity.VALID)
			return new UserLoc(longitude, longitude);
		else
			return new InvalidUserLoc();
	}
	
//	public UserLoc(double longitude, double latitude, Validity validity) {
//		longitude = longitude;
//		latitude = latitude;
//		validity = validity;
//	}
	public double getLongitude() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return _longitude;
	}
	public double getLatitude() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return _latitude;
	}
	public void setLongitude(double longitude) {
		_longitude = longitude;
	}
	public void setLatitude(double latitude) {
		_latitude = latitude;
	}
	public boolean isValid(){
//		if(_validity == Validity.VALID) 
//			return true;
//		else 
		return false;
	}

	public boolean isSame(UserLoc uLoc) throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID || !uLoc.isValid()) 
//			throw new InvalidUserEnvException();
		if(equals(uLoc))
			return true;
		else 
			return false;
	}
	
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID || !uLoc.isValid()) 
//			throw new InvalidUserEnvException();
		Location loc1 = new Location("loc1");
		Location loc2 = new Location("loc2");
		loc1.setLongitude(_longitude);
		loc1.setLatitude(_latitude);
		loc2.setLongitude(uLoc.getLongitude());
		loc2.setLatitude(uLoc.getLatitude());
		
		if(loc1.distanceTo(loc2) <= toleranceInMeter) return true;
		else return false;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
//		if(_validity == Validity.VALID)
		msg.append(" (").append(_longitude).append(", ").append(_latitude).append(") ");
//		else
//			msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UserLoc && _latitude == ((UserLoc)o)._latitude
				&& _longitude == ((UserLoc)o)._longitude)
//				&& _validity == ((UserLoc)o)._validity)
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return Double.valueOf(_longitude).hashCode() ^ Double.valueOf(_latitude).hashCode();
	}
	
	public enum UserLocValidity{
		VALID, 
		INVALID
	}
}