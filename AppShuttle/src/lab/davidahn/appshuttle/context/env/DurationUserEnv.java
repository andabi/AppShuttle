package lab.davidahn.appshuttle.context.env;

import java.util.Date;
import java.util.TimeZone;

public class DurationUserEnv {
	private Date time;
	private long duration;
	private Date endTime;
	private TimeZone timezone;
	private EnvType envType;
	private UserEnv userEnv;

	public DurationUserEnv(Date time, Date endTime, TimeZone timezone, EnvType envType, UserEnv userEnv) {
		this.time = time;
		this.endTime = endTime;
		this.timezone = timezone;
		this.envType = envType;
		this.userEnv = userEnv;
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
		return timezone;
	}
	public void setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
	}
	public EnvType getEnvType() {
		return envType;
	}
	public void setEnvType(EnvType envType) {
		this.envType = envType;
	}
	public UserEnv getUserEnv() {
		return userEnv;
	}
	public void setUserEnv(UserEnv userEnv) {
		this.userEnv = userEnv;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("duration: ").append(duration).append(", ");
		msg.append("endTime: ").append(endTime).append(", ");
		msg.append("timezone: ").append(timezone.getID()).append(", ");
		msg.append("envType: ").append(envType.toString()).append(", ");
		msg.append("userEnv: ").append(userEnv.toString());
		return msg.toString();
	}
}
