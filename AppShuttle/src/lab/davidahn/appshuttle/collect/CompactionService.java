package lab.davidahn.appshuttle.collect;

import lab.davidahn.appshuttle.context.DuratinoUserBhvDao;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.mine.matcher.MatchedResultDao;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvDao;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactionService extends IntentService {
    SharedPreferences settings;
	
	public CompactionService() {
		this("CompactionService");
	}
	
	public CompactionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
	}
	
	public void onHandleIntent(Intent intent){
		DuratinoUserBhvDao durationUserBhvDao = DuratinoUserBhvDao.getInstance(getApplicationContext());
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance(getApplicationContext());
		MatchedResultDao matchedResultDao = MatchedResultDao.getInstance(getApplicationContext());

		long time = System.currentTimeMillis() - settings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		durationUserBhvDao.deleteRfdCxtBefore(time);
		durationUserEnvDao.deleteDurationUserEnv(time);
		matchedResultDao.deleteMatchedResult(time);
		predictedBhvDao.deletePredictedBhv(time);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}