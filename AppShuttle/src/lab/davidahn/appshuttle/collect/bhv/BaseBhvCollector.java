package lab.davidahn.appshuttle.collect.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
	public List<BaseUserBhv> collect() {
		return Collections.emptyList();
	}
	
	@Override
	public List<DurationUserBhv> preExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public List<DurationUserBhv> extractDurationUserBhv(Date currTime, TimeZone currTimezone, List<BaseUserBhv> userBhvList) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

		if(AppShuttleApplication.durationUserBhvBuilderMap.isEmpty()) {
			for(BaseUserBhv uBhv : userBhvList){
				AppShuttleApplication.durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime())
				, new Date(currTime.getTime())
				, currTimezone
				, uBhv));
			}
		} else {
			for(BaseUserBhv uBhv : userBhvList){
				if(AppShuttleApplication.durationUserBhvBuilderMap.containsKey(uBhv)){
					DurationUserBhv.Builder durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
					durationUserBhvBuilder.setEndTime(new Date(currTime.getTime())).setTimeZone(currTimezone);
				} else {
					AppShuttleApplication.durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime())
					, new Date(currTime.getTime())
					, currTimezone
					, uBhv));
				}
			}
			for(BaseUserBhv uBhv : new HashSet<BaseUserBhv>((AppShuttleApplication.durationUserBhvBuilderMap.keySet()))){
				DurationUserBhv.Builder _durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
				if(currTime.getTime() - _durationUserBhvBuilder.getEndTime().getTime() 
						> preferenceSettings.getLong("collection.period", 10000) * 1.5){
					res.add(_durationUserBhvBuilder.build());
					AppShuttleApplication.durationUserBhvBuilderMap.remove(uBhv);
				}
			}
		}
		return res;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

		for(BaseUserBhv uBhv : AppShuttleApplication.durationUserBhvBuilderMap.keySet()){
			DurationUserBhv.Builder durationUserBhvBuilder = AppShuttleApplication.durationUserBhvBuilderMap.get(uBhv);
			if(currTimeDate.getTime() - durationUserBhvBuilder.getEndTime().getTime() 
					> preferenceSettings.getLong("collection.period", 10000) * 1.5){
				res.add(durationUserBhvBuilder.build());
			}
		}
		
		AppShuttleApplication.durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		
		return res;
	}
	
	public DurationUserBhv.Builder createDurationUserBhvBuilder(Date time, Date endTime, TimeZone currTimeZone, UserBhv bhv) {
		long adjustment = preferenceSettings.getLong("collection.period", 10000) / 2;

		return new DurationUserBhv.Builder()
		.setTime(new Date(time.getTime() - adjustment))
		.setEndTime(new Date(endTime.getTime() + adjustment))
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
