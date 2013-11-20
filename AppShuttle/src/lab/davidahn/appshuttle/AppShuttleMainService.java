package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.CollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.context.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.report.ReportService;
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
	private PendingIntent collectionOperation;
	private PendingIntent reportOperation;
	private PendingIntent notiViewOperation;
	private PendingIntent compactionOperation;
	private SharedPreferences preferenceSettings;
	
	@Override
	public void onCreate() {
		super.onCreate();

		preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenOnReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenOffReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.PREDICT");
		registerReceiver(predictReceiver, filter);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		if(preferenceSettings.getBoolean("service.collection.enabled", true)){
			Intent collectionIntent = new Intent(this, CollectionService.class);
			collectionOperation = PendingIntent.getService(this, 0, collectionIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.collection.period", 6000), collectionOperation);
		}

		if(preferenceSettings.getBoolean("service.compaction.enabled", true)){
			Intent compactionCxtIntent = new Intent(this, CompactionService.class);
			compactionOperation = PendingIntent.getService(this, 0, compactionCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(3), preferenceSettings.getLong("service.compaction.period", AlarmManager.INTERVAL_DAY), compactionOperation);
		}
		if(preferenceSettings.getBoolean("service.report.enabled", false)){
			Intent reportIntent = new Intent(this, ReportService.class);
			reportOperation = PendingIntent.getService(this, 0, reportIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(10), preferenceSettings.getLong("service.report.period", AlarmManager.INTERVAL_DAY), reportOperation);
		}
		
		startPeriodicPrediction();
	}
	
	private void startPeriodicPrediction() {
		startPrediction();
		activatePeriodicPrediction();
	}
	
	private void stopPeriodicPrediction() {
		startPrediction();
		alarmManager.cancel(notiViewOperation);
	}

	private void startPrediction() {
		if(AppShuttleApplication.isPredictionServiceRunning)
			return;
		
		startService(new Intent(this, PredictionService.class));
	}

	private void activatePeriodicPrediction() {
		Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.PREDICT");
		notiViewOperation = PendingIntent.getBroadcast(this, 0, notiViewIntent, 0);
		long period = preferenceSettings.getLong("service.predict.period", 180000);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + period, period, notiViewOperation);
	}

	public long getExecuteTimeHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if(calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		
		alarmManager.cancel(collectionOperation);
		alarmManager.cancel(reportOperation);
		alarmManager.cancel(notiViewOperation);
		alarmManager.cancel(compactionOperation);
		
		unregisterReceiver(screenOnReceiver);
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(predictReceiver);
		
		stopService(new Intent(AppShuttleMainService.this, CollectionService.class));
		stopService(new Intent(AppShuttleMainService.this, CompactionService.class));
		stopService(new Intent(AppShuttleMainService.this, UnregisterBhvService.class));
		stopService(new Intent(AppShuttleMainService.this, ReportService.class));
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
			stopPeriodicPrediction();
		}
	};

	BroadcastReceiver predictReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			startPrediction();
		}
	};
}