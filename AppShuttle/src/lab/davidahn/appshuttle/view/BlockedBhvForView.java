package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.BlockedUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;

public class BlockedBhvForView extends BaseBhvForView {

	public BlockedBhvForView(UserBhv blockedUserBhv) {
		super(blockedUserBhv);
	}
	
	@Override
	public String getViewMsg() {
		if(_viewMsg == null) {
			Date d = new Date(((BlockedUserBhv)_uBhv).getBlockedTime());

			StringBuffer msg = new StringBuffer();
			msg.append(d.toString());
			
			_viewMsg = msg.toString();
		}
		
		return _viewMsg;
	}
	
	public static List<BhvForView> convert(List<UserBhv> blockedBhvInfoList) {
		if(blockedBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		for(UserBhv info : blockedBhvInfoList){
			res.add(new BlockedBhvForView(info));
		}
		return res;
	}
}
