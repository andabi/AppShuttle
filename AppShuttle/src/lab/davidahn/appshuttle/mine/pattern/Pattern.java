package lab.davidahn.appshuttle.mine.pattern;

import java.util.Date;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.env.Area;

public class Pattern {
	private Date sTime;
	private Date eTime;
	private TimeZone timeZone;
	private Area area;
	private UserBhv bhv;
	
	public Pattern(Date sTime, Date eTime, TimeZone timeZone, Area area,
			UserBhv bhv) {
		super();
		this.sTime = sTime;
		this.eTime = eTime;
		this.timeZone = timeZone;
		this.area = area;
		this.bhv = bhv;
	}
	
	public Date getsTime() {
		return sTime;
	}
	public void setsTime(Date sTime) {
		this.sTime = sTime;
	}
	public Date geteTime() {
		return eTime;
	}
	public void seteTime(Date eTime) {
		this.eTime = eTime;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	public Area getArea() {
		return area;
	}
	public void setArea(Area area) {
		this.area = area;
	}
	public UserBhv getBhv() {
		return bhv;
	}
	public void setBhv(UserBhv bhv) {
		this.bhv = bhv;
	}
	
	
}