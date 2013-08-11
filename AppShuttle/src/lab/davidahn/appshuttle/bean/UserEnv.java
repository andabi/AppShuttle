package lab.davidahn.appshuttle.bean;

import java.util.Date;
import java.util.TimeZone;

public class UserEnv {
	private Date time;
	private TimeZone timeZone;
	private UserLoc loc;
	private UserLoc place;

	public UserEnv(){
	}
	
	public UserEnv(Date time, TimeZone timeZone, UserLoc loc, UserLoc place) {
		this.time = time;
		this.timeZone = timeZone;
		this.loc = loc;
		this.place = place;
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
	
	public UserLoc getLoc() {
		return loc;
	}
	public void setLoc(UserLoc loc) {
		this.loc = loc;
	}
	
	public UserLoc getPlace() {
		return place;
	}

	public void setPlace(UserLoc place) {
		this.place = place;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(time).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append(", ");
		msg.append(loc.toString()).append(", ");
		msg.append(place.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof UserEnv) 
				&& time.equals(((UserEnv)o).time)
				&& timeZone.equals(((UserEnv)o).timeZone)
				&& loc.equals(((UserEnv)o).loc)
				&& place.equals(((UserEnv)o).place))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}