package lab.davidahn.appshuttle.collect.env;

import java.util.Date;
import java.util.TimeZone;

public class DurationUserEnv {
	private Date _timeDate;
	private long _duration;
	private Date _endTimeDate;
	private TimeZone _timeZone;
	private EnvType _envType;
	private UserEnv _uEnv;

	public DurationUserEnv(Builder builder) {
		_timeDate = builder._timeDate;
		_endTimeDate = builder._endTimeDate;
		_timeZone = builder._timeZone;
		_uEnv = builder._uEnv;
		_envType = builder._envType;
		updateDuration();
	}
	
	public Date getTime() {
		return _timeDate;
	}
	public void setTime(Date time) {
		_timeDate = time;
		updateDuration();
	}
	public long getDuration() {
		return _duration;
	}
	public void setDuration(long duration) {
		_duration = duration;
	}
	public Date getEndTime() {
		return _endTimeDate;
	}
	public void setEndTime(Date endTime) {
		_endTimeDate = endTime;
		updateDuration();
	}
	public TimeZone getTimeZone() {
		return _timeZone;
	}
	public void setTimeZone(TimeZone timezone) {
		_timeZone = timezone;
	}
	public EnvType getEnvType() {
		return _envType;
	}
	public void setEnvType(EnvType envType) {
		_envType = envType;
	}
	public UserEnv getUserEnv() {
		return _uEnv;
	}
	public void setUserEnv(UserEnv userEnv) {
		_uEnv = userEnv;
	}

	private void updateDuration() {
		_duration = _endTimeDate.getTime() - _timeDate.getTime();
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(_timeDate).append(", ");
		msg.append("duration: ").append(_duration).append(", ");
		msg.append("endTime: ").append(_endTimeDate).append(", ");
		msg.append("timezone: ").append(_timeZone.getID()).append(", ");
		msg.append("envType: ").append(_envType.toString()).append(", ");
		msg.append("userEnv: ").append(_uEnv.toString());
		return msg.toString();
	}
	@Override
	public boolean equals(Object o) {
		if((o instanceof DurationUserEnv) 
				&& _timeDate.equals(((DurationUserEnv)o)._timeDate)
				&& _timeZone.equals(((DurationUserEnv)o)._timeZone)
				&& _envType.equals(((DurationUserEnv)o)._envType))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _timeDate.hashCode() ^ _timeZone.hashCode() ^ _envType.hashCode();
	}
	
	public static class Builder{
		private Date _timeDate = null;
		private Date _endTimeDate = null;
		private TimeZone _timeZone = null;
		private EnvType _envType;
		private UserEnv _uEnv = null;
		
		public Builder(){}
		
		public DurationUserEnv build(){
			return new DurationUserEnv(this);
		}
		
		public Builder setTime(Date time){
			_timeDate = time;
			return this;
		}
		public Builder setEndTime(Date endTime){
			_endTimeDate = endTime;
			return this;
		}
		public Builder setTimeZone(TimeZone timeZone){
			_timeZone = timeZone;
			return this;
		}
		public Builder setEnvType(EnvType envType){
			_envType = envType;
			return this;
		}
		public Builder setUserEnv(UserEnv userEnv){
			_uEnv = userEnv;
			return this;
		}
		
		public Date getTimeDate(){
			return _timeDate;
		}
		public UserEnv getUserEnv() {
			return _uEnv;
		}
	}
}
