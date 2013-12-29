package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;

public class CallBhvCollector extends BaseBhvCollector {
	private ContentResolver contentResolver;

	private Date lastCallTimeDate;

	private static CallBhvCollector callBhvCollector = new CallBhvCollector();
	private CallBhvCollector(){
		super();
		contentResolver = cxt.getContentResolver();
	}
	public static CallBhvCollector getInstance(){
		return callBhvCollector;
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		long historyAcceptance = preferenceSettings.getLong("collection.call.pre.period", 6 * AlarmManager.INTERVAL_DAY);
		lastCallTimeDate = new Date(currTimeDate.getTime() - historyAcceptance);
		List<DurationUserBhv> extractedCallBhvList = new ArrayList<DurationUserBhv>();
		extractedCallBhvList = extractCallBhvDuring(lastCallTimeDate, currTimeDate);
		
		if(!extractedCallBhvList.isEmpty()){
			lastCallTimeDate = extractedCallBhvList.get(extractedCallBhvList.size()-1).getTimeDate();
		}
		
		return extractedCallBhvList;
	}
	
	@Override
	public List<DurationUserBhv> extractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone, List<BaseUserBhv> userBhvList) {
		if(lastCallTimeDate == null)
			return Collections.emptyList();
		
		List<DurationUserBhv> extractedCallBhvList = extractCallBhvDuring(lastCallTimeDate, currTimeDate);
		
		if(!extractedCallBhvList.isEmpty()){
			lastCallTimeDate = extractedCallBhvList.get(extractedCallBhvList.size()-1).getTimeDate();
		}

		return extractedCallBhvList;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}
	
	private List<DurationUserBhv> extractCallBhvDuring(Date beginTime, Date endTime){
		Cursor cursor = contentResolver.query(
				CallLog.Calls.CONTENT_URI, 
				null, 
				CallLog.Calls.DATE + " > " + beginTime.getTime() + " AND " + CallLog.Calls.DATE + " <= " + endTime.getTime(),
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
			
			CallUserBhv callUserBhv = (CallUserBhv) create(UserBhvType.CALL, number);
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
