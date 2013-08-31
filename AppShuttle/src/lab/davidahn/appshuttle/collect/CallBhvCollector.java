package lab.davidahn.appshuttle.collect;

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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CallLog;

public class CallBhvCollector implements BhvCollector {
	private static CallBhvCollector callBhvCollector;
//	private TelephonyManager telephonyManager;
	private ContentResolver contentResolver;
	private SharedPreferences settings;
	private Date lastCallDate;

//	private Map<UserBhv, DurationUserBhv.Builder> ongoingBhvMap;
	
	private CallBhvCollector(Context cxt){
//	    telephonyManager = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		contentResolver = cxt.getContentResolver();
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);

//		ongoingBhvMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
	}
	
	public static CallBhvCollector getInstance(Context cxt){
		if(callBhvCollector == null) callBhvCollector = new CallBhvCollector(cxt);
		return callBhvCollector;
	}
	
	public List<UserBhv> collect(){
		List<UserBhv> res = new ArrayList<UserBhv>();
		
		//TODO
//		if(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
//			String bhvName = telephonyManager.getLine1Number();
//			res.add(new UserBhv(BhvType.CALL, bhvName));
//		}
		return res;
	}
	
	public List<DurationUserBhv> refineDurationUserBhv(Date currTime, TimeZone currTimeZone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		
		if(lastCallDate == null){
//			lastCallDate = currTime;
			lastCallDate = new Date(settings.getLong("collection.call.initial_history.period", 5 * AlarmManager.INTERVAL_DAY));
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
