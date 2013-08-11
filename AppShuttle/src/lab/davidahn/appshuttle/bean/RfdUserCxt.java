package lab.davidahn.appshuttle.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.exception.InvalidLocationException;

public class RfdUserCxt {
	int contextId;
	private Date sTime;
	private Date eTime;
	private TimeZone timeZone;
	private UserBhv bhv;
	private Map<Date, UserLoc> locs = new HashMap<Date, UserLoc>();
	private UserLoc lastPlace;
	private Map<Date, UserLoc> places = new HashMap<Date, UserLoc>();

	public RfdUserCxt(Date sTime, Date eTime, TimeZone timeZone, UserBhv bhv) {
		this.sTime = sTime;
		this.eTime = eTime;
		this.timeZone = timeZone;
		this.bhv = bhv;
	}
	
//	public RfdUserCxt(Date sTime, Date eTime, TimeZone timeZone, Map<Long, UserLoc> locFreqList, Map<Long, UserLoc> placeFreqList, UserBhv bhv) {
//		this.sTime = sTime;
//		this.eTime = eTime;
//		this.timeZone = timeZone;
//		this.locFreqList = locFreqList;
//		this.placeFreqList = placeFreqList;
//		this.bhv = bhv;
//	}
	
	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	public UserBhv getBhv() {
		return bhv;
	}
	public Date getStartTime() {
		return sTime;
	}
	public Date getEndTime() {
		return eTime;
	}
	public void setEndTime(Date eTime) {
		this.eTime = eTime;
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
	public void setLocs(Map<Date, UserLoc> locs){
		this.locs = locs;
	}
	public void setPlaces(Map<Date, UserLoc> places){
		this.places = places;
	}
	public void appendLoc(UserLoc uLoc, Date time){
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
			return ;
		}
	}
	public void appendPlace(UserLoc place, Date time){
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
			return ;
		}
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
}