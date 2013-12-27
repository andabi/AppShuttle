package lab.davidahn.appshuttle.collect.bhv;

import java.util.Date;
import java.util.TimeZone;

public class DurationUserBhv implements UserBhv {
	private final Date timeDate;
	private final long duration;
	private final Date endTimeDate;
	private final TimeZone timeZone;
	private final UserBhv uBhv;

	private DurationUserBhv(Builder builder){
		timeDate = builder.timeDate;
		duration = builder.duration;
		endTimeDate = builder.endTimeDate;
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

	public Date getTimeDate() {
		return timeDate;
	}
	public long getDuration() {
		return duration;
	}
	public Date getEndTimeDate() {
		return endTimeDate;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("start time: ").append(timeDate).append(", ");
		msg.append("duration: ").append(duration).append(", ");
		msg.append("end time: ").append(endTimeDate).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append("\n");
		msg.append(uBhv.toString()).append(", ");
		return msg.toString();
	}
	
	public static class Builder {
		private Date timeDate = null;
		private long duration = 0;
		private Date endTimeDate = null;
		private TimeZone timeZone = null;
		private UserBhv uBhv = null;

		public Builder(){}
		
		public DurationUserBhv build(){
			if(timeDate != null && endTimeDate != null)
				duration = endTimeDate.getTime() - timeDate.getTime();
			return new DurationUserBhv(this);
		}
		
		public Date getEndTime() {
			return endTimeDate;
		}
		
		public Builder setTime(Date sTime) {
			timeDate = sTime;
			return this;
		}
		
		public Builder setDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public Builder setEndTime(Date eTime) {
			endTimeDate = eTime;
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