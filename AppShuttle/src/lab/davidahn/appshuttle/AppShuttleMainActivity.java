package lab.davidahn.appshuttle;

import com.bugsense.trace.BugSenseHandler;

import lab.davidahn.appshuttle.report.ReportingCxtService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AppShuttleMainActivity extends Activity {
	private Button btnStart;
	private Button btnStop;
	private Button btnReport;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		BugSenseHandler.initAndStartSession(this, "a3573081");
		
		setContentView(R.layout.main);		
		btnStart = (Button)findViewById(R.id.startBtn);
		btnStop = (Button)findViewById(R.id.stopBtn);
		btnReport = (Button)findViewById(R.id.reportBtn);

		if(isAppShuttleServiceRunning()){
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			btnReport.setEnabled(true);
		} else {
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			btnReport.setEnabled(false);
		}
	}

	public void onStartClick(View v) {
		if(startService(new Intent(this, AppShuttleService.class)) != null){
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			btnReport.setEnabled(true);
			Toast.makeText(this, R.string.start, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onStopClick(View v) {
		if(stopService(new Intent(this, AppShuttleService.class))){			
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			btnReport.setEnabled(false);
			Toast.makeText(this, R.string.stop, Toast.LENGTH_SHORT).show();
		}
	}

	public void onReportClick(View v) {
		startService(new Intent(this, ReportingCxtService.class));
	}
	
	private boolean isAppShuttleServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (AppShuttleService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}