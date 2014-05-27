package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.math3.util.Pair;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.graphics.drawable.Drawable;

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
		if(lastCallTimeDate == null) {
			lastCallTimeDate = currTimeDate;
			return Collections.emptyList();
		}
			
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

	/**
	 * Lookup Contact name in String
	 * returns 'null' if there is no contact name with the given number
	 *
	 * @param phoneNumber
	 * @return contactName (null if not exists)
	 */
	public static String getContactName(String phoneNumber) {
		ContentResolver cr = AppShuttleApplication.getContext().getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);

		if (cursor == null)
			return null;

		String contactName = null;

		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		if (!cursor.isClosed())
			cursor.close();

		return contactName;
	}
	
	/**
	 * @return List of contactName & icon
	 */
	public static List<Pair<String, Drawable>> getContactList() {
		 // 주소록 URI        
        Uri people = Contacts.CONTENT_URI;
        
        // 검색할 컬럼 정하기
        String[] projection = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER };
        
        // 쿼리 날려서 커서 얻기
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";    

        ContentResolver cr = AppShuttleApplication.getContext().getContentResolver();
        Cursor cursor = cr.query(people, projection, null, selectionArgs, sortOrder);
        // return managedQuery(people, projection, null, selectionArgs, sortOrder);
        
        // 전화번호부의 갯수 세기
        int end = cursor.getCount();                
        // 전화번호부의 (이름,사진)을 저장할 배열 선언
        List<Pair<String, Drawable>> res = new ArrayList<Pair<String, Drawable>>();
        // 전화번호부의 번호를 저장할 배열 선언
        //String [] phone = new String[end];    
        int count = 0;    

        if(cursor.moveToFirst()) 
        {
            // 컬럼명으로 컬럼 인덱스 찾기 
            int idIndex = cursor.getColumnIndex("_id");

            do 
            {
                // 요소값 얻기
//                int id = cursor.getInt(idIndex);        
//                String phoneChk = cursor.getString(2);
//                if (phoneChk.equals("1")) {
//                    Cursor phones = cr.query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                                    + " = " + id, null, null);
//
//                    while (phones.moveToNext()) {
//                        phone[count] = phones
//                                .getString(phones
//                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    }        
//                }
                String name = cursor.getString(1);
                // TODO: 연락처 마다의 사진 가져오기.
                // 임시로 전화기 아이콘 가져오도록 함.
                Drawable icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
                res.add(new Pair<String, Drawable>(name, icon));
                //name[count] = cursor.getString(1);

                //Log.i("ANDROES", "id=" + id +", name["+count+"]=" + names.get(count));
                count++;
                
            } while(cursor.moveToNext() || count > end);
        }
        return res;
	}
}
