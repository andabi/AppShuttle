package lab.davidahn.appshuttle.collect;

import static lab.davidahn.appshuttle.context.bhv.UserBhv.create;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.CallUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;

public class CallBhvCollector extends BaseBhvCollector {
	private ContentResolver _contentResolver;

	private Date _lastCallTimeDate;

	private static CallBhvCollector callBhvCollector = new CallBhvCollector();

	private CallBhvCollector(){
		super();
		_contentResolver = _appShuttleContext.getContentResolver();
	}
	
	public static CallBhvCollector getInstance(){
		return callBhvCollector;
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		long historyAcceptance = _preferenceSettings.getLong("collection.call.initial_history.period", 5 * AlarmManager.INTERVAL_DAY);
		_lastCallTimeDate = new Date(currTimeDate.getTime() - historyAcceptance);
		List<DurationUserBhv> res = Collections.emptyList();
		res = extractCallBhvDuring(_lastCallTimeDate, currTimeDate);
		
		_lastCallTimeDate = currTimeDate;
		
		return res;
	}
	
	@Override
	public List<DurationUserBhv> extractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone, List<UserBhv> userBhvList) {
		if(_lastCallTimeDate == null)
			return Collections.emptyList();
		
		List<DurationUserBhv> res = extractCallBhvDuring(_lastCallTimeDate, currTimeDate);
		
		_lastCallTimeDate = currTimeDate;

		return res;
	}
	
	private List<DurationUserBhv> extractCallBhvDuring(Date beginTime, Date endTime){
		Cursor cursor = _contentResolver.query(
				CallLog.Calls.CONTENT_URI, 
				null, 
				CallLog.Calls.DATE + " >= " + beginTime.getTime() + " AND " + CallLog.Calls.DATE + " < " + endTime.getTime(),
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
			
			if(duration <= 0)
				continue;
			if(type != CallLog.Calls.INCOMING_TYPE && type != CallLog.Calls.OUTGOING_TYPE)
				continue;
			
			CallUserBhv callUserBhv = (CallUserBhv) create(BhvType.CALL, number);
			callUserBhv.setMeta("cachedName", name);
//			callUserBhv.setCachedName(name);
			
			DurationUserBhv durationUserBhv =  new DurationUserBhv.Builder()
			.setTime(date)
			.setEndTime(new Date(date.getTime() + duration))
			.setTimeZone(timeZone)
			.setBhv(callUserBhv)
			.build();
			res.add(durationUserBhv);
		}
		cursor.close();
		return res;
	}
}
