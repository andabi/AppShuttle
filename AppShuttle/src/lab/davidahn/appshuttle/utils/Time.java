package lab.davidahn.appshuttle.utils;


public class Time {
	public static boolean isIncludedIn(long fromTime, long time, long toTime, long period) {
		if(fromTime < 0 || fromTime >= period 
				|| time < 0 || time >= period
				|| toTime < 0 || toTime >= period)
			throw new IllegalArgumentException("out of time boundary: 0 <= time < " + period);
		
		if(toTime < fromTime)
			toTime += period;

		if(time < fromTime) 
			time += period;
		
		if(fromTime <= time && time <= toTime)
			return true;
		else 
			return false;
	}

//	public static long mean(long startTime, long endTime, long _period) {
//		if(endTime < startTime) 
//			endTime+=_period;
//		return ((startTime + endTime) / 2) % _period;
//	}
}
