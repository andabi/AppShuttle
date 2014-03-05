package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.IntentService;
import android.content.Intent;

public class PredictionService extends IntentService {
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
		sendBroadcast(new Intent().setAction(AppShuttleApplication.PROGRESS_VISIBILITY).putExtra("isOn", true));
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		sendBroadcast(new Intent().setAction(AppShuttleApplication.UPDATE_VIEW));
		sendBroadcast(new Intent().setAction(AppShuttleApplication.PROGRESS_VISIBILITY).putExtra("isOn", false));
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}