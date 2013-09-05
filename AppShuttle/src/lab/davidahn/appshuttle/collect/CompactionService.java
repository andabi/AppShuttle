package lab.davidahn.appshuttle.collect;

import lab.davidahn.appshuttle.context.DuratinoUserBhvDao;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.mine.matcher.MatchedResultDao;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvDao;
import static lab.davidahn.appshuttle.Settings.*;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;

public class CompactionService extends IntentService {
	public CompactionService() {
		this("CompactionService");
	}
	
	public CompactionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
	}
	
	public void onHandleIntent(Intent intent){
		DuratinoUserBhvDao durationUserBhvDao = DuratinoUserBhvDao.getInstance(getApplicationContext());
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance(getApplicationContext());
		MatchedResultDao matchedResultDao = MatchedResultDao.getInstance(getApplicationContext());

		long time = System.currentTimeMillis() - preferenceSettings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		durationUserBhvDao.deleteRfdCxtBefore(time);
		durationUserEnvDao.deleteDurationUserEnv(time);
		matchedResultDao.deleteMatchedResult(time);
		predictedBhvDao.deletePredictedBhv(time);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
