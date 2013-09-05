package lab.davidahn.appshuttle.report;

import static lab.davidahn.appshuttle.Settings.preferenceSettings;
import lab.davidahn.appshuttle.R;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

public class ReportingCxtService extends IntentService {
	private Handler handler;

	public ReportingCxtService(){
		this("ReportingCxtService");
	}
	public ReportingCxtService(String name){
		super(name);
	}
	
	public void onCreate(){
		super.onCreate();
		handler = new Handler();
	}
	@Override
	protected void onHandleIntent(Intent intent) {
//		Date eTime = new Date(System.currentTimeMillis());
//		Date sTime = new Date(eTime.getTime()-7*AlarmManager.INTERVAL_DAY);
		Reporter reporter = new Reporter(getApplicationContext(), handler);
//		reporter.addAttach(contextManager.loadCxtAsCsvFile(getApplicationContext(), "context", sTime, eTime));
//		reporter.addAttach(contextManager.loadRfdCxtAsCsvFile(getApplicationContext(), "refined_context", sTime, eTime));
		reporter.addAttach(getDatabasePath(preferenceSettings.getString("database.name", new StringBuilder(getResources().getString(R.string.app_name)).append(".db").toString())));
		reporter.report();
	}
}