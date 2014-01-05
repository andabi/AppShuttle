package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public class NormalBhv extends ViewableUserBhv {

	public NormalBhv(UserBhv uBhv){
		super(uBhv);
	}
	
	@Override
	public Integer getNotibarContainerId() {
		return R.id.noti_present_container;
	}
}
