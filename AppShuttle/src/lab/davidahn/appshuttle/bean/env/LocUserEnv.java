package lab.davidahn.appshuttle.bean.env;

import lab.davidahn.appshuttle.bean.UserLoc;

public class LocUserEnv extends UserEnv {
	private UserLoc loc;

	public LocUserEnv(UserLoc loc) {
		super(EnvType.LOCATION);
		this.loc = loc;
	}
	
	public UserLoc getLoc() {
		return loc;
	}
	public void setLoc(UserLoc loc) {
		this.loc = loc;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(loc.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof LocUserEnv) 
				&& loc.equals(((LocUserEnv)o).loc))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}