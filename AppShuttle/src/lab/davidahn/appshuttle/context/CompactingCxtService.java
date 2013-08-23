package lab.davidahn.appshuttle.context;

import java.util.Date;

import lab.davidahn.appshuttle.GlobalState;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactingCxtService extends IntentService {
//	private Calendar calendar;
	private ContextManager contextManager;
//	private UserBhvManager userBhvManager;
    SharedPreferences settings;
	
	public CompactingCxtService() {
		this("CompactingCxtService");
	}
	
	public CompactingCxtService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
		contextManager = ContextManager.getInstance(getApplicationContext());
//		userBhvManager = UserBhvManager.getInstance(getApplicationContext());
	}
	
	public void onHandleIntent(Intent intent){
		long time = System.currentTimeMillis() - settings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		contextManager.removeRfdCxtBefore(time);
		contextManager.removeChangedUserEnv(time);
		contextManager.removeMatchedCxt(time);
		contextManager.removePredictedBhv(time);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
