package lab.davidahn.appshuttle.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainActivity;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfoDao;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.widget.RemoteViews;

public class NotiViewService extends Service {
	private static final int NOTI_UPDATE = 1;
	
	private NotificationManager _notificationManager;
	private PackageManager _packageManager;
//	private LayoutInflater layoutInflater;

	public void onCreate(){
		super.onCreate();
		_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		_packageManager = getPackageManager();
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		updateNotiView(predictBhv());
		
//		View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//		ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//		ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onDestroy() {
		super.onDestroy();
		_notificationManager.cancelAll();
	}

	private List<PredictedBhvInfo> predictBhv() {
		Predictor predictor = Predictor.getInstance();
		List<PredictedBhvInfo> predictedBhvInfoList = predictor.predict(Integer.MAX_VALUE);

		storeNewPredictedBhv(predictedBhvInfoList);

		return predictedBhvInfoList;
	}

	@SuppressLint("NewApi")
	private void updateNotiView(List<PredictedBhvInfo> predictedBhvInfoList) {
		RemoteViews notiRemoteViews = createNotiRemoteViews(predictedBhvInfoList);

		Notification notiUpdate = new Notification.Builder(NotiViewService.this)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiRemoteViews)
			.setPriority(Notification.PRIORITY_MAX)
			.setOngoing(true)
			.build();
		_notificationManager.notify(NOTI_UPDATE, notiUpdate);
	}

	private RemoteViews createNotiRemoteViews(List<PredictedBhvInfo> predictedBhvInfoList) {
		RemoteViews notiView = new RemoteViews(getPackageName(), R.layout.noti);
		notiView.setOnClickPendingIntent(R.id.self_icon, PendingIntent.getActivity(this, 0, new Intent(this, AppShuttleMainActivity.class), 0));
//		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//		LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			BhvType bhvType = predictedBhv.getBhvType();
			String bhvName = predictedBhv.getBhvName();
			
			RemoteViews notiElemView = new RemoteViews(getPackageName(), R.layout.noti_element);
			if(bhvType == BhvType.APP){
				Intent launchIntent = _packageManager.getLaunchIntentForPackage(bhvName);
				if(launchIntent == null){
					continue;
				} else {
					try {
						BitmapDrawable iconDrawable = (BitmapDrawable) _packageManager.getApplicationIcon(bhvName);
						notiElemView.setImageViewBitmap(R.id.noti_elem_image, iconDrawable.getBitmap());
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					notiElemView.setOnClickPendingIntent(R.id.noti_elem_image, PendingIntent.getActivity(this, 0, launchIntent, 0));
				}
			} else if (bhvType == BhvType.CALL){
				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemView.setImageViewBitmap(R.id.noti_elem_image, callContactIcon);

				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				notiElemView.setOnClickPendingIntent(R.id.noti_elem_image, PendingIntent.getActivity(this, 0, callIntent, 0));
				notiElemView.setTextViewText(R.id.noti_elem_text, 
						(String) predictedBhv.getMeta("cachedName"));
			} else {
				continue;
			}
			
			notiView.addView(R.id.noti_container, notiElemView);
		}

		return notiView;
	}
	
	private void storeNewPredictedBhv(List<PredictedBhvInfo> predictedBhvInfoList) {
		Set<UserBhv> lastPredictedBhvSet = AppShuttleApplication.getContext().getRecentPredictedBhvSet();
		PredictedBhvInfoDao predictedBhvDao = PredictedBhvInfoDao.getInstance();

		Set<UserBhv> currPredictedBhvSet = new HashSet<UserBhv>();
		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			if(lastPredictedBhvSet == null || !lastPredictedBhvSet.contains(predictedBhv))
				predictedBhvDao.storePredictedBhv(predictedBhvInfo);
			currPredictedBhvSet.add(predictedBhv);
		}

		AppShuttleApplication.getContext().setRecentPredictedBhvSet(currPredictedBhvSet);
	}

}
