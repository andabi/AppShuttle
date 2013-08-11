package lab.davidahn.appshuttle.viewer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lab.davidahn.appshuttle.GlobalState;
import lab.davidahn.appshuttle.MainActivity;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.engine.matcher.Predictor;
import lab.davidahn.appshuttle.model.MatchedCxt;
import lab.davidahn.appshuttle.model.UserBhv;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.widget.RemoteViews;

public class NotiViewService extends Service {
//	public NotiViewService() {
//		this("NotiViewService");
//	}
//
//	public NotiViewService(String name) {
//		super(name);
//	}

	private static final int NOTI_UPDATE = 1;
	private NotificationManager notificationManager;
//	private PatternManager patternManager;
	private PackageManager packageManager;
//	private ContextManager contextManager;
//	private LayoutInflater layoutInflater;
	private SharedPreferences settings;

	public void onCreate(){
		super.onCreate();
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//		patternManager = PatternManager.getInstance(getApplicationContext());
		packageManager = getPackageManager();
//		contextManager = ContextManager.getInstance(getApplicationContext());
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
	}
	
	@SuppressLint("NewApi")
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
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
		iconSlotScoreIdList.offer(R.id.icon_slot0_score);
		iconSlotScoreIdList.offer(R.id.icon_slot1_score);
		iconSlotScoreIdList.offer(R.id.icon_slot2_score);
		iconSlotScoreIdList.offer(R.id.icon_slot3_score);

		notiRemoteViews.setOnClickPendingIntent(R.id.icon, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

		Predictor predictor = new Predictor(getApplicationContext());
		List<MatchedCxt> matchedCxtListForView = predictor.predict(settings.getInt("viewer.noti.num_slot", 4));
		
		List<UserBhv> matchedBhvList = new ArrayList<UserBhv>();
		for(MatchedCxt matchedCxt : matchedCxtListForView) {
			UserBhv uBhv = matchedCxt.getUserBhv();
			String bhvName = uBhv.getBhvName();
			double likelihood = matchedCxt.getLikelihood();
			matchedBhvList.add(uBhv);
			Intent launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
			if(launchIntent == null){
				;
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
				notiRemoteViews.setTextViewText(iconSlotScoreId, new DecimalFormat("##.#").format(likelihood)
						+"\n"+matchedCxt.getCondition().substring(0, 4));
			}
		}
		
		for(MatchedCxt matchedCxt : matchedCxtListForView) {
			if(GlobalState.recentMatchedBhvList == null ||
					!GlobalState.recentMatchedBhvList.contains(matchedCxt.getUserBhv())){
				predictor.storePrediction(matchedCxt);
			}
		}

		Notification notiUpdate;
		if(matchedCxtListForView.isEmpty()) {
			notiUpdate = new Notification.Builder(NotiViewService.this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContent(notiRemoteViews)
			.setOngoing(true)
			.build();
//			notificationManager.cancel(NOTI_UPDATE);
			notificationManager.notify(NOTI_UPDATE, notiUpdate);
		} else { 
			if(matchedBhvList.equals(GlobalState.recentMatchedBhvList)){
				notiUpdate = new Notification.Builder(NotiViewService.this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContent(notiRemoteViews)
				.setOngoing(true)
				.build();
			} else {
				notiUpdate = new Notification.Builder(NotiViewService.this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContent(notiRemoteViews)
				.setOngoing(true)
				.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
				.build();
			}
			notificationManager.notify(NOTI_UPDATE, notiUpdate);
		}
		GlobalState.recentMatchedBhvList = matchedBhvList;	

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
