package lab.davidahn.appshuttle.collect;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactionService extends IntentService {
	private SnapshotUserCxt currUserCxt;
	
	public CompactionService() {
		this("CompactionService");
	}
	
	public CompactionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		currUserCxt = AppShuttleApplication.currUserCxt;
	}
	
	public void onHandleIntent(Intent intent){
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		long expirationDuration = preferenceSettings.getLong("compaction.expiration", 15 * AlarmManager.INTERVAL_DAY);
		long expirationBoundTime = currUserCxt.getTime() - expirationDuration;
		
		compactHistoryUserBhv(expirationBoundTime);
		compactHistoryUserEnv(expirationBoundTime);
		compactUserBhvList(expirationBoundTime);
		compactStatEntires(expirationBoundTime);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	private void compactHistoryUserBhv(long expirationBoundTime) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		durationUserBhvDao.deleteBefore(expirationBoundTime);
	}

	private void compactHistoryUserEnv(long expirationBoundTime) {
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();
		durationUserEnvManager.deleteAllBefore(expirationBoundTime);
	}
	
	private void compactUserBhvList(long expirationBoundTime) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		
		for(BaseUserBhv uBhv : ViewableUserBhv.getNormalBhvSet()){
			List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(expirationBoundTime, currUserCxt.getTime(), uBhv);
			if(!durationUserBhvList.isEmpty())
				continue;
			userBhvManager.unregister(uBhv);
		}
	}
	
	private void compactStatEntires(long expirationBoundTime) {
		StatCollector statCollector = StatCollector.getInstance();
		statCollector.deleteAllBefore(expirationBoundTime);
	}
}
