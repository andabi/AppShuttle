package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;


public class Settings {
	public static SharedPreferences preferenceSettings;

	public static void preferenceSettings(Context cxt) {
		preferenceSettings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferenceSettings.edit();
		
		editor.putString("database.name", new StringBuilder(cxt.getResources().getString(R.string.app_name)).append(".db").toString());

		
		editor.putLong("collection.location.tolerance.time", 10000);
		editor.putInt("collection.location.tolerance.distance", 100);
		
		editor.putInt("collection.place.tolerance.distance", 5000);
		
		editor.putLong("collection.call.initial_history.period", 5 * AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("collection.store_cxt.enabled", false);
		
		
		editor.putBoolean("service.collection.enabled", true);
		editor.putLong("service.collection.period", 10000);

		editor.putBoolean("service.view.enabled", true);
		editor.putLong("service.view.peroid", 60000);

		editor.putBoolean("service.report.enabled", false);
		editor.putLong("service.report.period", AlarmManager.INTERVAL_DAY);

		editor.putBoolean("service.compaction.enabled", true);
		editor.putLong("service.compaction.period", AlarmManager.INTERVAL_DAY);
		editor.putLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);
		
		
		editor.putString("email.sender.addr", "davidahn412@gmail.com");
		editor.putString("email.sender.pwd", "rnrmfepdl");
		
		editor.putString("email.receiver.addr", "andabi412@gmail.com");
		
		
		editor.putLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 30);
		
		editor.putLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3);
		editor.putInt("matcher.freq.min_num_cxt", 3);
		
		editor.putLong("matcher.weak_time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR);
		editor.putLong("matcher.weak_time.tolerance", preferenceSettings.getLong("matcher.time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR) / 2);
//		editor.putLong("matcher.weak_time.acceptance_delay", 0);
//		editor.putLong("matcher.weak_time.tolerance", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.weak_time.min_likelihood", 0.5f);
		editor.putInt("matcher.weak_time.min_num_cxt", 3);
		
		editor.putLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3);
		editor.putLong("matcher.strict_time.tolerance", preferenceSettings.getLong("matcher.time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3) / 2);
		editor.putFloat("matcher.strict_time.min_likelihood", 0.3f);
		editor.putInt("matcher.strict_time.min_num_cxt", 3);
		
		editor.putFloat("matcher.place.min_likelihood", 0.7f);
		editor.putInt("matcher.place.min_num_cxt", 3);
		editor.putInt("matcher.place.min_distance", 2000);
		
		editor.putFloat("matcher.loc.min_likelihood", 0.5f);
		editor.putInt("matcher.loc.min_num_cxt", 3);
		editor.putInt("matcher.loc.min_distance", 100);

		
		editor.putInt("viewer.noti.num_slot", 4);

		editor.commit();
	}
}
