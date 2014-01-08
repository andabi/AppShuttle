package lab.davidahn.appshuttle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getData().getSchemeSpecificPart().equals(context.getPackageName())){
			context.startService(new Intent(context, AppShuttleMainService.class));
		}
	}
}