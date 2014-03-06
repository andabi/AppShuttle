package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
		Log.d("prediction", "started");
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_VISIBILITY").putExtra("isOn", true));
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW"));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_VISIBILITY").putExtra("isOn", false));
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}