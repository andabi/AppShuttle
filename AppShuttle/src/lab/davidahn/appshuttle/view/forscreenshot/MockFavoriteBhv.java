package lab.davidahn.appshuttle.view.forscreenshot;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.view.FavoriteBhv;

public class MockFavoriteBhv extends FavoriteBhv {

	public MockFavoriteBhv(UserBhv uBhv, String _viewMsg, boolean _isNotifiable) {
		super(uBhv, System.currentTimeMillis(), _isNotifiable);
		viewMsg = _viewMsg;
	}

	@Override
	public String getViewMsg() {
		return viewMsg;
	}
}
