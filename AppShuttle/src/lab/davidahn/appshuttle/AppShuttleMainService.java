package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.BhvCollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.collect.EnvCollectionService;
import lab.davidahn.appshuttle.collect.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.view.ViewService;
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
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

public class AppShuttleMainService extends Service {
	private AlarmManager alarmManager;
	private SharedPreferences preferenceSettings;
	private PendingIntent bhvCollectionOperation;
	private PendingIntent envCollectionOperation;
	private PendingIntent predictionOperation;
	private PendingIntent updateViewOperation;
	private PendingIntent compactionOperation;
	
	@Override
	public void onCreate() {
		super.onCreate();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();
		registerReceivers();
		startPeriodicOperationsAlways();
	    if(((PowerManager)getSystemService(Context.POWER_SERVICE)).isScreenOn())
	    	startPeriodicOperationsScreenOn();
	    
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
		
		stopPeriodicOperationsAlways();
		stopPeriodicOperationsScreenOn();
		
		unregisterReceiver(receiver);
		
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
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(PredictionService.PREDICT);
		filter.addAction(AppShuttlePreferences.SLEEP_MODE);
		registerReceiver(receiver, filter);
	}
	
	private void startPeriodicOperationsAlways() {
		startPeriodicEnvCollection();
		startPeriodicCompaction();
	}
	
	private void stopPeriodicOperationsAlways() {
		stopPeriodicEnvCollection();
		stopPeriodicCompaction();
	}
	
	private void startPeriodicOperationsScreenOn() {
		startPeriodicBhvCollection();
		startPeriodicPrediction();
		startPeriodicUpdateView();
	}
	
	private void stopPeriodicOperationsScreenOn() {
		stopPeriodicBhvCollection();
		stopPeriodicPrediction();
		stopPeriodicUpdateView();
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
		Intent predictionIntent = new Intent().setAction(PredictionService.PREDICT);
		predictionOperation = PendingIntent.getBroadcast(this, 0, predictionIntent, 0);
		long period = preferenceSettings.getLong("predictor.period", 120000);
		alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, predictionOperation);
	}
	
	private void startPeriodicUpdateView() {
		Intent predictionIntent = new Intent(this, ViewService.class).putExtra("isOnlyNotibar", true);
		updateViewOperation = PendingIntent.getService(this, 0, predictionIntent, 0);
		long period = preferenceSettings.getLong("view.update_period", 15000);
		alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, updateViewOperation);
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
	
	private void stopPeriodicUpdateView(){
		alarmManager.cancel(updateViewOperation);
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

	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				startPeriodicOperationsScreenOn();
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				stopPeriodicOperationsScreenOn();
				startService(new Intent(context, BhvCollectionService.class));
				startService(new Intent(context, ViewService.class).putExtra("isOnlyNotibar", true));
			} else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
				NotiBarNotifier.getInstance().updateNotification();
//				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			} else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				doPrediction(true);
//				boolean plugged = (intent.getIntExtra("state", 0) == 1);
			} else if (action.equals(PredictionService.PREDICT)) {
				boolean isForce = intent.getBooleanExtra("isForce", false);
				doPrediction(isForce);
			} else if (action.equals(AppShuttlePreferences.SLEEP_MODE)) {
				boolean isOn = intent.getBooleanExtra("isOn", false);
				if(isOn){
					stopPeriodicPrediction();
					stopPeriodicUpdateView();
				} else {
					startPeriodicPrediction();
					startPeriodicUpdateView();
				}
			} else if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
				startService(new Intent(context, ViewService.class).putExtra("isOnlyNotibar", true));
			} else {
				return ;
			}
		}
	};
}