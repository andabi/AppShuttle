package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.predict.Predictor;
import android.app.IntentService;
import android.content.Intent;

public class UpdateService extends IntentService {
	public UpdateService() {
		super("UpdateService");
	}
	public UpdateService(String name) {
		super(name);
	}

	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_VISIBLE"));
		Predictor.getInstance().predict();
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_INVISIBLE"));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.REFRESH"));
	}
}