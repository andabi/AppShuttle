package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.view.ui.AppShuttleMainActivity;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ViewService extends IntentService {
	public ViewService() {
		super("ViewService");
	}
	public ViewService(String name) {
		super(name);
	}

	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		Log.d("viewer","update view");
		if(!intent.getBooleanExtra("isOnlyNotibar", false))
			sendBroadcast(new Intent().setAction(AppShuttleMainActivity.UPDATE_ACTIVITY));
		NotiBarNotifier.getInstance().updateNotification();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}