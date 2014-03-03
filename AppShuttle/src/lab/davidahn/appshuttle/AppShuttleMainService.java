package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.BhvCollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.collect.EnvCollectionService;
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
	private PendingIntent bhvCollectionOperation;
	private PendingIntent envCollectionOperation;
	private PendingIntent predictionOperation;
	private PendingIntent compactionOperation;
	
	@Override
	public void onCreate() {
		super.onCreate();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		registerReceivers();
		startPeriodicBhvCollection();
		startPeriodicEnvCollection();
		startPeriodicCompaction();
		startPeriodicPrediction();
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
		
		stopPeriodicBhvCollection();
		stopPeriodicEnvCollection();
		stopPeriodicCompaction();
		stopPeriodicPrediction();
		
		unregisterReceiver(screenOnReceiver);
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(predictReceiver);
		unregisterReceiver(sleepModeReceiver);
		unregisterReceiver(screenOrientationReceiver);
		
		stopService(new Intent(AppShuttleMainService.this, BhvCollectionService.class));
		stopService(new Intent(AppShuttleMainService.this, EnvCollectionService.class));
		stopService(new Intent(AppShuttleMainService.this, CompactionService.class));
		stopService(new Intent(AppShuttleMainService.this, UnregisterBhvService.class));
		stopService(new Intent(AppShuttleMainService.this, PredictionService.class));
		
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
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
	}

	private void startPeriodicBhvCollection() {
		if(preferenceSettings.getBoolean("collection.bhv.enabled", true)){
			Intent bhvCollectionIntent = new Intent(this, BhvCollectionService.class);
			bhvCollectionOperation = PendingIntent.getService(this, 0, bhvCollectionIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("collection.bhv.period", 30000), bhvCollectionOperation);
		}
	}
	
	private void startPeriodicEnvCollection() {
		if(preferenceSettings.getBoolean("collection.env.enabled", true)){
			Intent envCollectionIntent = new Intent(this, EnvCollectionService.class);
			envCollectionOperation = PendingIntent.getService(this, 0, envCollectionIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("collection.env.period", 60000), envCollectionOperation);
		}
	}

	private void startPeriodicCompaction() {
		if(preferenceSettings.getBoolean("compaction.enabled", true)){
			Intent compactionCxtIntent = new Intent(this, CompactionService.class);
			compactionOperation = PendingIntent.getService(this, 0, compactionCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(3), preferenceSettings.getLong("compaction.period", AlarmManager.INTERVAL_DAY), compactionOperation);
		}
	}

	private void startPeriodicPrediction() {
		Intent predictionIntent = new Intent().setAction("lab.davidahn.appshuttle.PREDICT");
		predictionOperation = PendingIntent.getBroadcast(this, 0, predictionIntent, 0);
		long period = preferenceSettings.getLong("predictor.period", 120000);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, predictionOperation);
	}

	private void stopPeriodicBhvCollection(){
		
		alarmManager.cancel(bhvCollectionOperation);
	}
	
	private void stopPeriodicEnvCollection(){
		alarmManager.cancel(envCollectionOperation);
	}
	
	private void stopPeriodicCompaction(){
		alarmManager.cancel(compactionOperation);
	}
	
	private void stopPeriodicPrediction(){
		alarmManager.cancel(predictionOperation);
	}

	private void doBhvCollection() {
		startService(new Intent(this, BhvCollectionService.class));
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
	
	public long getExecuteTimeHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if(calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startPeriodicPrediction();
			startPeriodicBhvCollection();
		}
	};
	
	BroadcastReceiver screenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			stopPeriodicPrediction();
			stopPeriodicBhvCollection();
			doBhvCollection();
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
}