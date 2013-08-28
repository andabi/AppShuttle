package lab.davidahn.appshuttle.commons;

import android.app.AlarmManager;

public class Time {
	public static boolean isBetween(long startTime, long time, long endTime) {
		if(time < startTime) time+=AlarmManager.INTERVAL_DAY;
		if(endTime < startTime) time+=AlarmManager.INTERVAL_DAY;
		if(startTime < time && time < endTime) return true;
		else return false;
	}

	public static long mean(long startTime, long endTime) {
		if(endTime < startTime) endTime+=AlarmManager.INTERVAL_DAY;
		return ((startTime + endTime) / 2) % AlarmManager.INTERVAL_DAY;
	}
}
