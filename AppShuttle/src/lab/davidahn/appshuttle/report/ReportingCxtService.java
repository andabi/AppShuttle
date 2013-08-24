package lab.davidahn.appshuttle.report;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.UserCxtDao;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

public class ReportingCxtService extends IntentService {
	private UserCxtDao contextManager;
	private Handler handler;
	private SharedPreferences settings;

	public ReportingCxtService(){
		this("ReportingCxtService");
	}
	public ReportingCxtService(String name){
		super(name);
	}
	
	public void onCreate(){
		super.onCreate();
		contextManager = UserCxtDao.getInstance(getApplicationContext());
		handler = new Handler();
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
//		Date eTime = new Date(System.currentTimeMillis());
//		Date sTime = new Date(eTime.getTime()-7*AlarmManager.INTERVAL_DAY);
		Reporter reporter = new Reporter(getApplicationContext(), handler);
//		reporter.addAttach(contextManager.loadCxtAsCsvFile(getApplicationContext(), "context", sTime, eTime));
//		reporter.addAttach(contextManager.loadRfdCxtAsCsvFile(getApplicationContext(), "refined_context", sTime, eTime));
		reporter.addAttach(getDatabasePath(settings.getString("database.name", new StringBuilder(getResources().getString(R.string.app_name)).append(".db").toString())));
		reporter.report();
	}
}