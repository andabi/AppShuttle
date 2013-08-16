package lab.davidahn.appshuttle.bean.cxt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bean.env.EnvType;
import lab.davidahn.appshuttle.bean.env.TimeUserEnv;
import lab.davidahn.appshuttle.bean.env.UserEnv;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class UserCxt {
	private Date time;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private List<UserBhv> userBhvs;

	public UserCxt() {
		userEnvs = new HashMap<EnvType, UserEnv>();
		userBhvs = new ArrayList<UserBhv>();
	}
	
	public UserCxt(Date time, TimeZone timeZone) {
		this.time = time;
		this.timeZone = timeZone;
		userEnvs = new HashMap<EnvType, UserEnv>();
		userBhvs = new ArrayList<UserBhv>();
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	public UserEnv getUserEnv(EnvType envType) {
		return userEnvs.get(envType);
	}
	public void addUserEnv(EnvType envType, UserEnv userEnv) {
		userEnvs.put(envType, userEnv);
	}
	public Map<EnvType, UserEnv> getUserEnvs() {
		return userEnvs;
	}
	
	public List<UserBhv> getUserBhvs() {
		return userBhvs;
	}
	public void addUserBhv(UserBhv userBhv) {
		userBhvs.add(userBhv);
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append(", ");
		msg.append("userEnvs: ").append(userEnvs.toString()).append(", ");
		msg.append("userBhvs: ").append(userBhvs.toString());
		return msg.toString();
	}
	@Override
	public boolean equals(Object o) {
		if((o instanceof TimeUserEnv) 
				&& time.equals(((UserCxt)o).time)
				&& timeZone.equals(((UserCxt)o).timeZone)
				&& userEnvs.equals(((UserCxt)o).userEnvs)
				&& userBhvs.equals(((UserCxt)o).userBhvs))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}
