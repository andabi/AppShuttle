package lab.davidahn.appshuttle.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.bean.UserLoc.Validity;
import lab.davidahn.appshuttle.bhv.UserBhv;
import lab.davidahn.appshuttle.exception.InvalidLocationException;

public class MergedRfdUserCxt {
	private final Date sTime;
	private final Date eTime;
	private final TimeZone timeZone;
	private final UserBhv bhv;
	private final Map<Date, UserLoc> locs ;
	private final UserLoc lastLoc;
	private final Map<Date, UserLoc> places;
	private final UserLoc lastPlace;

	private MergedRfdUserCxt(Builder builder) {
		 sTime = builder.sTime;
		 eTime = builder.eTime;
		 timeZone = builder.timeZone;
		 bhv = builder.bhv;
		 locs = builder.locs;
		 lastLoc = builder.lastLoc;
		 places = builder.places;
		 lastPlace = builder.lastPlace;
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
	public Map<Date, UserLoc> getLocs() {
		return locs;
	}
	public Map<Date, UserLoc> getPlaces() {
		return places;
	}
	
	public UserLoc getLastLoc() {
		return lastLoc;
	}

	public UserLoc getLastPlace() {
		return lastPlace;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(bhv.toString()).append(", ");
		msg.append("start time: ").append(sTime).append(", ");
		msg.append("end time: ").append(eTime).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append(", ");
		msg.append(locs.toString());
		msg.append(places.toString());
		return msg.toString();
	}
	
	public static class Builder {
		private Date sTime = new Date();
		private Date eTime = new Date();
		private TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
		private UserBhv bhv = null;
		private Map<Date, UserLoc> locs = new HashMap<Date, UserLoc>();
		private UserLoc lastLoc = new UserLoc(0,0,Validity.INVALID);
		private Map<Date, UserLoc> places = new HashMap<Date, UserLoc>();
		private UserLoc lastPlace = new UserLoc(0,0,Validity.INVALID);
		
		public Builder(UserBhv bhv){
			this.bhv = bhv;
		}
		
		public MergedRfdUserCxt build(){
			return new MergedRfdUserCxt(this);
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

		public Builder setTimeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public UserBhv getBhv() {
			return bhv;
		}
		public TimeZone getTimeZone() {
			return timeZone;
		}
		public Map<Date, UserLoc> getLocs() {
			return locs;
		}
		public Map<Date, UserLoc> getPlaces() {
			return places;
		}
		public Builder setLocs(Map<Date, UserLoc> locs){
			this.locs = locs;
			return this;
		}
		public Builder setPlaces(Map<Date, UserLoc> places){
			this.places = places;
			return this;
		}
		public Builder appendLoc(UserLoc uLoc, Date time){
			try {
				if(!uLoc.isValid()) {
					throw new InvalidLocationException();
				}
				if(lastPlace == null) {
					lastPlace = uLoc;
					locs.put(time, uLoc);
				} else {
					if(uLoc.equals(lastPlace)) ;
					else 
						lastPlace = uLoc;
						locs.put(time, uLoc);
				}
			} catch (InvalidLocationException e) {
				;
			}
			return this;
		}
//		public void appendLocs(Map<Date, UserLoc> locs){
//			locs.putAll(locs);
//		}
		public Builder appendPlace(UserLoc place, Date time){
			try {
				if(!place.isValid()) {
					throw new InvalidLocationException();
				}
				if(lastPlace == null) {
					lastPlace = place;
					places.put(time, place);
				} else {
					if(place.equals(lastPlace)) ;
					else 
						lastPlace = place;
						places.put(time, place);
				}
			} catch (InvalidLocationException e) {
				;
			}
			return this;
		}
	}
}