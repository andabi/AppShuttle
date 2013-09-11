package lab.davidahn.appshuttle.context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;

public class SnapshotUserCxt {
	private Date time;
	private TimeZone timeZone;
	private Map<EnvType, UserEnv> userEnvs;
	private List<UserBhv> userBhvs;

	public SnapshotUserCxt() {
		userEnvs = new HashMap<EnvType, UserEnv>();
		userBhvs = new ArrayList<UserBhv>();
	}
	
	public SnapshotUserCxt(Date time, TimeZone timeZone) {
		this.time = time;
		this.timeZone = timeZone;
		userEnvs = new HashMap<EnvType, UserEnv>();
		userBhvs = new ArrayList<UserBhv>();
	}
	
	public Date getTimeDate() {
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
	public void setUserEnvs(Map<EnvType, UserEnv> userEnvs) {
		this.userEnvs = userEnvs;
	}
	
	public List<UserBhv> getUserBhvs() {
		return userBhvs;
	}
	public void addUserBhv(UserBhv userBhv) {
		userBhvs.add(userBhv);
	}
	public void addUserBhvAll(List<UserBhv> userBhvList) {
		userBhvs.addAll(userBhvList);
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
		if((o instanceof SnapshotUserCxt) 
				&& time.equals(((SnapshotUserCxt)o).time)
				&& timeZone.equals(((SnapshotUserCxt)o).timeZone)
				&& userEnvs.equals(((SnapshotUserCxt)o).userEnvs)
				&& userBhvs.equals(((SnapshotUserCxt)o).userBhvs))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}
