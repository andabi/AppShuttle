package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class UpdateService extends IntentService {
	public UpdateService() {
		super("UpdateService");
	}
	public UpdateService(String name) {
		super(name);
	}

	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {

		Predictor predictor = Predictor.getInstance();
		predictor.predict();
		
		OrdinaryUserBhv.extractViewListSorted();

		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		NotiBarNotifier notifier = new NotiBarNotifier();
		if(pref.getBoolean("noti.view.enabled", true))
			notifier.updateNotibar();
		else
			notifier.hideNotibar();
		
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.REFRESH"));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_INVISIBLE"));
	}
}
