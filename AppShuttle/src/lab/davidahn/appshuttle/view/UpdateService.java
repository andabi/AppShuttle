package lab.davidahn.appshuttle.view;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class UpdateService extends IntentService {
	public UpdateService() {
		super("NotiViewService");
	}
	public UpdateService(String name) {
		super(name);
	}

	private static final int UPDATE_NOTI_VIEW = 1;
	
	private NotificationManager _notificationManager;
//	private LayoutInflater layoutInflater;

	public void onCreate(){
		super.onCreate();
		_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		Predictor predictor = Predictor.getInstance();

		predictor.predict();
		
		List<PredictedBhvInfo> predictedBhvInfoList = predictor.getRecentPredictedBhvInfo(getNumElem());
		List<PredictedBhvInfoForView> predictedBhvInfoListForView = PredictedBhvInfoForView.getPredictedBhvInfoListForView(predictedBhvInfoList);
		
		updateNotiView(predictedBhvInfoListForView);

		//TODO
		//PredictedFragment update();
		
		predictor.storeNewPredictedBhv(predictedBhvInfoList);
	}

	@SuppressLint("NewApi")
	private void updateNotiView(List<PredictedBhvInfoForView> predictedBhvInfoForViewList) {
		RemoteViews notiView = createNotiRemoteViews(predictedBhvInfoForViewList);

		Notification notiUpdate = new Notification.Builder(UpdateService.this)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiView)
			.setOngoing(true)
			.setWhen(AppShuttleApplication.launchTime)
			.setPriority(Notification.PRIORITY_MAX)
			.build();
		_notificationManager.notify(UPDATE_NOTI_VIEW, notiUpdate);
	}

	private RemoteViews createNotiRemoteViews(List<PredictedBhvInfoForView> predictedBhvInfoForViewList) {
		RemoteViews notiRemoteView = new RemoteViews(getPackageName(), R.layout.noti);

		//clean
		notiRemoteView.removeAllViews(R.id.noti_elem_container);
		
		notiRemoteView.setOnClickPendingIntent(R.id.noti_icon, PendingIntent.getActivity(this, 0, new Intent(this, AppShuttleMainActivity.class), 0));

		for(PredictedBhvInfoForView predictedBhvInfoForView : predictedBhvInfoForViewList) {

			BhvType bhvType = predictedBhvInfoForView.getPredictedBhvInfo().getUserBhv().getBhvType();
			RemoteViews notiElemRemoteView = new RemoteViews(getPackageName(), R.layout.noti_element);
			
			notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, PendingIntent.getActivity(this, 0, predictedBhvInfoForView.getLaunchIntent(), 0));

			BitmapDrawable iconDrawable = (BitmapDrawable)predictedBhvInfoForView.getIcon();
			notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_icon, iconDrawable.getBitmap());
			
			if (bhvType == BhvType.CALL){
//				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemRemoteView.setTextViewText(R.id.noti_elem_text, predictedBhvInfoForView.getBhvNameText());
				notiElemRemoteView.setTextViewTextSize(R.id.noti_elem_text, 
						TypedValue.COMPLEX_UNIT_PX, 
						getResources().getDimension(R.dimen.notibar_text_size));
			}
			
			notiRemoteView.addView(R.id.noti_elem_container, notiElemRemoteView);
		}

		return notiRemoteView;
	}
	
	private int getNumElem() {
		int maxNumElem = AppShuttleApplication.getContext().getPreferenceSettings().getInt("viewer.noti.max_num_elem", Integer.MAX_VALUE);
		int NotibarIconAreaWidth = (int) ((getResources().getDimension(R.dimen.notibar_icon_area_width) / getResources().getDisplayMetrics().density));
		int NotibarPredictedBhvAreaWidth = (int) ((getResources().getDimension(R.dimen.notibar_predicted_bhv_area_width) / getResources().getDisplayMetrics().density));
		return Math.min(maxNumElem, (getNotibarWidth() - NotibarIconAreaWidth) / NotibarPredictedBhvAreaWidth);
	}
	
	private int getNotibarWidth(){
		WindowManager wm = (WindowManager) AppShuttleApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;
	    return (int)(outMetrics.widthPixels / density);
	}
}

//View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));
//LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

//private int getNotiViewWidth() {
//WindowManager wm = (WindowManager) AppShuttleApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
//Display display = wm.getDefaultDisplay();
//Point size = new Point();
//display.getSize(size);
//return size.x;
//}

