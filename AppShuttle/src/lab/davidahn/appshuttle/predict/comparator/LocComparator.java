package lab.davidahn.appshuttle.predict.comparator;

import java.util.Comparator;
import lab.davidahn.appshuttle.collect.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.collect.env.UserLoc;

/**
 * LocationComparator Example
 * @author carrot
 *
 */
public class LocComparator implements Comparator<UserLoc> {
	private double acceptDist = 1000; // 1km
	
	LocComparator(double acceptDist) {
		if (acceptDist > 1000)
			this.acceptDist = acceptDist;
	}

	@Override
	public int compare(UserLoc lhs, UserLoc rhs) {
		try {
			if (lhs.distanceTo(rhs) > acceptDist)
				return -1;
			else
				return 0;
		} catch (InvalidUserEnvException e) {
			return -1;
		}
	}
}
