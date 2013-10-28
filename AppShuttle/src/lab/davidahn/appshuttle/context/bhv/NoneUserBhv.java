package lab.davidahn.appshuttle.context.bhv;

public class NoneUserBhv extends BaseUserBhv {
	
	public NoneUserBhv(BhvType bhvType, String bhvName) {
		super(bhvType, bhvName);
	}

	@Override
	public boolean isValid() {
		return false;
	}
}