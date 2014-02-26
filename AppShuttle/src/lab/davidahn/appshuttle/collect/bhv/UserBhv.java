package lab.davidahn.appshuttle.collect.bhv;


public interface UserBhv {
	public UserBhvType getBhvType();
	public void setBhvType(UserBhvType bhvType);
	public String getBhvName();
	public void setBhvName(String bhvName);
	public Object getMeta(String key);
	public void setMeta(String key, Object val);
//	public boolean isValid();
//	public Map<String, Object> getMetas();
//	public void setMetas(Map<String, Object> metas);
}