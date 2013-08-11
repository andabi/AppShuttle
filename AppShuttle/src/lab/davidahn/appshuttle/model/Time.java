package lab.davidahn.appshuttle.model;

import android.app.AlarmManager;

public class Time {
	public static boolean isBetween(long startTime, long time, long endTime) {
		if(time < startTime) time+=AlarmManager.INTERVAL_DAY;
		if(endTime < startTime) time+=AlarmManager.INTERVAL_DAY;
		if(startTime <= time && time <= endTime) return true;
		else return false;
	}
}
