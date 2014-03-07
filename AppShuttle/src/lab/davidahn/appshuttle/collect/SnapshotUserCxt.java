package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;

public class SnapshotUserCxt {
	private Date _time;
	private TimeZone _timeZone;
	private Map<EnvType, UserEnv> _userEnvs;
	private List<UserBhv> _userBhvs;

	public SnapshotUserCxt() {
		_userEnvs = new EnumMap<EnvType, UserEnv>(EnvType.class);
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
	public Map<EnvType, UserEnv> getUserEnvs() {
		return _userEnvs;
	}
	public List<UserBhv> getUserBhvs() {
		return _userBhvs;
	}
//	public void setUserBhvs(List<UserBhv> userBhvList) {
//		_userBhvs = userBhvList;
//	}
//	public void setUserEnvs(Map<EnvType, UserEnv> userEnvs) {
//		_userEnvs = userEnvs;
//	}
	public void updateUserEnv(EnvType envType, UserEnv userEnv) {
		_userEnvs.put(envType, userEnv);
	}
	public void updateUserEnv(Map<EnvType, UserEnv> userEnvs) {
		_userEnvs.putAll(userEnvs);
	}
	public void addUserBhvs(List<UserBhv> userBhvList) {
		_userBhvs.addAll(userBhvList);
	}
	public void clearUserBhvs(){
		_userBhvs.clear();
	}
	
	public UserBhv getTopUserBhv(){
		if (_userBhvs.isEmpty())
			return null;
		
		return _userBhvs.get(0);
	}
	
	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("<Time>\n").append(_time.toString()).append("\n\n");
//		msg.append("timeZone: ").append(_timeZone.getID()).append(", ");
		msg.append("<Environment>\n").append(_userEnvs.toString()).append("\n\n");
		msg.append("<Behavior>\n").append(_userBhvs.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof SnapshotUserCxt) 
				&& _time.equals(((SnapshotUserCxt)o)._time)
				&& _timeZone.equals(((SnapshotUserCxt)o)._timeZone))
//				&& _userEnvs.equals(((SnapshotUserCxt)o)._userEnvs)
//				&& _userBhvs.equals(((SnapshotUserCxt)o)._userBhvs))
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return _time.hashCode() ^ _timeZone.hashCode();
	}
}
