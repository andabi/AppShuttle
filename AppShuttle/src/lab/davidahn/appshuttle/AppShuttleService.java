package lab.davidahn.appshuttle;

import java.util.Calendar;

import lab.davidahn.appshuttle.collector.CollectingCxtService;
import lab.davidahn.appshuttle.report.ReportingCxtService;
import lab.davidahn.appshuttle.viewer.NotiViewService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

public class AppShuttleService extends Service{
//	private boolean isRunning;
//	private Properties property;
	private AlarmManager alarmManager;
	private PendingIntent collectingCxtOperation;
	private PendingIntent reportingCxtOperation;
	private PendingIntent notiViewOperation;
	private SharedPreferences settings;
	
	BroadcastReceiver screenOnReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			GlobalState.isInUse = true;
		}
	};
	
	BroadcastReceiver screenOffReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			GlobalState.isInUse = false;
		}
	};

	BroadcastReceiver notiViewReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(GlobalState.isInUse == true) {
				context.startService(new Intent(context, NotiViewService.class));
			}
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();

//		isRunning = false;
		GlobalState.isInUse = true;
		
		//preference settings
		settings = getSharedPreferences("AppShuttle", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString("database.name", new StringBuilder(getResources().getString(R.string.app_name)).append(".db").toString());

		editor.putLong("collection.location.tolerance.time", 6000);
		editor.putInt("collection.location.tolerance.distance", 100);
		editor.putBoolean("collection.store_cxt.enabled", false);
		
		editor.putBoolean("service.collection.enabled", true);
		editor.putLong("service.collection.period", 6000);
		editor.putBoolean("service.view.enabled", true);
		editor.putLong("service.view.peroid", 30000);
		editor.putBoolean("service.report.enabled", false);
		
		editor.putString("email.sender.addr", "davidahn412@gmail.com");
		editor.putString("email.sender.pwd", "rnrmfepdl");
		editor.putString("email.receiver.addr", "andabi412@gmail.com");
		
		editor.putLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);

		editor.putLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6);
		editor.putFloat("matcher.freq.min_likelihood", 0);
		editor.putInt("matcher.freq.min_num_cxt", 3);
		
		editor.putLong("matcher.time.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.time.min_likelihood", 0.7f);
		editor.putInt("matcher.time.min_num_cxt", 3);
		editor.putLong("matcher.time.tolerance", settings.getLong("matcher.time.acceptance_delay", AlarmManager.INTERVAL_HOUR) / 2);

		editor.putFloat("matcher.loc.min_likelihood", 0.1f);
		editor.putInt("matcher.loc.min_num_cxt", 2);
		editor.putInt("matcher.loc.min_distance", 300);

		editor.putInt("viewer.noti.num_slot", 4);

		editor.commit();
		
//		GlobalState.settings = settings;
		
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
		
		if(settings.getBoolean("service.collection.enabled", true)){
			Intent collectingCxtIntent = new Intent(this, CollectingCxtService.class);
			collectingCxtOperation = PendingIntent.getService(this, 0, collectingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), settings.getLong("service.collection.period", 6000), collectingCxtOperation);
		}
		
		if(settings.getBoolean("service.view.enabled", true)){
			Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW");
			notiViewOperation = PendingIntent.getBroadcast(this, 0, notiViewIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), settings.getLong("service.view.period", 30000), notiViewOperation);
		}
		
		if(settings.getBoolean("service.report.enabled", false)){
			Intent reportingCxtIntent = new Intent(this, ReportingCxtService.class);
			reportingCxtOperation = PendingIntent.getService(this, 0, reportingCxtIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, getReportingTimeHour(21), AlarmManager.INTERVAL_DAY, reportingCxtOperation);
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
//		if(!isRunning) {
//			isRunning = true;
//		}
		return START_STICKY;
	}
	
	public long getReportingTimeHour(int hourOfDay){
		Calendar calendar = Calendar.getInstance();		
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if(calendar.getTimeInMillis() < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}

	public void onDestroy() {
		super.onDestroy();
		GlobalState.isInUse = false;
		alarmManager.cancel(collectingCxtOperation);
		alarmManager.cancel(reportingCxtOperation);
		alarmManager.cancel(notiViewOperation);
		unregisterReceiver(screenOnReceiver);
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(notiViewReceiver);
		stopService(new Intent(AppShuttleService.this, CollectingCxtService.class));
		stopService(new Intent(AppShuttleService.this, NotiViewService.class));
		stopService(new Intent(AppShuttleService.this, ReportingCxtService.class));
//		notificationManager.cancelAll();
//		isRunning = false;
	}
}

