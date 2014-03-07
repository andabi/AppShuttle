package lab.davidahn.appshuttle.collect.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;

public class BaseBhvCollector implements BhvCollector {
	protected AppShuttleApplication cxt;
	protected SharedPreferences preferenceSettings;

	public BaseBhvCollector(){
		cxt = AppShuttleApplication.getContext();
		preferenceSettings = cxt.getPreferences();
	}

	@Override
	public List<UserBhv> collect() {
		return Collections.emptyList();
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(long currTime, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public List<DurationUserBhv> extractDurationUserBhv(long currTime, TimeZone currTimezone, List<UserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

		if(AppShuttleApplication.durationUserBhvBuilderMap.isEmpty()) {
			for(UserBhv uBhv : userBhvList){
				AppShuttleApplication.durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(currTime
				, currTime
				, currTimezone
				, uBhv));
			}
		} else {
			for(UserBhv uBhv : userBhvList){
				if(AppShuttleApplication.durationUserBhvBuilderMap.containsKey(uBhv)){
					DurationUserBhv.Builder durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
					durationUserBhvBuilder.setEndTime(currTime).setTimeZone(currTimezone);
				} else {
					AppShuttleApplication.durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(currTime
					, currTime
					, currTimezone
					, uBhv));
				}
			}
			for(UserBhv uBhv : new HashSet<UserBhv>((AppShuttleApplication.durationUserBhvBuilderMap.keySet()))){
				DurationUserBhv.Builder _durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
				if(currTime - _durationUserBhvBuilder.getEndTime()
						> preferenceSettings.getLong("collection.bhv.period", 60000) * 1.5){
					res.add(_durationUserBhvBuilder.build());
					AppShuttleApplication.durationUserBhvBuilderMap.remove(uBhv);
				}
			}
		}
		return res;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(long currTime, TimeZone currTimeZone) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

		for(UserBhv uBhv : AppShuttleApplication.durationUserBhvBuilderMap.keySet()){
			DurationUserBhv.Builder durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
			if(currTime - durationUserBhvBuilder.getEndTime() 
					> preferenceSettings.getLong("collection.bhv.period", 60000) * 1.5){
				res.add(durationUserBhvBuilder.build());
			}
		}
		
		AppShuttleApplication.durationUserBhvBuilderMap = new HashMap<UserBhv, DurationUserBhv.Builder>();
		
		return res;
	}
	
	public DurationUserBhv.Builder createDurationUserBhvBuilder(long time, long endTime, TimeZone currTimeZone, UserBhv bhv) {
		long adjustment = preferenceSettings.getLong("collection.bhv.period", 60000) / 2;

		return new DurationUserBhv.Builder()
		.setTime(time - adjustment)
		.setEndTime(endTime + adjustment)
		.setTimeZone(currTimeZone)
		.setBhv(bhv);
	}
	
	public boolean isTrackedForDurationUserBhv(UserBhv uBhv){
		if(AppShuttleApplication.durationUserBhvBuilderMap.containsKey(uBhv))
			return true;
		else
			return false;
	}
}
