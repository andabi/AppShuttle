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
	private Date _time;
	private TimeZone _timeZone;
	private Map<EnvType, UserEnv> _userEnvs;
	private List<UserBhv> _userBhvs;

	public SnapshotUserCxt() {
		_userEnvs = new HashMap<EnvType, UserEnv>();
		_userBhvs = new ArrayList<UserBhv>();
	}
	
	public SnapshotUserCxt(Date time, TimeZone timeZone) {
		_time = time;
		_timeZone = timeZone;
		_userEnvs = new HashMap<EnvType, UserEnv>();
		_userBhvs = new ArrayList<UserBhv>();
	}
	
	public Date getTimeDate() {
		return _time;
	}
	public void setTime(Date time) {
		_time = time;
	}
	public TimeZone getTimeZone() {
		return _timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		_timeZone = timeZone;
	}
	
	public UserEnv getUserEnv(EnvType envType) {
		return _userEnvs.get(envType);
	}
	public void addUserEnv(EnvType envType, UserEnv userEnv) {
		_userEnvs.put(envType, userEnv);
	}
	public Map<EnvType, UserEnv> getUserEnvs() {
		return _userEnvs;
	}
	public void setUserEnvs(Map<EnvType, UserEnv> userEnvs) {
		_userEnvs = userEnvs;
	}
	
	public List<UserBhv> getUserBhvs() {
		return _userBhvs;
	}
	public void addUserBhv(UserBhv userBhv) {
		_userBhvs.add(userBhv);
	}
	public void addUserBhvAll(List<UserBhv> userBhvList) {
		_userBhvs.addAll(userBhvList);
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(_time).append(", ");
		msg.append("timeZone: ").append(_timeZone.getID()).append(", ");
		msg.append("userEnvs: ").append(_userEnvs.toString()).append(", ");
		msg.append("userBhvs: ").append(_userBhvs.toString());
		return msg.toString();
	}
	@Override
	public boolean equals(Object o) {
		if((o instanceof SnapshotUserCxt) 
				&& _time.equals(((SnapshotUserCxt)o)._time)
				&& _timeZone.equals(((SnapshotUserCxt)o)._timeZone)
				&& _userEnvs.equals(((SnapshotUserCxt)o)._userEnvs)
				&& _userBhvs.equals(((SnapshotUserCxt)o)._userBhvs))
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}
