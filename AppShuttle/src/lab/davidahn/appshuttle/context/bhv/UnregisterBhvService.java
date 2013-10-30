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

		//TODO pool에서 받아오는 걸로 바꾸기
		unregisterBhv(new BaseUserBhv(bhvType, bhvName));
	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	private void unregisterBhv(UserBhv uBhv) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		userBhvManager.unregisterBhv(uBhv);
	}
}
