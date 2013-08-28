package lab.davidahn.appshuttle.context.env;

import java.util.Date;
import java.util.TimeZone;

@Deprecated
public class ChangeUserEnv {
	private Date time;
	private TimeZone timezone;
	private EnvType envType;
	private UserEnv fromUserEnv;
	private UserEnv toUserEnv;
	
	public ChangeUserEnv(Date time, TimeZone timezone, EnvType envType, UserEnv fromUserEnv,
			UserEnv toUserEnv) {
		super();
		this.time = time;
		this.timezone = timezone;
		this.envType = envType;
		this.fromUserEnv = fromUserEnv;
		this.toUserEnv = toUserEnv;
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
		return fromUserEnv;
	}
	public void setFromUserEnv(UserEnv fromUserEnv) {
		this.fromUserEnv = fromUserEnv;
	}
	public UserEnv getToUserEnv() {
		return toUserEnv;
	}
	public void setToUserEnv(UserEnv toUserEnv) {
		this.toUserEnv = toUserEnv;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("timeZone: ").append(timezone.getID()).append(", ");
		msg.append("envType: ").append(envType.toString()).append(", ");
		msg.append("fromUserEnv: ").append(fromUserEnv.toString()).append(", ");
		msg.append("toUserEnv: ").append(toUserEnv.toString());
		return msg.toString();
	}
}
