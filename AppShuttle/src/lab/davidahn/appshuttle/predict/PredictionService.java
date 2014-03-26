package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.ui.AppShuttleMainActivity;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
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
		/* TODO:
		 * 왜 Predictor에 명령하고 PredictedPresentBhv에서 꺼내오나?
		 */
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		PredictedPresentBhv.extractPredictedPresentBhvList();
		sendBroadcast(new Intent().setAction(AppShuttleMainActivity.UPDATE_ACTIVITY));
		NotiBarNotifier.getInstance().updateNotification();
		sendBroadcast(new Intent().setAction(AppShuttleMainActivity.PROGRESS_VISIBILITY).putExtra("isOn", false));
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
}