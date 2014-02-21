package lab.davidahn.appshuttle.collect.bhv;

import android.app.IntentService;
import android.content.Intent;

public class UnregisterBhvService extends IntentService {
	public UnregisterBhvService() {
		this("UnregisterBhvService");
	}

	public UnregisterBhvService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
	}

	public void onHandleIntent(Intent intent) {
		UserBhvType bhvType = (UserBhvType) intent.getExtras().get("bhv_type");
		String bhvName = intent.getExtras().getString("bhv_name");
		UserBhvManager.getInstance().unregisterBhv(new BaseUserBhv(bhvType, bhvName));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PREDICT"));
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
