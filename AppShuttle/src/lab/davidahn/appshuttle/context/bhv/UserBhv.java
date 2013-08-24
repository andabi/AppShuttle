package lab.davidahn.appshuttle.context.bhv;


public class UserBhv {
	protected BhvType bhvType;
	protected String bhvName;
	
	public UserBhv(BhvType bhvType, String bhvName) {
		this.bhvType = bhvType;
		this.bhvName = bhvName;
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
	
//	public boolean isValid(Context cxt){
//		return true;
//	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("behavior type: ").append(bhvType.toString()).append(", ");
		msg.append("behavior name: ").append(bhvName);
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if((o instanceof UserBhv) && bhvName.equals(((UserBhv)o).bhvName) 
				&& bhvType == ((UserBhv)o).bhvType)
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return (bhvType.hashCode() + bhvName.hashCode()) / 2;
	}
}