package lab.davidahn.appshuttle.collect.bhv;


public class NoneUserBhv extends BaseUserBhv {
	
	public NoneUserBhv(UserBhvType bhvType, String bhvName) {
		super(bhvType, bhvName);
	}

	@Override
	public boolean isValid() {
		return false;
	}
}