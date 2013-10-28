package lab.davidahn.appshuttle.context.bhv;

import java.util.HashMap;
import java.util.Map;

public class BaseUserBhv implements UserBhv {
	protected BhvType _bhvType;
	protected String _bhvName;
	protected Map<String, Object> _metas;
	
	public BaseUserBhv(BhvType bhvType, String bhvName) {
		_bhvType = bhvType;
		_bhvName = bhvName;
		_metas = new HashMap<String, Object>();
	}
	
	//TODO userBhv pool 관리
	
	public static BaseUserBhv create(BhvType bhvType, String bhvname){
		switch (bhvType){
			case NONE:
				return new NoneUserBhv(bhvType, bhvname);
			case APP:
				return new AppUserBhv(bhvType, bhvname);
			case CALL:
				return new CallUserBhv(bhvType, bhvname);
			default:
				throw new IllegalArgumentException("unknown bhv type");
		}
	}

	public BhvType getBhvType() {
		return _bhvType;
	}
	
	public void setBhvType(BhvType bhvType) {
		_bhvType = bhvType;
	}
	
	public String getBhvName() {
		return _bhvName;
	}

	public void setBhvName(String bhvName) {
		_bhvName = bhvName;
	}
	
	public Map<String, Object> getMetas() {
		return _metas;
	}
	
	public void setMetas(Map<String, Object> metas) {
		_metas = metas;
	}

	public Object getMeta(String key) {
		return _metas.get(key);
	}
	
	public void setMeta(String key, Object val){
		_metas.put(key, val);
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("behavior type: ").append(_bhvType.toString()).append(", ");
		msg.append("behavior name: ").append(_bhvName).append(", ");
		msg.append("metas: ").append(_metas.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if((o instanceof BaseUserBhv) 
				&& _bhvName.equals(((BaseUserBhv)o)._bhvName) 
				&& _bhvType == ((BaseUserBhv)o)._bhvType)
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return _bhvType.hashCode() ^ _bhvName.hashCode();
	}
	
	public boolean isValid() {
		return true;
	}
}