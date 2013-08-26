package lab.davidahn.appshuttle.context;

import lab.davidahn.appshuttle.context.env.ChangeUserEnvDao;
import lab.davidahn.appshuttle.mine.matcher.MatchedResultDao;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvDao;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactingCxtService extends IntentService {
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
	}
	
	public void onHandleIntent(Intent intent){
//		UserCxtDao userCxtDao = UserCxtDao.getInstance(getApplicationContext());
		ChangeUserEnvDao changedUserEnvDao = ChangeUserEnvDao.getInstance(getApplicationContext());
		RfdUserCxtDao rfdUserCxtDao = RfdUserCxtDao.getInstance(getApplicationContext());
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance(getApplicationContext());
		MatchedResultDao matchedResultDao = MatchedResultDao.getInstance(getApplicationContext());

		long time = System.currentTimeMillis() - settings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		
		rfdUserCxtDao.deleteRfdCxtBefore(time);
		changedUserEnvDao.deleteChangedUserEnv(time);
		matchedResultDao.deleteMatchedResult(time);
		predictedBhvDao.deletePredictedBhv(time);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
