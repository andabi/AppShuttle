package lab.davidahn.appshuttle.predict.comparator;

import java.util.Comparator;

public class TimeComparator implements Comparator<Long> {
	private long acceptInterval = 0;
	
	/**
	 * 
	 * @param acceptInterval (in ms)
	 */
	TimeComparator(long acceptInterval) {
		if (acceptInterval > 0)
			this.acceptInterval = acceptInterval;
	}
	
	@Override
	public int compare(Long lhs, Long rhs) {
		// TODO Auto-generated method stub
		if (Math.abs(lhs - rhs) <= acceptInterval)
			return 0;
		
		return (int)(lhs - rhs);
	}
}
