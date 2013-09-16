package lab.davidahn.appshuttle.context.env;


//public class PlaceUserEnv extends UserEnv {
//	private UserPlace place;
//
//	public PlaceUserEnv(UserPlace place) {
//		super(EnvType.PLACE);
//		this.place = place;
//	}
//	
//	public UserPlace getPlace() {
//		return place;
//	}
//	public void setPlace(UserPlace place) {
//		this.place = place;
//	}
//
//	public String toString(){
//		StringBuffer msg = new StringBuffer();
//		msg.append(place.toString());
//		return msg.toString();
//	}
//	
//	@Override
//	public boolean equals(Object o) {
//		if((o instanceof PlaceUserEnv) 
//				&& place.equals(((PlaceUserEnv)o).place))
//			return true;
//		else return false;
//	}
//	
//	@Override
//	public int hashCode(){
//		return 0;
//	}
//}