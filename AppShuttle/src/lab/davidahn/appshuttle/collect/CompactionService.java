package lab.davidahn.appshuttle.collect;

import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DuratinoUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.mine.matcher.MatchedResultDao;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvDao;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactionService extends IntentService {
	private SharedPreferences preferenceSettings;
	
	public CompactionService() {
		this("CompactionService");
	}
	
	public CompactionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

	}
	
	public void onHandleIntent(Intent intent){
		DuratinoUserBhvDao durationUserBhvDao = DuratinoUserBhvDao.getInstance(getApplicationContext());
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance(getApplicationContext());
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance(getApplicationContext());
		MatchedResultDao matchedResultDao = MatchedResultDao.getInstance(getApplicationContext());
		UserBhvManager userBhvManager = UserBhvManager.getInstance(getApplicationContext());

		SnapshotUserCxt currUserCxt = ((AppShuttleApplication)getApplicationContext()).getCurrUserCxt();
		long expirationDuration = preferenceSettings.getLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		Date expirationBoundTimeDate = new Date(currUserCxt.getTimeDate().getTime() - expirationDuration);

		durationUserBhvDao.deleteRfdCxtBefore(expirationBoundTimeDate);
		durationUserEnvDao.deleteDurationUserEnv(expirationBoundTimeDate);
		matchedResultDao.deleteMatchedResult(expirationBoundTimeDate);
		predictedBhvDao.deletePredictedBhv(expirationBoundTimeDate);
		
		for(UserBhv uBhv : userBhvManager.getBhvList()){
			List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveRfdCxtByBhv(expirationBoundTimeDate, currUserCxt.getTimeDate(), uBhv);
			if(durationUserBhvList.isEmpty())
				userBhvManager.unregisterBhv(uBhv);
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
