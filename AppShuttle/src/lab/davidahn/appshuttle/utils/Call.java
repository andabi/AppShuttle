package lab.davidahn.appshuttle.utils;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class Call {
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
}
