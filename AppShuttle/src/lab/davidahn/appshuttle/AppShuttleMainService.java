package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.CollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.collect.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.view.ui.NotiBarNotifier;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

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
		filter.addAction("lab.davidahn.appshuttle.SLEEP_MODE");
		registerReceiver(sleepModeReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(screenOrientationReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(headsetPluggedReceiver, filter);
	}

	private void doPrediction(boolean isForce) {
		if(!isForce) {
			long ignoredDelay = preferenceSettings.getLong("predictor.delay_ignorance", 60000);
			if(System.currentTimeMillis() - AppShuttleApplication.lastPredictionTime < ignoredDelay)
				return;
		}
		startService(new Intent(this, PredictionService.class));
		AppShuttleApplication.lastPredictionTime = System.currentTimeMillis();
//		Log.d("prediction", "prediction service started, isForce=" + isForce);
	}
	
	private void startPeriodicPrediction() {
		Intent predictionIntent = new Intent().setAction("lab.davidahn.appshuttle.PREDICT");
		predictionOperation = PendingIntent.getBroadcast(this, 0, predictionIntent, 0);
		long period = preferenceSettings.getLong("predictor.period", 120000);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, predictionOperation);
	}
	
	private void stopPeriodicPrediction(){
		alarmManager.cancel(predictionOperation);
	}
	
	public long getExecuteTimeHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if(calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent){
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
		unregisterReceiver(sleepModeReceiver);
		unregisterReceiver(screenOrientationReceiver);
		unregisterReceiver(headsetPluggedReceiver);
		
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
			doPrediction(false);
			stopPeriodicPrediction();
		}
	};

	BroadcastReceiver sleepModeReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isOn = intent.getBooleanExtra("isOn", false);
			if(isOn)
				stopPeriodicPrediction();
			else
				startPeriodicPrediction();
		}
	};
	
	BroadcastReceiver predictReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isForce = intent.getBooleanExtra("isForce", false);
			doPrediction(isForce);
		}
	};
	
	BroadcastReceiver screenOrientationReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			NotiBarNotifier.getInstance().updateNotification();
//			if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		}
	};
	
	BroadcastReceiver headsetPluggedReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(AppShuttleApplication.currUserCxt != null){
//				boolean plugged = (intent.getIntExtra("state", 0) == 1);
				doPrediction(true);
			}
		}
	};
}