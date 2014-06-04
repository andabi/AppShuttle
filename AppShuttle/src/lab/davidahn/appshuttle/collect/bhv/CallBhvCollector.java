package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
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
	//TODO fix
	public static List<UserBhv> getContactList() {
		 // �ּҷ� URI        
        Uri people = Contacts.CONTENT_URI;
        
        // �˻��� �÷� ���ϱ�
        String[] projection = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER };
        
        // ���� ������ Ŀ�� ���
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";    

        ContentResolver cr = AppShuttleApplication.getContext().getContentResolver();
        Cursor cursor = cr.query(people, projection, null, selectionArgs, sortOrder);
        // return managedQuery(people, projection, null, selectionArgs, sortOrder);
        
        // ��ȭ��ȣ���� ���� ����
        int end = cursor.getCount();                
        // ��ȭ��ȣ���� (�̸�,����)�� ������ �迭 ����
        List<UserBhv> res = new ArrayList<UserBhv>();
        // ��ȭ��ȣ���� ��ȣ�� ������ �迭 ����
        //String [] phone = new String[end];    
        int count = 0;    

        if(cursor.moveToFirst()) 
        {
            // �÷������� �÷� �ε��� ã�� 
            int idIndex = cursor.getColumnIndex("_id");

            do 
            {
                // ��Ұ� ���
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
                // TODO: ����ó ������ ���� ��������.
                // �ӽ÷� ��ȭ�� ������ ���������� ��.
//                Drawable icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
                res.add(BaseUserBhv.create(UserBhvType.CALL, name));
                //name[count] = cursor.getString(1);

                //Log.i("ANDROES", "id=" + id +", name["+count+"]=" + names.get(count));
                count++;
                
            } while(cursor.moveToNext() || count > end);
        }
        return res;
	}
}
