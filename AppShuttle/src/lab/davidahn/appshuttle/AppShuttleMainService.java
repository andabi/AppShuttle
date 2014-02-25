package lab.davidahn.appshuttle;

import java.lang.reflect.Method;
import java.util.Calendar;

import lab.davidahn.appshuttle.bhv.ViewableUserBhv;
import lab.davidahn.appshuttle.collect.CollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.NotiBarNotifier;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AppShuttleMainService extends Service {
	private AlarmManager alarmManager;
	private SharedPreferences preferenceSettings;
	private PendingIntent collectionOperation;
	private PendingIntent predictionOperation;
	private PendingIntent compactionOperation;
	
	@Override
	public void onCreate() {
		super.onCreate();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		registerReceivers();

		startPeriodicCollection();
		startPeriodicCompaction();
		startPeriodicPrediction();
	}
	
	private void startPeriodicCollection() {
		if(preferenceSettings.getBoolean("collection.enabled", true)){
			Intent collectionIntent = new Intent(this, CollectionService.class);
			collectionOperation = PendingIntent.getService(this, 0, collectionIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("collection.period", 6000), collectionOperation);
		}
	}

	private void startPeriodicCompaction() {
		if(preferenceSettings.getBoolean("compaction.enabled", true)){
			Intent compactionCxtIntent = new Intent(this, CompactionService.class);
			compactionOperation = PendingIntent.getService(this, 0, compactionCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(3), preferenceSettings.getLong("compaction.period", AlarmManager.INTERVAL_DAY), compactionOperation);
		}
	}

	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenOnReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenOffReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.PREDICT");
		registerReceiver(predictReceiver, filter);

		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.TRY_PREDICT");
		registerReceiver(tryPredictReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(screenOrientationReceiver, filter);
	}

	private void tryPrediction() {
		long ignoredDelay = preferenceSettings.getLong("predictor.delay_ignorance", 180000);
		if(System.currentTimeMillis() - AppShuttleApplication.lastPredictionTime < ignoredDelay)
			return;
	
		doPrediction();
	}
	
	private void doPrediction() {
		startService(new Intent(this, PredictionService.class));
		AppShuttleApplication.lastPredictionTime = System.currentTimeMillis();
	}
	
	private void startPeriodicPrediction() {
		Intent predictionIntent = new Intent().setAction("lab.davidahn.appshuttle.TRY_PREDICT");
		predictionOperation = PendingIntent.getBroadcast(this, 0, predictionIntent, 0);
		long period = preferenceSettings.getLong("predictor.period", 180000);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, predictionOperation);
	}

	public long getExecuteTimeHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if(calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	private void handleExecutionIntent(Intent intent){
		if (intent == null)
			return;
		Log.i("Service", (intent != null ? intent.toString() : "null"));
		
		Bundle b = intent.getExtras();
		if (b == null || (b.getBoolean("doExec", false) == false))
			return;
		
		/* 이 아래의 코드들은 doExec 이 true일 때만 사용됨 */
		UserBhvType bhvType = UserBhvType.NONE;
		String bhvName = b.getString("bhvName");
		if (b.containsKey("bhvType"))
			bhvType = (UserBhvType)b.getSerializable("bhvType");
		
		if (bhvType == UserBhvType.NONE || bhvName == null)
			return;
		
		BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
		ViewableUserBhv vBhv = new ViewableUserBhv(uBhv);
		
		Log.i("Service", "Exec req " + uBhv.toString());

		StatCollector.getInstance().notifyBhvTransition(uBhv, true);
		Intent launchIntent = vBhv.getLaunchIntent();
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 전화 bhv 띄우기 위해서 필요
		this.startActivity(launchIntent);
		
		/* status 바 숨기기
		 * 참고
		 * http://stackoverflow.com/questions/16966540/how-to-hide-notification-panel-after-sending-a-pendingintent-to-service-in-a-not
		 */
		try
		{
		    Object service  = getSystemService("statusbar");
		    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");

		    Method collapse;
		    if (Build.VERSION.SDK_INT <= 16)
		    	collapse = statusbarManager.getMethod("collapse");
		    else
		    	collapse = statusbarManager.getMethod("collapsePanels");
		    
		    collapse.setAccessible(true);
		    collapse.invoke(service);
		}catch(Exception ex){}
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		// handleExecutionIntent(intent);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		// handleExecutionIntent(intent);
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		alarmManager.cancel(collectionOperation);
		alarmManager.cancel(predictionOperation);
		alarmManager.cancel(compactionOperation);
		
		unregisterReceiver(screenOnReceiver);
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(predictReceiver);
		unregisterReceiver(tryPredictReceiver);
		unregisterReceiver(screenOrientationReceiver);
		
		stopService(new Intent(AppShuttleMainService.this, CollectionService.class));
		stopService(new Intent(AppShuttleMainService.this, CompactionService.class));
		stopService(new Intent(AppShuttleMainService.this, UnregisterBhvService.class));
		stopService(new Intent(AppShuttleMainService.this, PredictionService.class));
		
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}
	
	BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startPeriodicPrediction();
		}
	};
	
	BroadcastReceiver screenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			tryPrediction();
			alarmManager.cancel(predictionOperation);
		}
	};

	BroadcastReceiver predictReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			doPrediction();
		}
	};
	
	BroadcastReceiver tryPredictReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			tryPrediction();
		}
	};
	
	BroadcastReceiver screenOrientationReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			NotiBarNotifier.getInstance().doNotification();
//			if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		}
	};
}