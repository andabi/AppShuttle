package lab.davidahn.appshuttle.context.bhv;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class UpdateBhvService extends IntentService {
	SharedPreferences settings;

	public UpdateBhvService() {
		this("UpdateBhvService");
	}

	public UpdateBhvService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
	}

	public void onHandleIntent(Intent intent) {
		UserBhvDao userBhvDao = UserBhvDao.getInstance(getApplicationContext());
		BhvType bhvType = (BhvType) intent.getExtras().get("bhv_type");
		String bhvName = intent.getExtras().getString("bhv_name");
		UserBhv uBhv = new UserBhv(bhvType, bhvName);
		userBhvDao.deleteUserBhv(uBhv);
	}

	public void onDestroy() {
		super.onDestroy();
	}
}
