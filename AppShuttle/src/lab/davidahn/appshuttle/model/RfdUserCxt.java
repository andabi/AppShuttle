package lab.davidahn.appshuttle.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.exception.InvalidLocationException;

public class RfdUserCxt {
	int contextId;
	private Date sTime;
	private Date eTime;
	private TimeZone timeZone;
	private List<LocFreq> locFreqList = new ArrayList<LocFreq>();
	private List<LocFreq> placeFreqList = new ArrayList<LocFreq>();
	private UserBhv bhv;

	public RfdUserCxt(Date sTime, Date eTime, TimeZone timeZone, UserBhv bhv) {
		this.sTime = sTime;
		this.eTime = eTime;
		this.timeZone = timeZone;
		this.bhv = bhv;
	}
	
	public RfdUserCxt(Date sTime, Date eTime, TimeZone timeZone, List<LocFreq> locFreqList, List<LocFreq> placeFreqList, UserBhv bhv) {
		this.sTime = sTime;
		this.eTime = eTime;
		this.timeZone = timeZone;
		this.locFreqList = locFreqList;
		this.placeFreqList = placeFreqList;
		this.bhv = bhv;
	}
	
	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	public UserBhv getBhv() {
		return bhv;
	}
	public void setBhv(UserBhv bhv) {
		this.bhv = bhv;
	}
	public Date getStartTime() {
		return sTime;
	}
//	public void setStartTime(Date time) {
//		this.sTime = time;
//	}
	
	public Date getEndTime() {
		return eTime;
	}
	public void setEndTime(Date eTime) {
		this.eTime = eTime;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
//	public void setTimeZone(TimeZone timeZone) {
//		this.timeZone = timeZone;
//	}
	
	public List<LocFreq> getLocFreqList() {
		return locFreqList;
	}
//	public void setLocFreqList(List<LocFreq> locList) {
//		this.locFreqList = locList;
//	}
	public List<LocFreq> getPlaceFreqList() {
		return placeFreqList;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(bhv.toString()).append(", ");
		msg.append("start time: ").append(sTime).append(", ");
		msg.append("end time: ").append(eTime).append(", ");
		msg.append("timeZone: ").append(timeZone.getID()).append(", ");
		msg.append(locFreqList.toString());
		msg.append(placeFreqList.toString());
		return msg.toString();
	}
	public void addLoc(UserLoc uLoc){
		try {
			if(!uLoc.isValid()) {
				throw new InvalidLocationException();
			}
			if(locFreqList.isEmpty()) locFreqList.add(new LocFreq(uLoc, 1));
			else {
				LocFreq lastLocFreq = locFreqList.get(locFreqList.size()-1);
				if(lastLocFreq.getULoc().equals(uLoc)) lastLocFreq.setFreq(lastLocFreq.getFreq() + 1);
				else locFreqList.add(new LocFreq(uLoc, 1));
			}
		} catch (InvalidLocationException e) {
			return ;
		}
	}
	public void addLoc(List<UserLoc> uLocList){
		for(UserLoc uLoc : uLocList){
			addLoc(uLoc);
		}
	}
	public void addLocFreq(List<LocFreq> locFreqList){
		for(LocFreq locFreq : locFreqList){
			for(int i=0;i<locFreq.getFreq();i++){
				addLoc(locFreq.getULoc());
			}
		}
	}
	public void addPlace(UserLoc place){
		try {
			if(!place.isValid()) {
				throw new InvalidLocationException();
			}
			if(placeFreqList.isEmpty()) placeFreqList.add(new LocFreq(place, 1));
			else {
				LocFreq lastPlaceFreq = placeFreqList.get(placeFreqList.size()-1);
				if(lastPlaceFreq.getULoc().equals(place)) lastPlaceFreq.setFreq(lastPlaceFreq.getFreq() + 1);
				else placeFreqList.add(new LocFreq(place, 1));
			}
		} catch (InvalidLocationException e) {
			return ;
		}
	}
	public void addPlace(List<UserLoc> uPlaceList){
		for(UserLoc userPlace : uPlaceList){
			addPlace(userPlace);
		}
	}
	public void addPlaceFreq(List<LocFreq> locFreqList){
		for(LocFreq locFreq : locFreqList){
			for(int i=0;i<locFreq.getFreq();i++){
				addPlace(locFreq.getULoc());
			}
		}
	}
}