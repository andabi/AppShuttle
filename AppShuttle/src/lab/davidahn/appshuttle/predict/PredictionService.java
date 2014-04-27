package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.ui.AppShuttleMainActivity;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
import android.app.IntentService;
import android.content.Intent;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

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
		
		long startTime = System.currentTimeMillis();
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		long endTime = System.currentTimeMillis();

		Tracker easyTracker = EasyTracker.getInstance(AppShuttleApplication.getContext());
		easyTracker.send(MapBuilder
				.createTiming("algorithm",
							endTime - startTime,
							"overall_prediction_cost",
							null)
				.build()
			);
		
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