package lab.davidahn.appshuttle.context.env;

import java.util.Date;
import java.util.TimeZone;

public class ChangedUserEnv {
	private Date time;
	private TimeZone timezone;
	private EnvType envType;
	private UserEnv from;
	private UserEnv to;
	
	public ChangedUserEnv(Date time, TimeZone timezone, EnvType envType, UserEnv fromUserEnv,
			UserEnv toUserEnv) {
		super();
		this.time = time;
		this.timezone = timezone;
		this.envType = envType;
		this.from = fromUserEnv;
		this.to = toUserEnv;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
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
	public UserEnv getFromUserEnv() {
		return from;
	}
	public void setFromUserEnv(UserEnv fromUserEnv) {
		this.from = fromUserEnv;
	}
	public UserEnv getToUserEnv() {
		return to;
	}
	public void setToUserEnv(UserEnv toUserEnv) {
		this.to = toUserEnv;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("timeZone: ").append(timezone.getID()).append(", ");
		msg.append("envType: ").append(envType.toString()).append(", ");
		msg.append("from: ").append(from.toString()).append(", ");
		msg.append("to: ").append(to.toString());
		return msg.toString();
	}
}
