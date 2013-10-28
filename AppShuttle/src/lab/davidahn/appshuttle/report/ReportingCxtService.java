package lab.davidahn.appshuttle.report;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

public class ReportingCxtService extends IntentService {
	private SharedPreferences preferenceSettings;
	private Handler handler;

	public ReportingCxtService(){
		this("ReportingCxtService");
	}
	public ReportingCxtService(String name){
		super(name);
	}
	
	public void onCreate(){
		super.onCreate();
		preferenceSettings = AppShuttleApplication.getContext().getPreferenceSettings();
		handler = new Handler();
//		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
//		SharedPreferences preferenceSettings = ((AppShuttleApplication)getApplicationContext()).getPreferenceSettings();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
//		Date eTime = new Date(System.currentTimeMillis());
//		Date sTime = new Date(eTime.getTime()-7*AlarmManager.INTERVAL_DAY);
		Reporter reporter = new Reporter(handler);
//		reporter.addAttach(contextManager.loadCxtAsCsvFile(getApplicationContext(), "context", sTime, eTime));
//		reporter.addAttach(contextManager.loadRfdCxtAsCsvFile(getApplicationContext(), "refined_context", sTime, eTime));
		reporter.addAttach(getDatabasePath(preferenceSettings.getString("database.name", "AppShuttle.db")));
		reporter.report();
	}
}