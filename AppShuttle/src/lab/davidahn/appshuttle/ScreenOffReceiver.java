package lab.davidahn.appshuttle;

import lab.davidahn.appshuttle.view.NotiViewService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenOffReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
//		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		alarmManager.cancel(notiViewOperation);
//		stopService(new Intent(AppShuttleService.this, NotiViewService.class));
	}
}