package lab.davidahn.appshuttle.commons;


public class Time {
	public static boolean isBetween(long startTime, long time, long endTime, long period) {
		if(time < startTime) 
			time+=period;
		if(endTime < startTime) 
			time+=period;
		
		if(startTime <= time && time < endTime)
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
