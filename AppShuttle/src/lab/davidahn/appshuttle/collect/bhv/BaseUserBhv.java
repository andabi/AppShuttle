package lab.davidahn.appshuttle.collect.bhv;

import java.util.HashMap;
import java.util.Map;

public class BaseUserBhv implements UserBhv {
	protected UserBhvType _bhvType;
	protected String _bhvName;
	protected Map<String, Object> _metas;
	
	public BaseUserBhv(UserBhvType bhvType, String bhvName) {
		_bhvType = bhvType;
		_bhvName = bhvName;
		_metas = new HashMap<String, Object>();
	}
	
	//TODO userBhv pool 관리
	public static BaseUserBhv create(UserBhvType bhvType, String bhvName){
		switch (bhvType){
		case NONE:
			return new NoneUserBhv(bhvType, bhvName);
		case APP:
			return new AppUserBhv(bhvType, bhvName);
		case CALL:
			return new CallUserBhv(bhvType, bhvName);
		case SENSOR_ON:
			return new SensorOnUserBhv(bhvType, bhvName);
		default:
			throw new IllegalArgumentException("unknown bhv type");
		}
	}

	@Override
	public UserBhvType getBhvType() {
		return _bhvType;
	}
	
	@Override
	public void setBhvType(UserBhvType bhvType) {
		_bhvType = bhvType;
	}
	
	@Override
	public String getBhvName() {
		return _bhvName;
	}
	
	@Override
	public void setBhvName(String bhvName) {
		_bhvName = bhvName;
	}
	
	public Map<String, Object> getMetas() {
		return _metas;
	}
	
	public void setMetas(Map<String, Object> metas) {
		_metas = metas;
	}

	@Override
	public Object getMeta(String key) {
		return _metas.get(key);
	}
	
	@Override
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
		if((o instanceof UserBhv) 
				&& _bhvName.equals(((UserBhv)o).getBhvName()) 
				&& _bhvType == ((UserBhv)o).getBhvType())
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