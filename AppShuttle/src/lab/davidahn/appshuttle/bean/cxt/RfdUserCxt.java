package lab.davidahn.appshuttle.bean.cxt;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import lab.davidahn.appshuttle.bean.UserLoc;
import lab.davidahn.appshuttle.bhv.UserBhv;

public class RfdUserCxt {
//	private final int contextId;
	private final Date sTime;
	private final Date eTime;
	private final TimeZone timeZone;
	private final UserBhv bhv;
	private final Map<Date, UserLoc> locs;
	private final UserLoc lastLoc;
	private final Map<Date, UserLoc> places;
	private final UserLoc lastPlace;

	private RfdUserCxt(Builder builder){
//		contextId = builder.contextId;
		sTime = builder.sTime;
		eTime = builder.eTime;
		timeZone = builder.timeZone;
		bhv = builder.bhv;
		locs = builder.locs;
		lastLoc = builder.lastLoc;
		places = builder.places;
		lastPlace = builder.lastPlace;
	}
	
//	public int getContextId() {
//		return contextId;
//	}
	public UserBhv getBhv() {
		return bhv;
	}
	public Date getStartTime() {
		return sTime;
	}
	public Date getEndTime() {
		return eTime;
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
		msg.append("timeZone: ").append(timeZone.getID()).append("\n");
		msg.append("locations: ").append(locs.toString()).append("\n");
		msg.append("places: ").append(places.toString());
		return msg.toString();
	}
	
	public static class Builder {
//		private int contextId = 0;
		private Date sTime = null;
		private Date eTime = null;
		private TimeZone timeZone = null;
		private UserBhv bhv = null;
		private Map<Date, UserLoc> locs = new TreeMap<Date, UserLoc>();
		private UserLoc lastLoc = null;
		private Map<Date, UserLoc> places = new TreeMap<Date, UserLoc>();
		private UserLoc lastPlace = null;

		public Builder(){}
		
		public RfdUserCxt build(){
			return new RfdUserCxt(this);
		}
		
		public Date getEndTime() {
			return eTime;
		}
		
		public Builder setStartTime(Date sTime) {
			this.sTime = sTime;
			return this;
		}

		public Builder setEndTime(Date eTime) {
			this.eTime = eTime;
			return this;
		}

		public Builder setTimeZone(TimeZone timeZone) {
			this.timeZone = timeZone;
			return this;
		}

		public Builder setBhv(UserBhv bhv) {
			this.bhv = bhv;
			return this;
		}
		
//		public Builder setContextId(int contextId) {
//			this.contextId = contextId;
//			return this;
//		}
		public Builder setLocs(Map<Date, UserLoc> locs){
			this.locs = locs;
			return this;
		}
		public Builder setPlaces(Map<Date, UserLoc> places){
			this.places = places;
			return this;
		}
		public Builder appendLoc(UserLoc uLoc, Date time) {
			if(lastLoc == null) {
				lastLoc = uLoc;
				locs.put(time, uLoc);
			} else {
				if(uLoc.equals(lastLoc))
					;
				else { 
					lastLoc = uLoc;
					locs.put(time, uLoc);
				}
			}
			return this;
		}
		public Builder appendPlace(UserLoc place, Date time) {
			if(lastPlace == null) {
				lastPlace = place;
				places.put(time, place);
			} else {
				if(place.equals(lastPlace)) 
					;
				else {
					lastPlace = place;
					places.put(time, place);
				}
			}
			return this;
		}
	}
}