package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.content.SharedPreferences;

public class AppShuttlePreferences {
	public static void setDefaultPreferences() {
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		SharedPreferences.Editor editor = pref.edit();
		
		//general
		editor.putBoolean("mode.debug", false);
		editor.putString("database.name", "AppShuttle.db");

		//collection
		editor.putBoolean("collection.enabled", true);
		editor.putLong("collection.period", 30000);
		
		editor.putLong("collection.common.auto_store.max_duration", AlarmManager.INTERVAL_HOUR);
		
		editor.putLong("collection.location.tolerance.time", 25000);
		editor.putInt("collection.location.tolerance.distance", 500);
		editor.putInt("collection.place.num_address_prefix_words", 6);
		
		editor.putLong("collection.app.pre.depreciation", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5);
		editor.putLong("collection.call.pre.period", 6 * AlarmManager.INTERVAL_DAY);
		
		editor.putBoolean("collection.store_snapshot_cxt.enabled", false);
		
		//compaction
		editor.putBoolean("compaction.enabled", true);
		editor.putLong("compaction.period", AlarmManager.INTERVAL_DAY);
		editor.putLong("compaction.expiration", 35 * AlarmManager.INTERVAL_DAY);
		
		//report
		editor.putString("report.email.sender_addr", "appshuttle2@gmail.com");
		editor.putString("report.email.sender_pwd", "appshuttle2@");
		editor.putString("report.email.receiver_addr", "andabi412@gmail.com");
		
		//predictor
		editor.putBoolean("predictor.store", false);
		editor.putLong("predictor.period", 120000);
		editor.putLong("predictor.delay_ignorance", 60000);
		
		editor.putLong("matcher.recent.frequently.duration", AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putInt("matcher.recent.frequently.min_num_related_history", 3);
		
		editor.putLong("matcher.recent.instantly.duration", AlarmManager.INTERVAL_HOUR / 60);
		editor.putLong("matcher.recent.instantly.acceptance_delay", 0);
		editor.putInt("matcher.recent.instantly.min_num_related_history", 1);

		editor.putLong("matcher.time.daily.duration", 4 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily.min_likelihood", Float.MIN_VALUE);
		editor.putFloat("matcher.time.daily.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.time.daily.min_num_related_history", 3);
		editor.putLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HOUR);
		
		editor.putLong("matcher.time.daily_weekday.duration", 7 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily_weekday.min_likelihood", 0.5f);
		editor.putFloat("matcher.time.daily_weekday.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.time.daily_weekday.min_num_related_history", 3);
		editor.putLong("matcher.time.daily_weekday.tolerance", 3 * AlarmManager.INTERVAL_HALF_HOUR);

		editor.putLong("matcher.time.daily_weekend.duration", 21 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily_weekend.min_likelihood", 0.5f);
		editor.putFloat("matcher.time.daily_weekend.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.time.daily_weekend.min_num_related_history", 3);
		editor.putLong("matcher.time.daily_weekend.tolerance", 2 * AlarmManager.INTERVAL_HOUR);

//		editor.putLong("matcher.position.place.duration", 5 * AlarmManager.INTERVAL_DAY);
//		editor.putLong("matcher.position.place.acceptance_delay", AlarmManager.INTERVAL_HOUR);
//		editor.putFloat("matcher.position.place.min_likelihood", 0.3f);
//		editor.putFloat("matcher.position.place.min_inverse_entropy", 0.1f);
//		editor.putInt("matcher.position.place.min_num_history", 3);

		editor.putLong("matcher.position.move.duration", 7 * AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.position.move.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.position.move.min_likelihood", 0.3f);
		editor.putInt("matcher.position.move.min_num_related_history", 3);

		editor.putLong("matcher.position.loc.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.position.loc.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.position.loc.min_likelihood", 0.3f);
		editor.putFloat("matcher.position.loc.min_inverse_entropy", 0.1f);
		editor.putInt("matcher.position.loc.min_num_history", 3);
		editor.putInt("matcher.position.loc.tolerance_in_meter", 50);
		
		//view
		editor.putInt("viewer.noti.max_num", 24);
		editor.putInt("viewer.noti.proper_num_favorite", 6);
//		editor.putInt("viewer.noti.max_num_favorite", 24);
//		editor.putInt("viewer.min_num_present_bhv", 6);
		
		editor.commit();
	}

	public static boolean isSleepMode() {
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		if(pref.getBoolean("settings_pref_sleep_mode_key", false))
			return true;
		else
			return false;
	}
	
	public static boolean isSystemAreaIconHidden(){
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		if(pref.getBoolean("settings_pref_system_area_icon_hide_key", false))
			return true;
		else
			return false;
	}
	
//	public boolean isHidden(){
//	SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
//	if(pref.getBoolean("settings_pref_noti_view_hide_key", false))
//		return true;
//	else
//		return false;
//}

}
