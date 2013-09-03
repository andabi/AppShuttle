package lab.davidahn.appshuttle.context.env;


public class CopyOfPlaceUserEnv extends UserEnv {
	private UserLoc place;

	public CopyOfPlaceUserEnv(UserLoc place) {
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
		if((o instanceof CopyOfPlaceUserEnv) 
				&& place.equals(((CopyOfPlaceUserEnv)o).place))
			return true;
		else return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}