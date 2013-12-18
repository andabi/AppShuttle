package lab.davidahn.appshuttle.collect.env;

import android.location.Location;

public class UserLoc extends UserEnv {
	protected double latitude;
	protected double longitude;
//	protected Validity _validity;

//	public UserLoc() {}
	
	public UserLoc(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
//		_validity = Validity.VALID;
	}
	
//	public static UserLoc create(UserLocValidity validity, double latitude, double longitude) {
//		if (validity == UserLocValidity.VALID)
//			return new UserLoc(latitude, longitude);
//		else
//			return InvalidUserLoc.getInstance();
//	}
//	
	public static UserLoc create(double latitude, double longitude) {
		return new UserLoc(latitude, longitude);
	}

	
//	public UserLoc(double longitude, double latitude, Validity validity) {
//		longitude = longitude;
//		latitude = latitude;
//		validity = validity;
//	}
	public double getLatitude() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return latitude;
	}
	public double getLongitude() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return longitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

//	public boolean isSame(UserEnv uLoc) throws InvalidUserEnvException {
////		if(_validity == Validity.INVALID || !uLoc.isValid()) 
////			throw new InvalidUserEnvException();
//		if(equals(uLoc))
//			return true;
//		else 
//			return false;
//	}
	
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID || !uLoc.isValid()) 
//			throw new InvalidUserEnvException();
		Location loc1 = new Location("loc1");
		Location loc2 = new Location("loc2");
		loc1.setLatitude(latitude);
		loc1.setLongitude(longitude);
		loc2.setLatitude(uLoc.getLatitude());
		loc2.setLongitude(uLoc.getLongitude());
		
		if(loc1.distanceTo(loc2) <= toleranceInMeter) return true;
		else return false;
	}
	
	public double distanceTo(UserLoc uLoc) throws InvalidUserEnvException {
		Location loc1 = new Location("loc1");
		Location loc2 = new Location("loc2");
		loc1.setLatitude(latitude);
		loc1.setLongitude(longitude);
		loc2.setLatitude(uLoc.getLatitude());
		loc2.setLongitude(uLoc.getLongitude());

		return loc1.distanceTo(loc2);
	}

	
	public EnvType getEnvType(){
		return EnvType.LOCATION;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
//		if(_validity == Validity.VALID)
		msg.append(" (").append(latitude).append(", ").append(longitude).append(") ");
//		else
//			msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UserLoc && latitude == ((UserLoc)o).latitude
				&& longitude == ((UserLoc)o).longitude)
//				&& _validity == ((UserLoc)o)._validity)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return Double.valueOf(latitude).hashCode() ^ Double.valueOf(longitude).hashCode();
	}
}