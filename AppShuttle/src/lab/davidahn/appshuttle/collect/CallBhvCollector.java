package lab.davidahn.appshuttle.collect;

import static lab.davidahn.appshuttle.Settings.preferenceSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.CallUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

public class CallBhvCollector implements BhvCollector {
	private static CallBhvCollector callBhvCollector;
	private ContentResolver contentResolver;
	private Date lastCallDate;

	private CallBhvCollector(Context cxt){
		contentResolver = cxt.getContentResolver();
	}
	
	public synchronized static CallBhvCollector getInstance(Context cxt){
		if(callBhvCollector == null) callBhvCollector = new CallBhvCollector(cxt);
		return callBhvCollector;
	}
	
	public List<UserBhv> collect(){
		List<UserBhv> res = new ArrayList<UserBhv>();
		return res;
	}
	
	public List<DurationUserBhv> refineDurationUserBhv(Date currTime, TimeZone currTimeZone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		
		if(lastCallDate == null){
			Date period = new Date(preferenceSettings.getLong("collection.call.initial_history.period", 5 * AlarmManager.INTERVAL_DAY));
			lastCallDate = new Date(currTime.getTime() - period.getTime());
		}
		Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.DATE + " > " + lastCallDate.getTime(), null, 
				CallLog.Calls.DATE + " ASC");
		while(cursor.moveToNext()) {
			int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
			int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
			int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
			int durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);
			int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);

			String number = cursor.getString(numIdx);
			String name = cursor.getString(nameIdx);
			if(name == null)
				name = number;
			Date date = new Date(cursor.getLong(dateIdx));
			Calendar calender = Calendar.getInstance();
			calender.setTime(date);
			TimeZone timeZone = calender.getTimeZone();
			long duration = cursor.getInt(durationIdx) * 1000;
			int type = cursor.getInt(typeIdx);
			
			if(type == CallLog.Calls.INCOMING_TYPE || type == CallLog.Calls.OUTGOING_TYPE) {
				DurationUserBhv durationUserBhv =  new DurationUserBhv.Builder()
				.setTime(date)
				.setEndTime(new Date(date.getTime() + duration))
				.setTimeZone(timeZone)
				.setBhv(new CallUserBhv(BhvType.CALL, number, name))
				.build();
				res.add(durationUserBhv);
			}
			lastCallDate = date;
		}
		cursor.close();
		return res;
	}
}
