package lab.davidahn.appshuttle.context.bhv;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;


public class UserBhv {
	protected BhvType bhvType;
	protected String bhvName;
	protected Map<String, Object> metas;
	
	public UserBhv(BhvType bhvType, String bhvName) {
		this.bhvType = bhvType;
		this.bhvName = bhvName;
		this.metas = new HashMap<String, Object>();
	}

	public BhvType getBhvType() {
		return bhvType;
	}
	
	public void setBhvType(BhvType bhvType) {
		this.bhvType = bhvType;
	}
	
	public String getBhvName() {
		return bhvName;
	}

	public void setBhvName(String bhvName) {
		this.bhvName = bhvName;
	}
	
	public Map<String, Object> getMetas() {
		return metas;
	}
	
	public void setMetas(Map<String, Object> metas) {
		this.metas = metas;
	}

	public Object getMeta(String key) {
		return metas.get(key);
	}
	
	public void setMeta(String key, Object val){
		metas.put(key, val);
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("behavior type: ").append(bhvType.toString()).append(", ");
		msg.append("behavior name: ").append(bhvName).append(", ");
		msg.append("metas: ").append(metas.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if((o instanceof UserBhv) && bhvName.equals(((UserBhv)o).bhvName) 
				&& bhvType == ((UserBhv)o).bhvType)
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return bhvType.hashCode() ^ bhvName.hashCode();
	}
	
	public boolean isValid(Context cxt) {
		if(bhvType == BhvType.NONE)
			return false;
		else
			return true;
	}
}