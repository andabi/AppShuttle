package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleMainService;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.R.id;
import lab.davidahn.appshuttle.R.layout;
import lab.davidahn.appshuttle.R.string;
import lab.davidahn.appshuttle.report.ReportingCxtService;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class InfoFragment extends Fragment implements OnClickListener{
	private Button btnStart;
	private Button btnStop;
	private Button btnReport;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.info, container, false);
		
		btnStart = (Button) v.findViewById(R.id.startBtn);
		btnStop = (Button) v.findViewById(R.id.stopBtn);
		btnReport = (Button) v.findViewById(R.id.reportBtn);
		
		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnReport.setOnClickListener(this);

		if (isAppShuttleServiceRunning()) {
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			btnReport.setEnabled(true);
		} else {
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			btnReport.setEnabled(false);
		}

		return v;
	}

	public void onStartClick(View v) {
		if (getActivity().startService(new Intent(getActivity(), AppShuttleMainService.class)) != null) {
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			btnReport.setEnabled(true);
			Toast.makeText(getActivity(), R.string.start, Toast.LENGTH_SHORT).show();
		}
	}

	public void onStopClick(View v) {
		if (getActivity().stopService(new Intent(getActivity(), AppShuttleMainService.class))) {
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
			btnReport.setEnabled(false);
			Toast.makeText(getActivity(), R.string.stop, Toast.LENGTH_SHORT).show();
		}
	}

	public void onReportClick(View v) {
		getActivity().startService(new Intent(getActivity(), ReportingCxtService.class));
	}

	private boolean isAppShuttleServiceRunning() {
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (AppShuttleMainService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.startBtn:
			onStartClick(v);
			break;
		case R.id.stopBtn:
			onStopClick(v);
			break;
		case R.id.reportBtn:
			onReportClick(v);
		}
	}
}