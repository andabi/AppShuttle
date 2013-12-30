package lab.davidahn.appshuttle.view.forscreenshot;

import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.view.PresentBhv;

public class MockPresentUserBhv extends PresentBhv{

	public MockPresentUserBhv(UserBhv bhvInfo, String _viewMsg) {
		super(bhvInfo);
		viewMsg = _viewMsg;
	}
	
	@Override
	public String getViewMsg() {
		return viewMsg;
	}
}
