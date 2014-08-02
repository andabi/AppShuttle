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
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public class CallBhvCollector extends BaseBhvCollector {
	private ContentResolver contentResolver;

	private long lastCallTime;

	private static CallBhvCollector callBhvCollector = new CallBhvCollector();
	private CallBhvCollector(){
		super();
		contentResolver = cxt.getContentResolver();
	}
	public static CallBhvCollector getInstance(){
		return callBhvCollector;
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(long currTime, TimeZone currTimeZone) {
		long historyAcceptance = preferenceSettings.getLong("collection.bhv.call.pre.period", 6 * AlarmManager.INTERVAL_DAY);
		lastCallTime = currTime - historyAcceptance;
		List<DurationUserBhv> extractedCallBhvList = new ArrayList<DurationUserBhv>();
		extractedCallBhvList = extractCallBhvDuring(lastCallTime, currTime);
		
		if(!extractedCallBhvList.isEmpty()){
			lastCallTime = extractedCallBhvList.get(extractedCallBhvList.size()-1).getTime();
		}
		
		return extractedCallBhvList;
	}
	
	@Override
	public List<DurationUserBhv> extractDurationUserBhv(long currTime, TimeZone currTimeZone, List<UserBhv> userBhvList) {
		if(lastCallTime == 0) {
			lastCallTime = currTime;
			return Collections.emptyList();
		}
			
		List<DurationUserBhv> extractedCallBhvList = extractCallBhvDuring(lastCallTime, currTime);
		
		if(!extractedCallBhvList.isEmpty()){
			lastCallTime = extractedCallBhvList.get(extractedCallBhvList.size()-1).getTime();
		}

		return extractedCallBhvList;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(long currTime, TimeZone currTimeZone) {
		return Collections.emptyList();
	}
	
	private List<DurationUserBhv> extractCallBhvDuring(long beginTime, long endTime){
		Cursor cursor = contentResolver.query(
				CallLog.Calls.CONTENT_URI, 
				null, 
				CallLog.Calls.DATE + " > " + beginTime + " AND " + CallLog.Calls.DATE + " <= " + endTime,
				null, 
				CallLog.Calls.DATE + " ASC"
				);
		
		if(cursor == null) return null;

		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();
		while(cursor.moveToNext()) {
			int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
			int timeIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
			int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
			int durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);
			int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);

			String number = cursor.getString(numIdx);
			String name = cursor.getString(nameIdx);
			if(name == null)
				name = number;
			
			long time = cursor.getLong(timeIdx);
			Calendar calender = Calendar.getInstance();
			calender.setTime(new Date(time));
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
			.setTime(time)
			.setEndTime(time + duration)
			.setTimeZone(timeZone)
			.setBhv(callUserBhv)
			.build();
			res.add(durationUserBhv);
		}
		cursor.close();
		return res;
	}

	/**
	 * Lookup Contact name in String
	 * returns 'null' if there is no contact name with the given number
	 *
	 * @param phoneNumber
	 * @return contactName (null if not exists)
	 */
	public String getContactName(String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = contentResolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);

		if(cursor == null) return null;
		
		String contactName = null;

		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		cursor.close();

		return contactName;
	}
	
	public List<String> getContactList() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED};
        String selection = ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED + "> 0";
        String[] selectionArgs = null;
//        String sortOrder = ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED + " DESC";
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

        if(cursor == null) return null;

        List<String> res = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String number = cursor.getString(0);
			res.add(number);
		}
		
		cursor.close();

        return res;
	}
	
	public long getLastCallTime(String phoneNumber){
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = contentResolver.query(uri, new String[]{PhoneLookup.LAST_TIME_CONTACTED}, null, null, null);

		if(cursor == null) return 0;

		long lastTimeContacted = 0;
		if (cursor.moveToFirst()) {
			lastTimeContacted = cursor.getLong(cursor.getColumnIndex(PhoneLookup.LAST_TIME_CONTACTED));
		}

		cursor.close();

		return lastTimeContacted;
	}
}
