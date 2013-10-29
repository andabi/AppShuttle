package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.BlockedUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.text.format.DateUtils;

public class BlockedBhvForView extends BaseBhvForView {

	public BlockedBhvForView(UserBhv blockedUserBhv) {
		super(blockedUserBhv);
	}
	
	@Override
	public String getViewMsg() {
		if(_viewMsg == null) {
			long blocked_time = ((BlockedUserBhv)_uBhv).getBlockedTime();
			StringBuffer msg = new StringBuffer();
			msg.append(DateUtils.getRelativeTimeSpanString(blocked_time, 
					System.currentTimeMillis(), 
					DateUtils.MINUTE_IN_MILLIS, 
					0
					));
			_viewMsg = msg.toString();
		}
		
		return _viewMsg;
	}
	
	public static List<BhvForView> convert(List<BlockedUserBhv> blockedBhvInfoList) {
		if(blockedBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		for(BlockedUserBhv blockedUBhv : blockedBhvInfoList){
			res.add(new BlockedBhvForView(blockedUBhv));
		}
		return res;
	}
}
