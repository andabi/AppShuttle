package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.OrdinaryUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
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
		
		OrdinaryUserBhv.extractViewListSorted();

		NotiBarNotifier notifier = new NotiBarNotifier();
		
		List<ViewableUserBhv> viewableUserBhvList = new ArrayList<ViewableUserBhv>();
		viewableUserBhvList.addAll(UserBhvManager.getInstance().getFavoratesBhvSetSorted());
		viewableUserBhvList.addAll(OrdinaryUserBhv.getExtractedViewListSorted(notifier.getNumElem()));
		
		notifier.updateNotiView(viewableUserBhvList);

		Intent refreshIntent = new Intent().setAction("lab.davidahn.appshuttle.REFRESH");
		sendBroadcast(refreshIntent);
	}
}
