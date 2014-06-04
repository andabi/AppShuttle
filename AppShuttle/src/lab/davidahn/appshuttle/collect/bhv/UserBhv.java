package lab.davidahn.appshuttle.collect.bhv;

import java.util.Map;

import android.content.Intent;

public interface UserBhv {
	public UserBhvType getBhvType();
	public void setBhvType(UserBhvType bhvType);
	public String getBhvName();
	public void setBhvName(String bhvName);
//	public Object getMeta(String key);
//	public void setMeta(String key, Object val);
	public Map<String, Object> getMetas();
	public void setMetas(Map<String, Object> metas);
	public boolean isValid();
	public Intent getLaunchIntent();
}