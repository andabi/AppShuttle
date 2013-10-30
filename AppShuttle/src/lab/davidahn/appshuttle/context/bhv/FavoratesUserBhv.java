package lab.davidahn.appshuttle.context.bhv;


public class FavoratesUserBhv implements UserBhv, Comparable<FavoratesUserBhv> {
	private UserBhv _uBhv;
	private long _setTime;
	
	public FavoratesUserBhv(UserBhv uBhv, long setTime){
		_uBhv = uBhv;
		_setTime = setTime;
	}
	
	public UserBhv getUserBhv() {
		return _uBhv;
	}

	@Override
	public BhvType getBhvType() {
		return _uBhv.getBhvType();
	}
	@Override
	public void setBhvType(BhvType bhvType) {
		_uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return _uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		_uBhv.setBhvName(bhvName);
	}
	@Override
	public Object getMeta(String key) {
		return _uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		_uBhv.setMeta(key, val);
	}
	
	public long getSetTime() {
		return _setTime;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& _uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& _uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	@Override
	public int compareTo(FavoratesUserBhv uBhv) {
		if(_setTime > uBhv._setTime)
			return 1;
		else if(_setTime == uBhv._setTime)
			return 0;
		else
			return -1;
	}
	

}
