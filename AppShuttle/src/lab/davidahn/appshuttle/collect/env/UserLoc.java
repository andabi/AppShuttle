package lab.davidahn.appshuttle.collect.env;

import android.location.Location;

public class UserLoc extends UserEnv {
	protected double latitude;
	protected double longitude;
	
	public UserLoc(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public static UserLoc create(double latitude, double longitude) {
		return new UserLoc(latitude, longitude);
	}

	public double getLatitude() throws InvalidUserEnvException {
		return latitude;
	}
	public double getLongitude() throws InvalidUserEnvException {
		return longitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
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
		msg.append("(").append(latitude).append(", ").append(longitude).append(")");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UserLoc && latitude == ((UserLoc)o).latitude
				&& longitude == ((UserLoc)o).longitude)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return Double.valueOf(latitude).hashCode() ^ Double.valueOf(longitude).hashCode();
	}
}