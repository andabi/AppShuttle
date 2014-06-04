package lab.davidahn.appshuttle.report;

import android.content.Context;
import android.content.Intent;

public class ShareUtils {
	public static void shareTextPlain(Context cxt, String subject, String text){
		if(subject == null || text == null)
			throw new IllegalArgumentException("subject and text could not be null.");
		
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		cxt.startActivity(Intent.createChooser(sharingIntent, null));
//		, cxt.getResources().getString(R.string.share_using))
	}
}
