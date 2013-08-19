package lab.davidahn.appshuttle.bean.cxt;

import java.util.Date;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bean.UserLoc.Validity;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class MatcherCountUnit {
	private final Date sTime;
	private final Date eTime;
	private final TimeZone timeZone;
	private final UserBhv bhv;
	private final UserLoc loc;
	private final UserLoc place;

	private MatcherCountUnit(Builder builder) {
		 sTime = builder.sTime;
		 eTime = builder.eTime;
		 timeZone = builder.timeZone;
		 bhv = builder.bhv;
		 loc = builder.loc;
		 place = builder.place;
	}
	
	public Date getStartTime() {
		return sTime;
	}

	public Date getEndTime() {
		return eTime;
	}

	public UserBhv getBhv() {
		return bhv;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	
	public UserLoc getLoc() {
		return loc;
	}

	public UserLoc getPlace() {
		return place;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(bhv.toString()).append(", ");
		msg.append("start time: ").append(sTime).append(", ");
		msg.append("end time: ").append(eTime).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append(", ");
		msg.append("lastLoc: ").append(loc).append(", ");
		msg.append("lastPlace: ").append(place);
		return msg.toString();
	}
	
	public static class Builder {
		private Date sTime = new Date();
		private Date eTime = new Date();
		private TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
		private UserBhv bhv = null;
		private UserLoc loc = new UserLoc(0,0,Validity.INVALID);
		private UserLoc place = new UserLoc(0,0,Validity.INVALID);
		
		public Builder(UserBhv bhv){
			this.bhv = bhv;
		}
		
		public MatcherCountUnit build(){
			return new MatcherCountUnit(this);
		}

		public Date getStartTime() {
			return sTime;
		}

		public Builder setStartTime(Date sTime) {
			this.sTime = sTime;
			return this;
}

		public Date getEndTime() {
			return eTime;
		}

		public Builder setEndTime(Date eTime) {
			this.eTime = eTime;
			return this;
		}

		public TimeZone getTimeZone() {
			return timeZone;
		}
		public Builder setTimeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public UserBhv getBhv() {
			return bhv;
		}
		public void setBhv(UserBhv bhv) {
			this.bhv = bhv;
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
	}
}