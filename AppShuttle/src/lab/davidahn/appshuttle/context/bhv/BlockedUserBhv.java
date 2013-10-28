package lab.davidahn.appshuttle.context.bhv;

public class BlockedUserBhv implements UserBhv, Comparable<BlockedUserBhv> {
	private BaseUserBhv _uBhv;
	private long _blockedTime;
	
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
	
	public long getBlockedTime() {
		return _blockedTime;
	}
	public void setBlockedTime(long blockedTime) {
		this._blockedTime = blockedTime;
	}
	
	@Override
	public boolean equals(Object o){
		return _uBhv.equals(o);
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	@Override
	public int compareTo(BlockedUserBhv uBhv) {
		if(_blockedTime < uBhv._blockedTime)
			return 1;
		else if(_blockedTime == uBhv._blockedTime)
			return 0;
		else
			return -1;
	}
}
