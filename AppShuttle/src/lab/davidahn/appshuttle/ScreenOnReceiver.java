package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOnReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if(Settings.preferenceSettings.getBoolean("service.view.enabled", true)){
			Intent notiViewIntent = new Intent().setAction("lab.davidahn.appshuttle.VIEW");
			PendingIntent notiViewOperation = PendingIntent.getBroadcast(context, 0, notiViewIntent, 0);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), Settings.preferenceSettings.getLong("service.view.period", 30000), notiViewOperation);
		}
	}
}