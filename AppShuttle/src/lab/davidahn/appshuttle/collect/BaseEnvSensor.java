package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.content.Context;
import android.content.SharedPreferences;

public class BaseEnvSensor implements EnvSensor {
	protected SharedPreferences preferenceSettings;

	public BaseEnvSensor(Context cxt){
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
	}
	
	public UserEnv sense() {
		return null;
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return null;
	}
	
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return null;
	}
}
