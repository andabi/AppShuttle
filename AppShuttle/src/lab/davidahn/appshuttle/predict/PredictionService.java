package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.view.ViewService;
import lab.davidahn.appshuttle.view.ui.AppShuttleMainActivity;
import android.app.IntentService;
import android.content.Intent;

public class PredictionService extends IntentService {
	public static final String PREDICT = "lab.davidahn.appshuttle.PREDICT";

	public PredictionService() {
		super("PredictionService");
	}
	public PredictionService(String name) {
		super(name);
	}

	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
//		Log.d("prediction", "started");
		sendBroadcast(new Intent().setAction(AppShuttleMainActivity.PROGRESS_VISIBILITY).putExtra("isOn", true));
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		startService(new Intent(this, ViewService.class));
		sendBroadcast(new Intent().setAction(AppShuttleMainActivity.PROGRESS_VISIBILITY).putExtra("isOn", false));
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}