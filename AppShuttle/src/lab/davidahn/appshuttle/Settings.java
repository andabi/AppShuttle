package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;


public class Settings {
	public static void preferenceSettings(Context cxt) {
		SharedPreferences preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferenceSettings.edit();
		
		//general
		editor.putString("database.name", new StringBuilder(cxt.getResources().getString(R.string.app_name)).append(".db").toString());

		//service
		editor.putBoolean("service.collection.enabled", true);
		editor.putLong("service.collection.period", 10000);
		
		editor.putBoolean("service.view.enabled", true);
		editor.putLong("service.view.peroid", 300000);
		
		editor.putBoolean("service.report.enabled", false);
		editor.putLong("service.report.period", AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("service.compaction.enabled", true);
		editor.putLong("service.compaction.period", AlarmManager.INTERVAL_DAY);
		editor.putLong("service.compaction.expiration", 30 * AlarmManager.INTERVAL_DAY);

		//collection
		editor.putLong("collection.location.tolerance.time", 10000);
		editor.putInt("collection.location.tolerance.distance", 500);
		
		editor.putLong("collection.call.initial_history.period", 6 * AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("collection.store_cxt.enabled", false);
		
		//report
		editor.putString("email.sender.addr", "davidahn412@gmail.com");
		editor.putString("email.sender.pwd", "rnrmfepdl");
		
		editor.putString("email.receiver.addr", "andabi412@gmail.com");
		
		//matcher
		editor.putLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		
		editor.putLong("matcher.freq.duration", AlarmManager.INTERVAL_DAY);
		editor.putInt("matcher.freq.min_num_cxt", 3);
		editor.putLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3);

		editor.putLong("matcher.weak_time.duration", 4 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.weak_time.min_likelihood", 0.5f);
		editor.putFloat("matcher.weak_time.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.weak_time.min_num_cxt", 2);
		editor.putLong("matcher.weak_time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR);
		editor.putLong("matcher.weak_time.tolerance", preferenceSettings.getLong("matcher.weak_time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR) / 2);
		
		editor.putLong("matcher.strict_time.duration", 4 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.strict_time.min_likelihood", 0.5f);
		editor.putFloat("matcher.strict_time.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.strict_time.min_num_cxt", 2);
		editor.putLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3);
		editor.putLong("matcher.strict_time.tolerance", preferenceSettings.getLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3) / 2);
		
		editor.putLong("matcher.place.duration", 6 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.place.min_likelihood", 0.7f);
		editor.putFloat("matcher.place.min_inverse_entropy", 0.3f);
		editor.putInt("matcher.place.min_num_cxt", 3);
		editor.putInt("matcher.place.distance_tolerance", 2000);
		
		editor.putLong("matcher.loc.duration", 6 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.loc.min_likelihood", 0.7f);
		editor.putFloat("matcher.loc.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.loc.min_num_cxt", 3);
		editor.putInt("matcher.loc.distance_tolerance", 100);

		//view
		editor.putInt("viewer.noti.num_slot", 4);

		editor.commit();
	}
}
