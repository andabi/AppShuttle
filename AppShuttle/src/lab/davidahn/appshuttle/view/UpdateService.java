package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.OrdinaryUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.mine.matcher.PredictionInfo;
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
		
		NotiBarNotifier notifier = new NotiBarNotifier();
//		List<PredictionInfo> predictedBhvList = predictor.getRecentPredictedBhv(notifier.getNumElem());
		
//		List viewableUserBhv = new ArrayList<UserBhv>();
//		
//		viewableUserBhv.addAll(UserBhvManager.getInstance().getFavoratesBhvSet());
//		viewableUserBhv.addAll(OrdinaryUserBhv.extractViewList(predictedBhvList));
//		
//		notifier.updateNotiView(viewableUserBhv);

		List<PredictionInfo> predictedBhvList = predictor.getRecentPredictedBhv(Integer.MAX_VALUE/*notifier.getNumElem()*/);
		notifier.updateNotiView(OrdinaryUserBhv.extractViewList(predictedBhvList));
		
		Intent refreshIntent = new Intent().setAction("lab.davidahn.appshuttle.REFRESH");
		sendBroadcast(refreshIntent);
		
		predictor.storeNewPredictedBhv(predictedBhvList);
	}
}
