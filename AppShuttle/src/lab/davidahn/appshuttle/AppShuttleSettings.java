package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.content.SharedPreferences;


public class AppShuttleSettings {
	public static void setDefaultPreferences() {
//		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		SharedPreferences.Editor editor = pref.edit();
		
		//general
//		editor.putString("database.name", new StringBuilder(AppShuttleApplication.getContext().getResources().getString(R.string.app_name)).append(".db").toString());
		editor.putBoolean("mode.debug", false);
		
		editor.putString("database.name", "AppShuttle.db");

		//service
		editor.putBoolean("service.collection.enabled", true);
		editor.putLong("service.collection.period", 30000);
		
		editor.putBoolean("service.report.enabled", false);
		editor.putLong("service.report.period", AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("service.compaction.enabled", true);
		editor.putLong("service.compaction.period", AlarmManager.INTERVAL_DAY);
		editor.putLong("service.compaction.expiration", 35 * AlarmManager.INTERVAL_DAY);

		editor.putLong("service.update.period", 300000);

		//collection
		editor.putLong("collection.location.tolerance.time", 300000);
		editor.putInt("collection.location.tolerance.distance", 500);
		
		editor.putInt("collection.place.num_address_prefix_words", 3);
		
		editor.putLong("collection.call.initial_history.period", 6 * AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("collection.store_cxt.enabled", false);
		
		//TODO history enable setting
		
		//context
//		editor.putInt("context.bhv.duration_user_bhv.cache_size", 100);
		
		//report
		editor.putString("email.sender.addr", "davidahn412@gmail.com");
		editor.putString("email.sender.pwd", "rnrmfepdl");
		
		editor.putString("email.receiver.addr", "andabi412@gmail.com");
		
		//matcher
		editor.putBoolean("predictor.store", true);
		
		editor.putLong("matcher.noise.time_tolerance", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 60);
		
		editor.putLong("matcher.freq.duration", AlarmManager.INTERVAL_DAY);
		editor.putInt("matcher.freq.min_num_cxt", 3);
		editor.putLong("matcher.freq.acceptance_delay", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3);

		editor.putLong("matcher.weak_time.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.weak_time.min_likelihood", 0.5f);
		editor.putFloat("matcher.weak_time.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.weak_time.min_num_cxt", 3);
		editor.putLong("matcher.weak_time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR);
		editor.putLong("matcher.weak_time.tolerance", pref.getLong("matcher.weak_time.acceptance_delay", 2 * AlarmManager.INTERVAL_HOUR) / 2);
		
		editor.putLong("matcher.strict_time.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.strict_time.min_likelihood", 0.5f);
		editor.putFloat("matcher.strict_time.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.strict_time.min_num_cxt", 3);
		editor.putLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3);
		editor.putLong("matcher.strict_time.tolerance", pref.getLong("matcher.strict_time.acceptance_delay", AlarmManager.INTERVAL_HALF_HOUR / 3) / 2);
		
		editor.putLong("matcher.place.duration", 6 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.place.min_likelihood", 0.7f);
		editor.putFloat("matcher.place.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.place.min_num_cxt", 3);
		editor.putInt("matcher.place.distance_tolerance", 2000);
		
		editor.putLong("matcher.loc.duration", 6 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.loc.min_likelihood", 0.7f);
		editor.putFloat("matcher.loc.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.loc.min_num_cxt", 3);
		editor.putInt("matcher.loc.distance_tolerance", 100);

		//view
//		editor.putBoolean("noti.view.enabled", true);
		editor.putInt("viewer.noti.max_num_ordinary", 8);
		editor.putInt("viewer.noti.max_num_favorates", 8);
		
		editor.commit();
	}
}
