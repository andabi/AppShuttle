package lab.davidahn.appshuttle;

import lab.davidahn.appshuttle.context.env.InvalidLocationException;
import lab.davidahn.appshuttle.context.env.UserLoc;
import android.location.Location;

public class Utils {
	public static boolean Proximity(UserLoc uLoc1, UserLoc uLoc2, double toleranceInMeter) throws InvalidLocationException {
		Location loc1 = new Location("loc1");
		loc1.setLongitude(uLoc1.getLongitude());
		loc1.setLatitude(uLoc1.getLatitude());
		Location loc2 = new Location("loc2");
		loc2.setLongitude(uLoc2.getLongitude());
		loc2.setLatitude(uLoc2.getLatitude());
		
		if(loc1.distanceTo(loc2) <= toleranceInMeter) return true;
		else return false;
	}
}
