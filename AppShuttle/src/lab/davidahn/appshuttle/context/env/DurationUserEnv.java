package lab.davidahn.appshuttle.context.env;

import java.util.Date;
import java.util.TimeZone;

public class DurationUserEnv {
	private Date time;
	private long duration;
	private Date endTime;
	private TimeZone timeZone;
	private EnvType envType;
	private UserEnv usrEnv;

	public DurationUserEnv(Builder builder) {
		this.time = builder.time;
		this.endTime = builder.endTime;
		this.timeZone = builder.timeZone;
		this.usrEnv = builder.usrEnv;
		envType = usrEnv.getEnvType();
		duration = endTime.getTime() - time.getTime();
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timezone) {
		this.timeZone = timezone;
	}
	public EnvType getEnvType() {
		return envType;
	}
	public void setEnvType(EnvType envType) {
		this.envType = envType;
	}
	public UserEnv getUserEnv() {
		return usrEnv;
	}
	public void setUserEnv(UserEnv userEnv) {
		this.usrEnv = userEnv;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("duration: ").append(duration).append(", ");
		msg.append("endTime: ").append(endTime).append(", ");
		msg.append("timezone: ").append(timeZone.getID()).append(", ");
		msg.append("envType: ").append(envType.toString()).append(", ");
		msg.append("userEnv: ").append(usrEnv.toString());
		return msg.toString();
	}
	@Override
	public boolean equals(Object o) {
		if((o instanceof DurationUserEnv) 
				&& time.equals(((DurationUserEnv)o).time)
				&& timeZone.equals(((DurationUserEnv)o).timeZone)
				&& envType.equals(((DurationUserEnv)o).envType))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
	
	public static class Builder{
		private Date time = null;
		private Date endTime = null;
		private TimeZone timeZone = null;
		private UserEnv usrEnv = null;
		
		public Builder(){}
		
		public DurationUserEnv build(){
			return new DurationUserEnv(this);
		}
		
		public Builder setTime(Date time){
			this.time = time;
			return this;
		}
		public Builder setEndTime(Date endTime){
			this.endTime = endTime;
			return this;
		}
		public Builder setTimeZone(TimeZone timeZone){
			this.timeZone = timeZone;
			return this;
		}
		public Builder setUserEnv(UserEnv usrEnv){
			this.usrEnv = usrEnv;
			return this;
		}

		public UserEnv getUserEnv() {
			return usrEnv;
		}
	}
}
