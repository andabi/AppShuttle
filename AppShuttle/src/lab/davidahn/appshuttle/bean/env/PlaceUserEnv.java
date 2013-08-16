package lab.davidahn.appshuttle.bean.env;

import lab.davidahn.appshuttle.bean.UserLoc;

public class PlaceUserEnv extends UserEnv {
	private UserLoc place;

	public PlaceUserEnv(UserLoc place) {
		super(EnvType.PLACE);
		this.place = place;
	}
	
	public UserLoc getPlace() {
		return place;
	}
	public void setPlace(UserLoc place) {
		this.place = place;
	}

	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(place.toString());
		return msg.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof PlaceUserEnv) 
				&& place.equals(((PlaceUserEnv)o).place))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}