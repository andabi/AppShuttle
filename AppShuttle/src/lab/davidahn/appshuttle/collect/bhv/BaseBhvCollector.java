package lab.davidahn.appshuttle.collect.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;

public class BaseBhvCollector implements BhvCollector {
	protected AppShuttleApplication cxt;
	protected SharedPreferences preferenceSettings;

	private Map<BaseUserBhv, DurationUserBhv.Builder> durationUserBhvBuilderMap;

	public BaseBhvCollector(){
		cxt = AppShuttleApplication.getContext();
		preferenceSettings = cxt.getPreferences();
		
//		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		durationUserBhvBuilderMap = AppShuttleApplication.durationUserBhvBuilderMap;
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
		long adjustment = preferenceSettings.getLong("collection.period", 10000) / 2;

		if(durationUserBhvBuilderMap.isEmpty()) {
			for(BaseUserBhv uBhv : userBhvList){
				durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
				, new Date(currTime.getTime() + adjustment)
				, currTimezone
				, uBhv));
			}
		} else {
			for(BaseUserBhv uBhv : userBhvList){
				if(durationUserBhvBuilderMap.containsKey(uBhv)){
					DurationUserBhv.Builder durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
					durationUserBhvBuilder.setEndTime(new Date(currTime.getTime() + adjustment)).setTimeZone(currTimezone);
				} else {
					durationUserBhvBuilderMap.put(uBhv, createDurationUserBhvBuilder(new Date(currTime.getTime() - adjustment)
					, new Date(currTime.getTime() + adjustment)
					, currTimezone
					, uBhv));
				}
			}
			for(BaseUserBhv uBhv : new HashSet<BaseUserBhv>((durationUserBhvBuilderMap.keySet()))){
				DurationUserBhv.Builder _durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
				if(currTime.getTime() - _durationUserBhvBuilder.getEndTime().getTime() 
						> preferenceSettings.getLong("collection.period", 10000) * 1.5){
					res.add(_durationUserBhvBuilder.build());
					durationUserBhvBuilderMap.remove(uBhv);
				}
			}
		}
		return res;
	}
	
	@Override
	public List<DurationUserBhv> postExtractDurationUserBhv(Date currTimeDate, TimeZone currTimeZone) {
		List<DurationUserBhv> res = new ArrayList<DurationUserBhv>();

		for(BaseUserBhv uBhv : durationUserBhvBuilderMap.keySet()){
			DurationUserBhv.Builder durationUserBhvBuilder = durationUserBhvBuilderMap.get(uBhv);
			if(currTimeDate.getTime() - durationUserBhvBuilder.getEndTime().getTime() 
					> preferenceSettings.getLong("collection.period", 10000) * 1.5){
				res.add(durationUserBhvBuilder.build());
			}
		}
		
		durationUserBhvBuilderMap = new HashMap<BaseUserBhv, DurationUserBhv.Builder>();
		
		return res;
	}
	
	private DurationUserBhv.Builder createDurationUserBhvBuilder(Date time, Date endTime, TimeZone currTimeZone, BaseUserBhv bhv) {
		return new DurationUserBhv.Builder()
		.setTime(time)
		.setEndTime(endTime)
		.setTimeZone(currTimeZone)
		.setBhv(bhv);
	}
}
