package lab.davidahn.appshuttle.context.bhv;

import java.util.Date;
import java.util.TimeZone;

public class DurationUserBhv {
	private final Date time;
	private final long duration;
	private final Date endTime;
	private final TimeZone timeZone;
	private final UserBhv bhv;
//	private final Map<EnvType, UserEnv> initialUEnvs;
//	private final List<ChangedUserEnv> changedUEnvs;
//	private final Map<EnvType, UserEnv> lastUEnvs;

//	private final Map<Date, UserLoc> locs;
//	private final UserLoc lastLoc;
//	private final Map<Date, UserLoc> places;
//	private final UserLoc lastPlace;

	private DurationUserBhv(Builder builder){
		time = builder.time;
		duration = builder.duration;
		endTime = builder.endTime;
		timeZone = builder.timeZone;
		bhv = builder.bhv;
//		initialUEnvs = builder.initialUEnvs;
//		changedUEnvs = builder.changedUEnvs;
//		lastUEnvs = builder.lastUEnvs;
//		locs = builder.locs;
//		lastLoc = builder.lastLoc;
//		places = builder.places;
//		lastPlace = builder.lastPlace;
	}
	
	public Date getTimeDate() {
		return time;
	}
	public long getDuration() {
		return duration;
	}
	public Date getEndTime() {
		return endTime;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
//	public Map<EnvType, UserEnv> getInitialUEnvs() {
//		return initialUEnvs;
//	}
//	public List<ChangedUserEnv> getChangedUEnvs() {
//		return changedUEnvs;
//	}
//	public Map<EnvType, UserEnv> getLastUEnvs() {
//		return lastUEnvs;
//	}

	//	public Map<Date, UserLoc> getLocs() {
//		return locs;
//	}
//	public Map<Date, UserLoc> getPlaces() {
//		return places;
//	}
//	
//	public UserLoc getLastLoc() {
//		return lastLoc;
//	}
//
//	public UserLoc getLastPlace() {
//		return lastPlace;
//	}
	public UserBhv getBhv() {
		return bhv;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("start time: ").append(time).append(", ");
		msg.append("duration: ").append(duration).append(", ");
		msg.append("end time: ").append(endTime).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append("\n");
		msg.append(bhv.toString()).append(", ");
		return msg.toString();
	}
	
	public static class Builder {
		private Date time = null;
		private long duration = 0;
		private Date endTime = null;
		private TimeZone timeZone = null;
		private UserBhv bhv = null;
//		private Map<EnvType, UserEnv> initialUEnvs = new HashMap<EnvType, UserEnv>();
//		private final List<ChangedUserEnv> changedUEnvs = new ArrayList<ChangedUserEnv>();
//		private final Map<EnvType, UserEnv> lastUEnvs = new HashMap<EnvType, UserEnv>();
//		private Map<Date, UserLoc> locs = new TreeMap<Date, UserLoc>();
//		private UserLoc lastLoc = null;
//		private Map<Date, UserLoc> places = new TreeMap<Date, UserLoc>();
//		private UserLoc lastPlace = null;

		public Builder(){}
		
		public DurationUserBhv build(){
			if(time != null && endTime != null)
				duration = endTime.getTime() - time.getTime();
			return new DurationUserBhv(this);
		}
		
		public Date getEndTime() {
			return endTime;
		}
		
		public Builder setTime(Date sTime) {
			this.time = sTime;
			return this;
		}
		
		public Builder setDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public Builder setEndTime(Date eTime) {
			this.endTime = eTime;
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
		
//		public Builder addInitialUserEnv(UserEnv userEnv){
//			initialUEnvs.put(userEnv.getEnvType(), userEnv);
//			return this;
//		}
//		
//		public Builder setInitialUserEnvs(HashMap<EnvType, UserEnv> initialUEnvs){
//			this.initialUEnvs = initialUEnvs;
//			return this;
//		}
		
//		public Builder setLocs(Map<Date, UserLoc> locs){
//			this.locs = locs;
//			return this;
//		}
//		public Builder setPlaces(Map<Date, UserLoc> places){
//			this.places = places;
//			return this;
//		}
//		public Builder appendLoc(UserLoc uLoc, Date time) {
//			if(lastLoc == null) {
//				lastLoc = uLoc;
//				locs.put(time, uLoc);
//			} else {
//				if(uLoc.equals(lastLoc))
//					;
//				else { 
//					lastLoc = uLoc;
//					locs.put(time, uLoc);
//				}
//			}
//			return this;
//		}
//		public Builder appendPlace(UserLoc place, Date time) {
//			if(lastPlace == null) {
//				lastPlace = place;
//				places.put(time, place);
//			} else {
//				if(place.equals(lastPlace)) 
//					;
//				else {
//					lastPlace = place;
//					places.put(time, place);
//				}
//			}
//			return this;
//		}
		
//		public Builder setPlaces(Map<Date, UserLoc> places){
//			this.places = places;
//			return this;
//		}
//		public Builder appendEnv(EnvType envType, UserEnv userEnv, Date time) {
//			if(!initialUEnvs.containsKey(envType)) {
//				initialUEnvs.put(envType, userEnv);
//				lastUEnvs.put(envType, userEnv);
//			} else {
//				
//			}
//			UserEnv lastPlace = 
//			if(lastPlace == null) {
//				lastPlace = userEnv;
//				places.put(time, userEnv);
//			} else {
//				if(userEnv.equals(lastPlace)) 
//					;
//				else {
//					lastPlace = userEnv;
//					places.put(time, userEnv);
//				}
//			}
//			return this;
//		}
	}
}