package lab.davidahn.appshuttle.context.bhv;


public class CallUserBhv extends UserBhv {
//	private String cachedName;
	
	public CallUserBhv(BhvType bhvType, String number, String cachedName) {
		super(bhvType, number);
//		this.cachedName = cachedName;
		setMeta("cachedName", cachedName);
	}

//	public String getCachedName() {
//		return cachedName;
//	}
//
//	public void setCachedName(String cachedName) {
//		this.cachedName = cachedName;
//	}
	
}