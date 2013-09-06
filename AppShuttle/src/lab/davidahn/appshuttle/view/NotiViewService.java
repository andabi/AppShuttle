package lab.davidahn.appshuttle.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.MainActivity;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhv;
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
	private NotificationManager notificationManager;
	private PackageManager packageManager;
//	private LayoutInflater layoutInflater;

	public void onCreate(){
		super.onCreate();
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		packageManager = getPackageManager();
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	@SuppressLint("NewApi")
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
//		SharedPreferences settings = Settings.preferenceSettings;
		
//		View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//		ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//		ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));

		RemoteViews notiRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
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

		notiRemoteViews.setOnClickPendingIntent(R.id.icon, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

		Predictor predictor = new Predictor(getApplicationContext());
		List<PredictedBhv> predictedBhvForView = predictor.predict(Integer.MAX_VALUE);
		
		Set<UserBhv> matchedBhvSet = new HashSet<UserBhv>();
		for(PredictedBhv predictedBhv : predictedBhvForView) {
			UserBhv uBhv = predictedBhv.getUserBhv();
			BhvType bhvType = uBhv.getBhvType();
			String bhvName = uBhv.getBhvName();
			if(bhvType == BhvType.APP){
				Intent launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
				if(launchIntent == null){
					continue;
				} else {
					int iconSlotId = iconSlotIdList.poll();
					int iconSlotScoreId = iconSlotScoreIdList.poll();
					
					try {
						BitmapDrawable iconDrawable = (BitmapDrawable) packageManager.getApplicationIcon(bhvName);
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
						(String) uBhv.getMeta("cachedName"));
			} else {
				continue;
			}
			matchedBhvSet.add(uBhv);
			if(iconSlotIdList.isEmpty()) 
				break;
		}
		
		for(PredictedBhv predictedBhv : predictedBhvForView) {
			if(GlobalState.recentMatchedBhvSet == null ||
					!GlobalState.recentMatchedBhvSet.contains(predictedBhv.getUserBhv())){
				predictor.storePredictedBhv(predictedBhv);
			}
		}

		Notification notiUpdate;
		if(predictedBhvForView.isEmpty()) {
			notiUpdate = new Notification.Builder(NotiViewService.this)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiRemoteViews)
			.setOngoing(true)
			.build();
//			notificationManager.cancel(NOTI_UPDATE);
			notificationManager.notify(NOTI_UPDATE, notiUpdate);
		} else { 
			if(matchedBhvSet.equals(GlobalState.recentMatchedBhvSet)){
				notiUpdate = new Notification.Builder(NotiViewService.this)
				.setSmallIcon(R.drawable.appshuttle)
				.setContent(notiRemoteViews)
				.setOngoing(true)
				.build();
			} else {
				notiUpdate = new Notification.Builder(NotiViewService.this)
				.setSmallIcon(R.drawable.appshuttle)
				.setContent(notiRemoteViews)
				.setOngoing(true)
				.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
				.build();
			}
			notificationManager.notify(NOTI_UPDATE, notiUpdate);
		}
		GlobalState.recentMatchedBhvSet = matchedBhvSet;	

		return START_NOT_STICKY;
	}
	
	public void onDestroy() {
		super.onDestroy();
		notificationManager.cancelAll();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
