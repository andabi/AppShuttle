package lab.davidahn.appshuttle.collect.bhv;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class SensorOnCollector extends BaseBhvCollector {
	ConnectivityManager connManager = (ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
	
	private static SensorOnCollector sensorOnCollector = new SensorOnCollector();
	private SensorOnCollector(){
		super();
	}
	public static SensorOnCollector getInstance(){
		return sensorOnCollector;
	}
	
	public List<BaseUserBhv> collect() {
		List<BaseUserBhv> res = new ArrayList<BaseUserBhv>();
		if(isWifiConnected()){
			Log.d("test", "wifi on");
			res.add(create(UserBhvType.SENSOR_ON, SensorType.WIFI.name()));
		}
		return res;
	}
	
	private boolean isWifiConnected() {
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi.isConnected();
	}
}
