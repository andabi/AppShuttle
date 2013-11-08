package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

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
		
		int numElem = notifier.getNumElem();
		List<FavoratesUserBhv> notifiableFavoratesBhvList = FavoratesUserBhv.getNotifiableFavoratesBhvList();
		int numFavoratesElem = Math.min(notifiableFavoratesBhvList.size(), numElem);
		int numOrdinaryElem = numElem - numFavoratesElem;
		
		viewableUserBhvList.addAll(notifiableFavoratesBhvList.subList(0, numFavoratesElem));
		viewableUserBhvList.addAll(OrdinaryUserBhv.getExtractedViewListSorted(numOrdinaryElem));
		
		notifier.updateNotiView(viewableUserBhvList);

		Intent refreshIntent = new Intent().setAction("lab.davidahn.appshuttle.REFRESH");
		sendBroadcast(refreshIntent);
	}
}
