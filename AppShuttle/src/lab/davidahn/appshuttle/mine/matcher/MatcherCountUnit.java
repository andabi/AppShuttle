package lab.davidahn.appshuttle.mine.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.context.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public class MatcherCountUnit {
	private final UserBhv uBhv;
	private final List<DurationUserBhv> rfdUserCxtList;
	private final Map<String, Object> properties;

	private MatcherCountUnit(Builder builder) {
		uBhv = builder.uBhv;
		rfdUserCxtList = builder.rfdUserCxtList;
		properties = builder.properties;
	}
	
	public UserBhv getUBhv() {
		return uBhv;
	}

	public List<DurationUserBhv> getRfdUserCxtList() {
		return rfdUserCxtList;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("bhv: ").append(uBhv).append(", ");
		msg.append("properties: ").append(properties.toString());
		return msg.toString();
	}
	
	public static class Builder {
		private UserBhv uBhv = null;
		private List<DurationUserBhv> rfdUserCxtList = new ArrayList<DurationUserBhv>();
		private Map<String, Object> properties = new HashMap<String, Object>();

		public Builder(UserBhv uBhv){
			this.uBhv = uBhv;
		}
		
		public MatcherCountUnit build(){
			return new MatcherCountUnit(this);
		}
		
		public void setProperty(String key, Object val){
			properties.put(key, val);
		}
		
		public void addRfdUserCxtList(DurationUserBhv rfdUserCxt){
			rfdUserCxtList.add(rfdUserCxt);
		}	
	}
}