package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collect.CollectionService;
import lab.davidahn.appshuttle.collect.CompactionService;
import lab.davidahn.appshuttle.context.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.report.ReportingCxtService;
import lab.davidahn.appshuttle.view.NotiViewService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

public class AppShuttleService extends Service {
	private AlarmManager alarmManager;
	private PendingIntent collectingCxtOperation;
	private PendingIntent reportingCxtOperation;
	private PendingIntent notiViewOperation;
	private PendingIntent compactingCxtOperation;
	private SharedPreferences preferenceSettings;
	
	@Override
	public void onCreate() {
		super.onCreate();

		DBHelper.create(getApplicationContext());
		
		Settings.preferenceSettings(getApplicationContext());
		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenOnReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenOffReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.UPDATE_VIEW");
		registerReceiver(notiViewReceiver, filter);

		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		if(preferenceSettings.getBoolean("service.collection.enabled", true)){
			Intent collectingCxtIntent = new Intent(this, CollectionService.class);
			collectingCxtOperation = PendingIntent.getService(this, 0, collectingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.collection.period", 6000), collectingCxtOperation);
		}

		if(preferenceSettings.getBoolean("service.compaction.enabled", true)){
			Intent compactingCxtIntent = new Intent(this, CompactionService.class);
			compactingCxtOperation = PendingIntent.getService(this, 0, compactingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getReportingTimeHour(3), preferenceSettings.getLong("service.compaction.period", AlarmManager.INTERVAL_DAY), compactingCxtOperation);
		}
		if(preferenceSettings.getBoolean("service.report.enabled", false)){
			Intent reportingCxtIntent = new Intent(this, ReportingCxtService.class);
			reportingCxtOperation = PendingIntent.getService(this, 0, reportingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getReportingTimeHour(3), preferenceSettings.getLong("service.report.period", AlarmManager.INTERVAL_DAY), reportingCxtOperation);
		}

		if(preferenceSettings.getBoolean("service.view.enabled", true)){
			Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW");
			notiViewOperation = PendingIntent.getBroadcast(this, 0, notiViewIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.view.period", 30000), notiViewOperation);
		}
	}
	
	public long getReportingTimeHour(int hourOfDay){
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
		unregisterReceiver(notiViewReceiver);
		
		stopService(new Intent(AppShuttleService.this, CollectionService.class));
		stopService(new Intent(AppShuttleService.this, CompactionService.class));
		stopService(new Intent(AppShuttleService.this, UnregisterBhvService.class));
		stopService(new Intent(AppShuttleService.this, ReportingCxtService.class));
		stopService(new Intent(AppShuttleService.this, NotiViewService.class));
	}
	
	BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if(preferenceSettings.getBoolean("service.view.enabled", true)){
				Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW");
				PendingIntent notiViewOperation = PendingIntent.getBroadcast(context, 0, notiViewIntent, 0);
				alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), preferenceSettings.getLong("service.view.period", 30000), notiViewOperation);
			}
		}
	};
	
	BroadcastReceiver screenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			alarmManager.cancel(notiViewOperation);
		}
	};

	BroadcastReceiver notiViewReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.startService(new Intent(context, NotiViewService.class));
		}
	};
}

