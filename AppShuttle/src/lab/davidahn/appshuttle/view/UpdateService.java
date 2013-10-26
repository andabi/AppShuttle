package lab.davidahn.appshuttle.view;

import java.util.List;

import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
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
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		Predictor predictor = Predictor.getInstance();
		predictor.predict();
		
		Notifier notifier = new Notifier();
		List<PredictedBhvInfo> predictedBhvInfoList = predictor.getRecentPredictedBhvInfo(notifier.getNumElem());
		notifier.updateNotiView(BhvInfoForView.convert(predictedBhvInfoList));

		Intent refreshIntent = new Intent().setAction("lab.davidahn.appshuttle.REFRESH");
		sendBroadcast(refreshIntent);
		
		predictor.storeNewPredictedBhv(predictedBhvInfoList);
	}
}
