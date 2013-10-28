package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public class MatcherCountUnit {
	private final UserBhv _uBhv;
	private final List<DurationUserBhv> _durationUserBhvList;
	private final Map<String, Object> _properties;

	private MatcherCountUnit(Builder builder) {
		_uBhv = builder._uBhv;
		_durationUserBhvList = builder._durationUserBhvList;
		_properties = builder._properties;
	}
	
	public UserBhv getUBhv() {
		return _uBhv;
	}

	public List<DurationUserBhv> getRfdUserCxtList() {
		return _durationUserBhvList;
	}

	public Map<String, Object> getProperties() {
		return _properties;
	}

	public Object getProperty(String key) {
		return _properties.get(key);
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("bhv: ").append(_uBhv).append(", ");
		msg.append("properties: ").append(_properties.toString());
		return msg.toString();
	}
	
	public static class Builder {
		private UserBhv _uBhv = null;
		private List<DurationUserBhv> _durationUserBhvList = new ArrayList<DurationUserBhv>();
		private Map<String, Object> _properties = new HashMap<String, Object>();

		public Builder(UserBhv uBhv){
			_uBhv = uBhv;
		}
		
		public MatcherCountUnit build(){
			return new MatcherCountUnit(this);
		}
		
		public void setProperty(String key, Object val){
			_properties.put(key, val);
		}
		
		public void addRfdUserCxtList(DurationUserBhv rfdUserCxt){
			_durationUserBhvList.add(rfdUserCxt);
		}	
	}
}