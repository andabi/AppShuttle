package lab.davidahn.appshuttle.context.bhv;

public class UserBhvFactory {
	public static UserBhv create(BhvType bhvType, String bhvname){
		switch (bhvType){
			case NONE:
				return new NoneUserBhv(bhvType, bhvname);
			case APP:
				return new AppUserBhv(bhvType, bhvname);
			case CALL:
				return new CallUserBhv(bhvType, bhvname);
			default:
				throw new IllegalArgumentException("unknown bhv type");
		}
	}
}
