package lab.davidahn.appshuttle.context.bhv;

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
		BhvType bhvType = (BhvType) intent.getExtras().get("bhv_type");
		String bhvName = intent.getExtras().getString("bhv_name");
		UserBhv uBhv = new UserBhv(bhvType, bhvName);
		
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		userBhvManager.unregisterBhv(uBhv);
	}

	public void onDestroy() {
		super.onDestroy();
	}
}
