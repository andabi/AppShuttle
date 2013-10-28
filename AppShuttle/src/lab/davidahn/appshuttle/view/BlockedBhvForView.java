package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.BlockedUserBhv;

public class BlockedBhvForView extends BaseBhvForView {

	public BlockedBhvForView(BlockedUserBhv blockedUserBhv) {
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
	
	public static List<BhvForView> convert(List<BlockedUserBhv> blockedBhvInfoList) {
		if(blockedBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		for(BlockedUserBhv info : blockedBhvInfoList){
			res.add(new BlockedBhvForView(info));
		}
		return res;
	}
}
