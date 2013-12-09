package lab.davidahn.appshuttle;

import lab.davidahn.appshuttle.collect.bhv.UnregisterBhvService;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PackageRemovedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!intent.getExtras().getBoolean((Intent.EXTRA_REPLACING))) {
			Uri uri = intent.getData();
			String name = uri != null ? uri.getSchemeSpecificPart() : null;
			if(name != null) {
				Intent updateBhvIntent = new Intent(context, UnregisterBhvService.class);
				updateBhvIntent.putExtra("bhv_type", UserBhvType.APP);
				updateBhvIntent.putExtra("bhv_name", name);
				context.startService(updateBhvIntent);
			}
		}
	}
}