package lab.davidahn.appshuttle.bhv;

import lab.davidahn.appshuttle.R;

public class NormalBhv extends ViewableUserBhv {

	public NormalBhv(UserBhv uBhv){
		super(uBhv);
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_present_container;
	}
}
