package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.content.Context;
import android.content.SharedPreferences;

public class BaseBhvCollector implements BhvCollector {
	protected SharedPreferences preferenceSettings;

	public BaseBhvCollector(Context cxt){
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
	}

	public List<UserBhv> collect() {
		return Collections.emptyList();
	}
	
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public List<DurationUserBhv> extractDurationUserBhv(Date currTime, TimeZone currTimezone, List<UserBhv> userBhvList) {
		return Collections.emptyList();
	}
	
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}
}
