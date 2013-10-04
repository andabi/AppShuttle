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
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

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
			.setPriority(Notification.PRIORITY_HIGH)
			.setOngoing(true)
			.build();
		_notificationManager.notify(NOTI_UPDATE, notiUpdate);
	}

	private RemoteViews createNotiRemoteViews(List<PredictedBhvInfo> predictedBhvInfoList) {

//		Queue<Integer> iconSlotIdList = new LinkedList<Integer>();
//		iconSlotIdList.offer(R.id.icon_slot0);
//		iconSlotIdList.offer(R.id.icon_slot1);
//		iconSlotIdList.offer(R.id.icon_slot2);
//		iconSlotIdList.offer(R.id.icon_slot3);
//		
//		Queue<Integer> iconSlotScoreIdList = new LinkedList<Integer>();
//		iconSlotScoreIdList.offer(R.id.icon_slot0_text);
//		iconSlotScoreIdList.offer(R.id.icon_slot1_text);
//		iconSlotScoreIdList.offer(R.id.icon_slot2_text);
//		iconSlotScoreIdList.offer(R.id.icon_slot3_text);
		
		RemoteViews notiRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
		notiRemoteViews.setOnClickPendingIntent(R.id.icon, PendingIntent.getActivity(this, 0, new Intent(this, AppShuttleMainActivity.class), 0));
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

		TextView textView = new TextView(this);
		textView.setText("text");
		textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT));
		notiViewLayout.addView(textView);
		
		int count = 3;
		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			BhvType bhvType = predictedBhv.getBhvType();
			String bhvName = predictedBhv.getBhvName();
			
			if(bhvType == BhvType.APP){
				Intent launchIntent = _packageManager.getLaunchIntentForPackage(bhvName);
				if(launchIntent == null){
					continue;
				} else {
//					int iconSlotImageId = iconSlotIdList.poll();
//					int iconSlotTextId = iconSlotScoreIdList.poll();
					
					LinearLayout bhvBundleLayout = new LinearLayout(this);
					bhvBundleLayout.setOrientation(LinearLayout.VERTICAL);
					MarginLayoutParams bhvBundleLayoutParams = new LinearLayout.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.MATCH_PARENT);
					bhvBundleLayoutParams.setMargins(0, 0, 15, 0);
					bhvBundleLayout.setLayoutParams(bhvBundleLayoutParams);
					bhvBundleLayout.setGravity(Gravity.CENTER);
					
					ImageView bhvIconView = new ImageView(this);
					bhvIconView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT));
					bhvIconView.setClickable(true);
					try {
						BitmapDrawable iconDrawable = (BitmapDrawable) _packageManager.getApplicationIcon(bhvName);
						bhvIconView.setImageBitmap(iconDrawable.getBitmap());
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}

					TextView bhvTextView = new TextView(this);
					bhvTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT));
					bhvTextView.setText("text");
					
					bhvBundleLayout.addView(bhvIconView);
					bhvBundleLayout.addView(bhvTextView);
					
//					notiRemoteViews = new RemoteViews(getPackageName(), bhvBundleLayout.getId());
//					notiRemoteViews.getLayoutId();
//					notiLayout.addView(bhvTextView);
					notiViewLayout.addView(bhvBundleLayout);
					notiRemoteViews.setOnClickPendingIntent(bhvIconView.getId(), PendingIntent.getActivity(this, 0, launchIntent, 0));
//					
//					try {
//						BitmapDrawable iconDrawable = (BitmapDrawable) _packageManager.getApplicationIcon(bhvName);
//						notiRemoteViews.setImageViewBitmap(bhvIconView.getId(), iconDrawable.getBitmap());
//					} catch (NameNotFoundException e) {
//						e.printStackTrace();
//					} catch (NullPointerException e) {
//						e.printStackTrace();
//					}
//					notiRemoteViews.setOnClickPendingIntent(bhvIconView.getId(), PendingIntent.getActivity(this, 0, launchIntent, 0));
//					notiRemoteViews.setTextViewText(iconSlotTextId, "");
				}
			} else if (bhvType == BhvType.CALL){
//				int iconSlotId = iconSlotIdList.poll();
//				int iconSlotScoreId = iconSlotScoreIdList.poll();
//				
//				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
//				notiRemoteViews.setImageViewBitmap(iconSlotId, callContactIcon);
//
//				Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
//				notiRemoteViews.setOnClickPendingIntent(iconSlotId, PendingIntent.getActivity(this, 0, callIntent, 0));
//				notiRemoteViews.setTextViewText(iconSlotScoreId, 
//						(String) predictedBhv.getMeta("cachedName"));
			} else {
				continue;
			}
			
			if(--count <= 0)
				break;
//			if(iconSlotIdList.isEmpty()) 
//				break;
		}

		notiRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
//		notiRemoteViews.addView(R.layout.notification_view, notiViewLayout);
		return notiRemoteViews;
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
