package lab.davidahn.appshuttle.compact;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.context.ContextManager;
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
		long time = GlobalState.currentUCxt.getTime().getTime() - settings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		contextManager.removeRfdCxtBefore(time);
		
		//TODO remove matched cxt, predictedBhv ...
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
