package lab.davidahn.appshuttle.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
		RemoteViews notiRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
		notiRemoteViews.setOnClickPendingIntent(R.id.icon, PendingIntent.getActivity(this, 0, new Intent(this, AppShuttleMainActivity.class), 0));
		
		fillNotiRemoteViews(notiRemoteViews, predictedBhvInfoList);

		Notification notiUpdate = new Notification.Builder(NotiViewService.this)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiRemoteViews)
			.setPriority(Notification.PRIORITY_HIGH)
			.setOngoing(true)
			.build();
		_notificationManager.notify(NOTI_UPDATE, notiUpdate);
	}

	private void fillNotiRemoteViews(RemoteViews notiRemoteViews, List<PredictedBhvInfo> predictedBhvInfoList) {
		Queue<Integer> iconSlotIdList = new LinkedList<Integer>();
		iconSlotIdList.offer(R.id.icon_slot0);
		iconSlotIdList.offer(R.id.icon_slot1);
		iconSlotIdList.offer(R.id.icon_slot2);
		iconSlotIdList.offer(R.id.icon_slot3);
		
		Queue<Integer> iconSlotScoreIdList = new LinkedList<Integer>();
		iconSlotScoreIdList.offer(R.id.icon_slot0_text);
		iconSlotScoreIdList.offer(R.id.icon_slot1_text);
		iconSlotScoreIdList.offer(R.id.icon_slot2_text);
		iconSlotScoreIdList.offer(R.id.icon_slot3_text);

		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			BhvType bhvType = predictedBhv.getBhvType();
			String bhvName = predictedBhv.getBhvName();
			
			if(bhvType == BhvType.APP){
				Intent launchIntent = _packageManager.getLaunchIntentForPackage(bhvName);
				if(launchIntent == null){
					continue;
				} else {
					int iconSlotId = iconSlotIdList.poll();
					int iconSlotScoreId = iconSlotScoreIdList.poll();
					
					try {
						BitmapDrawable iconDrawable = (BitmapDrawable) _packageManager.getApplicationIcon(bhvName);
						notiRemoteViews.setImageViewBitmap(iconSlotId, iconDrawable.getBitmap());
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					notiRemoteViews.setOnClickPendingIntent(iconSlotId, PendingIntent.getActivity(this, 0, launchIntent, 0));
					notiRemoteViews.setTextViewText(iconSlotScoreId, "");
				}
			} else if (bhvType == BhvType.CALL){
				int iconSlotId = iconSlotIdList.poll();
				int iconSlotScoreId = iconSlotScoreIdList.poll();
				
				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiRemoteViews.setImageViewBitmap(iconSlotId, callContactIcon);

				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				notiRemoteViews.setOnClickPendingIntent(iconSlotId, PendingIntent.getActivity(this, 0, callIntent, 0));
				notiRemoteViews.setTextViewText(iconSlotScoreId, 
						(String) predictedBhv.getMeta("cachedName"));
			} else {
				continue;
			}
			
			if(iconSlotIdList.isEmpty()) 
				break;
		}
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
