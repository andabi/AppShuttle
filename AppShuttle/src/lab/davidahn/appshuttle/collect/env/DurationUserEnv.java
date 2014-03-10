package lab.davidahn.appshuttle.collect.env;

import java.util.TimeZone;

public class DurationUserEnv {
	private long _time;
	private long _duration;
	private long _endTime;
	private TimeZone _timeZone;
	private UserEnv _uEnv;

	public DurationUserEnv(Builder builder) {
		_time = builder._time;
		_endTime = builder._endTime;
		_timeZone = builder._timeZone;
		_uEnv = builder._uEnv;
		updateDuration();
	}
	
	public long getTime() {
		return _time;
	}
	public void setTime(long time) {
		_time = time;
		updateDuration();
	}
	public long getDuration() {
		return _duration;
	}
	public void setDuration(long duration) {
		_duration = duration;
	}
	public long getEndTime() {
		return _endTime;
	}
	public void setEndTime(long endTime) {
		_endTime = endTime;
		updateDuration();
	}
	public TimeZone getTimeZone() {
		return _timeZone;
	}
	public void setTimeZone(TimeZone timezone) {
		_timeZone = timezone;
	}
	public UserEnv getUserEnv() {
		return _uEnv;
	}
	public void setUserEnv(UserEnv userEnv) {
		_uEnv = userEnv;
	}

	private void updateDuration() {
		_duration = _endTime - _time;
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("time: ").append(_time).append(", ");
		msg.append("duration: ").append(_duration).append(", ");
		msg.append("endTime: ").append(_endTime).append(", ");
		msg.append("timezone: ").append(_timeZone.getID()).append(", ");
		msg.append("userEnv: ").append(_uEnv.toString());
		return msg.toString();
	}
	@Override
	public boolean equals(Object o) {
		if((o instanceof DurationUserEnv) 
				&& _time == ((DurationUserEnv)o)._time
				&& _timeZone.equals(((DurationUserEnv)o)._timeZone)
				&& _uEnv.getEnvType().equals(((DurationUserEnv)o)._uEnv.getEnvType()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return Long.valueOf(_time).hashCode() ^ _timeZone.hashCode() ^ _uEnv.getEnvType().hashCode();
	}
	
	public static class Builder{
		private long _time = 0;
		private long _endTime = 0;
		private TimeZone _timeZone = null;
		private UserEnv _uEnv = null;
		
		public Builder(){}
		
		public DurationUserEnv build(){
			return new DurationUserEnv(this);
		}
		
		public Builder setTime(long time){
			_time = time;
			return this;
		}
		public Builder setEndTime(long endTime){
			_endTime = endTime;
			return this;
		}
		public Builder setTimeZone(TimeZone timeZone){
			_timeZone = timeZone;
			return this;
		}
		public Builder setUserEnv(UserEnv userEnv){
			_uEnv = userEnv;
			return this;
		}
		
		public long getTime(){
			return _time;
		}
		public UserEnv getUserEnv() {
			return _uEnv;
		}
	}
}
