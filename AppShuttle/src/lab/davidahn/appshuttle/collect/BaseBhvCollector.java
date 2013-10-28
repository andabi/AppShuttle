package lab.davidahn.appshuttle.collect;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import android.content.SharedPreferences;

public class BaseBhvCollector implements BhvCollector {
	protected AppShuttleApplication _appShuttleContext;
	protected SharedPreferences _preferenceSettings;

	public BaseBhvCollector(){
		_appShuttleContext = AppShuttleApplication.getContext();
		_preferenceSettings = _appShuttleContext.getPreferenceSettings();
//		preferenceSettings = ((AppShuttleApplication)cxt.getApplicationContext()).getPreferenceSettings();
	}

	public List<BaseUserBhv> collect() {
		return Collections.emptyList();
	}
	
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public List<DurationUserBhv> extractDurationUserBhv(Date currTime, TimeZone currTimezone, List<BaseUserBhv> userBhvList) {
		return Collections.emptyList();
	}
	
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}
}
