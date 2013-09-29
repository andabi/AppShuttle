package lab.davidahn.appshuttle.collect;

import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
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
		SharedPreferences preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

		SnapshotUserCxt currUserCxt = ((AppShuttleApplication)getApplicationContext()).getCurrUserCxt();
		long expirationDuration = preferenceSettings.getLong("service.compaction.expiration", 15 * AlarmManager.INTERVAL_DAY);
		Date expirationBoundTimeDate = new Date(currUserCxt.getTimeDate().getTime() - expirationDuration);

		compactHistoryUserBhv(expirationBoundTimeDate);
		compactHistoryUserEnv(expirationBoundTimeDate);
		
		compactPredictedBhv(expirationBoundTimeDate);
		compactMatchedResult(expirationBoundTimeDate);

		compactUserBhv(expirationBoundTimeDate);
	}
	
	private void compactUserBhv(Date expirationBoundTimeDate) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance(getApplicationContext());
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		SnapshotUserCxt currUserCxt = ((AppShuttleApplication)getApplicationContext()).getCurrUserCxt();
		for(UserBhv uBhv : userBhvManager.getBhvList()){
			List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(expirationBoundTimeDate, currUserCxt.getTimeDate(), uBhv);
			if(durationUserBhvList.isEmpty())
				userBhvManager.unregisterBhv(uBhv);
		}

	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	private void compactHistoryUserBhv(Date expirationBoundTimeDate) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		durationUserBhvDao.deleteBefore(expirationBoundTimeDate);
	}

	private void compactHistoryUserEnv(Date expirationBoundTimeDate) {
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance();
		durationUserEnvDao.deleteBefore(expirationBoundTimeDate);
	}
	
	private void compactPredictedBhv(Date expirationBoundTimeDate) {
		PredictedBhvDao predictedBhvDao = PredictedBhvDao.getInstance();
		predictedBhvDao.deletePredictedBhv(expirationBoundTimeDate);
	}

	private void compactMatchedResult(Date expirationBoundTimeDate) {
		MatchedResultDao matchedResultDao = MatchedResultDao.getInstance();
		matchedResultDao.deleteMatchedResult(expirationBoundTimeDate);
	}
}
