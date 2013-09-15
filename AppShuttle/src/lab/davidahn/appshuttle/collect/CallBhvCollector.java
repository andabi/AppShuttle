package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.CallUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CallLog;

public class CallBhvCollector implements BhvCollector {
	private static CallBhvCollector callBhvCollector;
	private ContentResolver contentResolver;
	private Date lastCallTimeDate;
	private SharedPreferences preferenceSettings;

	private CallBhvCollector(Context cxt){
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
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
	
	public List<DurationUserBhv> extractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = Collections.emptyList();
		if(lastCallTimeDate != null){
			res = extractCallBhvDuring(lastCallTimeDate, currTimeDate);
		}
		
		lastCallTimeDate = currTimeDate;

		return res;
	}
	
	private List<DurationUserBhv> extractCallBhvDuring(Date beginTime, Date endTime){
		Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, 
				CallLog.Calls.DATE + " >= " + beginTime.getTime() + "AND" + CallLog.Calls.DATE + " < " + endTime.getTime(),
				null, 
				CallLog.Calls.DATE + " ASC"
				);
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
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
			
			if(duration > 0 && (type == CallLog.Calls.INCOMING_TYPE || type == CallLog.Calls.OUTGOING_TYPE)) {
				DurationUserBhv durationUserBhv =  new DurationUserBhv.Builder()
				.setTime(date)
				.setEndTime(new Date(date.getTime() + duration))
				.setTimeZone(timeZone)
				.setBhv(new CallUserBhv(BhvType.CALL, number, name))
				.build();
				res.add(durationUserBhv);
			}
		}
		cursor.close();
		return res;
	}

	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		Date initialHistoryPeriod = new Date(preferenceSettings.getLong("collection.call.initial_history.period", 5 * AlarmManager.INTERVAL_DAY));
		lastCallTimeDate = new Date(currTimeDate.getTime() - initialHistoryPeriod.getTime());
		List<DurationUserBhv> res = Collections.emptyList();
		res = extractCallBhvDuring(lastCallTimeDate, currTimeDate);

		lastCallTimeDate = currTimeDate;

		return res;
	}
}
