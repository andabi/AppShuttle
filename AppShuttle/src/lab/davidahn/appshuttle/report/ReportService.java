package lab.davidahn.appshuttle.report;

import java.io.File;
import java.util.Date;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class ReportService extends IntentService {
	private SharedPreferences preferenceSettings;

	public ReportService(){
		this("ReportService");
	}
	public ReportService(String name){
		super(name);
	}
	
	public void onCreate(){
		super.onCreate();
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try{
			boolean isSuceess = reportUserData();
			if (isSuceess) {
				Log.i("mail", getResources().getString(R.string.report_success_msg));
				Toast.makeText(this, getResources().getString(R.string.report_success_msg), Toast.LENGTH_SHORT).show();
			} else {
				Log.d("mail", getResources().getString(R.string.report_failure_msg));
				Toast.makeText(this, getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {			
			Log.e("mail", "Could not send email", e);
			Toast.makeText(this, getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();
		}
	}
	
	public boolean reportUserData() throws Exception {

		String[] receivers = {preferenceSettings.getString("report.email.receiver_addr", "appshuttle2@gmail.com")};
		
		String subject = "[appshuttle user data] ";
		Account[] account = AccountManager.get(this).getAccounts();
		if(account.length <= 0) 
			subject += "unknown";
		else 
			subject += account[0].name + " (" + account[0].type + ")";
		
		Date currentTime = new Date(System.currentTimeMillis());
		String body = "From " + account[0].name + " (" + account[0].type + ")" + " at "+currentTime.toString();		

		File userData = getDatabasePath(preferenceSettings.getString("database.name", "AppShuttle.db"));

		Reporter reporter = new Reporter(receivers, subject, body);
		reporter.addAttach(userData);
		
		return reporter.report();
	}
}

//		Date eTime = new Date(System.currentTimeMillis());
//		Date sTime = new Date(eTime.getTime()-7*AlarmManager.INTERVAL_DAY);
//		reporter.addAttach(contextManager.loadCxtAsCsvFile(getApplicationContext(), "context", sTime, eTime));
//		reporter.addAttach(contextManager.loadRfdCxtAsCsvFile(getApplicationContext(), "refined_context", sTime, eTime));