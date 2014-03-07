package lab.davidahn.appshuttle.collect.bhv;

import java.util.TimeZone;

import android.content.Intent;

public class DurationUserBhv implements UserBhv {
	private final long time;
	private final long duration;
	private final long endTime;
	private final TimeZone timeZone;
	private final UserBhv uBhv;

	private DurationUserBhv(Builder builder){
		time = builder.time;
		duration = builder.duration;
		endTime = builder.endTime;
		timeZone = builder.timeZone;
		uBhv = builder.uBhv;
	}
	@Override
	public UserBhvType getBhvType() {
		return uBhv.getBhvType();
	}
	@Override
	public void setBhvType(UserBhvType bhvType) {
		uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		uBhv.setBhvName(bhvName);
	}
	@Override
	public Object getMeta(String key) {
		return uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		uBhv.setMeta(key, val);
	}

	public long getTime() {
		return time;
	}
	public long getDuration() {
		return duration;
	}
	public long getEndTime() {
		return endTime;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public UserBhv getUserBhv() {
		return uBhv;
	}
	
	public Intent getLaunchIntent(){
		if (uBhv == null)
			return null;
		
		return uBhv.getLaunchIntent(); 
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("start time: ").append(time).append(", ");
		msg.append("duration: ").append(duration).append(", ");
		msg.append("end time: ").append(endTime).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append("\n");
		msg.append(uBhv.toString()).append(", ");
		return msg.toString();
	}
	
	public static class Builder {
		private long time = 0;
		private long duration = 0;
		private long endTime = 0;
		private TimeZone timeZone = null;
		private UserBhv uBhv = null;

		public Builder(){}
		
		public DurationUserBhv build(){
			if(time != 0 && endTime != 0)
				duration = endTime - time;
			return new DurationUserBhv(this);
		}
		
		public long getEndTime() {
			return endTime;
		}
		
		public Builder setTime(long sTime) {
			time = sTime;
			return this;
		}
		
		public Builder setDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public Builder setEndTime(long eTime) {
			endTime = eTime;
			return this;
		}

		public Builder setTimeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public Builder setBhv(UserBhv uBhv) {
			this.uBhv = uBhv;
			return this;
		}
	}
}