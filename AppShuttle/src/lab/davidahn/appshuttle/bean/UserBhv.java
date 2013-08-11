package lab.davidahn.appshuttle.bean;

public class UserBhv {
	private String bhvType;
	private String bhvName;
	
	public UserBhv(String bhvType, String bhvName) {
		this.bhvType = bhvType;
		this.bhvName = bhvName;
	}

	public String getBhvType() {
		return bhvType;
	}
	
	public void setBhvType(String bhvType) {
		this.bhvType = bhvType;
	}
	
	public String getBhvName() {
		return bhvName;
	}

	public void setBhvName(String bhvName) {
		this.bhvName = bhvName;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("behavior type: ").append(bhvType).append(", ");
		msg.append("behavior name: ").append(bhvName);
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if((o instanceof UserBhv) && bhvName.equals(((UserBhv)o).bhvName) 
				&& bhvType.equals(((UserBhv)o).bhvType)) 
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return (bhvType.hashCode() + bhvName.hashCode()) / 2;
	}
}