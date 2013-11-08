package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.CollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.context.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.report.ReportingCxtService;
import lab.davidahn.appshuttle.view.UpdateService;
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
	private PendingIntent collectingCxtOperation;
	private PendingIntent reportingCxtOperation;
	private PendingIntent notiViewOperation;
	private PendingIntent compactingCxtOperation;
	private SharedPreferences preferenceSettings;
	
	@Override
	public void onCreate() {
		super.onCreate();

//		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenOnReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenOffReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.UPDATE");
		registerReceiver(updateReceiver, filter);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		if(preferenceSettings.getBoolean("service.collection.enabled", true)){
			Intent collectingCxtIntent = new Intent(this, CollectionService.class);
			collectingCxtOperation = PendingIntent.getService(this, 0, collectingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.collection.period", 6000), collectingCxtOperation);
		}

		if(preferenceSettings.getBoolean("service.compaction.enabled", true)){
			Intent compactingCxtIntent = new Intent(this, CompactionService.class);
			compactingCxtOperation = PendingIntent.getService(this, 0, compactingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(3), preferenceSettings.getLong("service.compaction.period", AlarmManager.INTERVAL_DAY), compactingCxtOperation);
		}
		if(preferenceSettings.getBoolean("service.report.enabled", false)){
			Intent reportingCxtIntent = new Intent(this, ReportingCxtService.class);
			reportingCxtOperation = PendingIntent.getService(this, 0, reportingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getExecuteTimeHour(3), preferenceSettings.getLong("service.report.period", AlarmManager.INTERVAL_DAY), reportingCxtOperation);
		}
		
		startRepeatingUpdateBroadCast();
	}
	
	private void startRepeatingUpdateBroadCast() {
//		if(preferenceSettings.getBoolean("service.view.enabled", true)){
		Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.UPDATE");
		notiViewOperation = PendingIntent.getBroadcast(this, 0, notiViewIntent, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.update.period", 300000), notiViewOperation);
	}
	
	private void stopRepeatingUpdateBroadCast() {
		alarmManager.cancel(notiViewOperation);
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
		
		alarmManager.cancel(collectingCxtOperation);
		alarmManager.cancel(reportingCxtOperation);
		alarmManager.cancel(notiViewOperation);
		alarmManager.cancel(compactingCxtOperation);
		
		unregisterReceiver(screenOnReceiver);
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(updateReceiver);
		
		stopService(new Intent(AppShuttleMainService.this, CollectionService.class));
		stopService(new Intent(AppShuttleMainService.this, CompactionService.class));
		stopService(new Intent(AppShuttleMainService.this, UnregisterBhvService.class));
		stopService(new Intent(AppShuttleMainService.this, ReportingCxtService.class));
		stopService(new Intent(AppShuttleMainService.this, UpdateService.class));
		
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
	}
	
	BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startRepeatingUpdateBroadCast();
		}
	};
	
	BroadcastReceiver screenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			stopRepeatingUpdateBroadCast();
		}
	};

	BroadcastReceiver updateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.startService(new Intent(context, UpdateService.class));
		}
	};
}

