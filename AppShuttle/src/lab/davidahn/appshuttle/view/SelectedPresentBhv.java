package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;

public class SelectedPresentBhv extends PresentBhv {

	public SelectedPresentBhv(UserBhv uBhv) {
		super(uBhv);
	}
	
	@Override
	public ViewableBhvType getViewableBhvType() {
		return ViewableBhvType.SELECTED;
	}

	@Override
	public String getViewMsg() {
		return "";
	}

}
