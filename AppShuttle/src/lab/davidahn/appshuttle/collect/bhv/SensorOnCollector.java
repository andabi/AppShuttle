package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.Context;
import android.net.wifi.WifiManager;

public class SensorOnCollector extends BaseBhvCollector {
	private static SensorOnCollector sensorOnCollector = new SensorOnCollector();

	private SensorOnCollector() {
		super();
	}

	public static SensorOnCollector getInstance() {
		return sensorOnCollector;
	}

	public List<UserBhv> collect() {
		List<UserBhv> res = new ArrayList<UserBhv>();
		WifiManager wifi = (WifiManager)AppShuttleApplication.getContext().getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {
			res.add(create(UserBhvType.SENSOR_ON, SensorType.WIFI.name()));
		}
		return res;
	}

//	public static boolean isWifiEnabled() {
////		ConnectivityManager connManager = (ConnectivityManager) AppShuttleApplication
////				.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
////		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
////		return wifi.isAvailable();
//		WifiManager wifi = (WifiManager)AppShuttleApplication.getContext().getSystemService(Context.WIFI_SERVICE);
//		return wifi.isWifiEnabled();
//	}
}
