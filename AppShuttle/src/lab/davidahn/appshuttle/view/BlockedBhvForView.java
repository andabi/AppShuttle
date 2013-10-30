package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.context.bhv.BlockedUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhv;
import android.text.format.DateUtils;

public class BlockedBhvForView extends BaseBhvForView {

	public BlockedBhvForView(BlockedUserBhv blockedUserBhv) {
		super(blockedUserBhv);
	}
	
	@Override
	public String getViewMsg() {
		if(_viewMsg == null) {
			long blockedTime = ((BlockedUserBhv)_uBhv).getBlockedTime();
			StringBuffer msg = new StringBuffer();
			msg.append(DateUtils.getRelativeTimeSpanString(blockedTime, 
					System.currentTimeMillis(), 
					DateUtils.MINUTE_IN_MILLIS, 
					0
					));
			_viewMsg = msg.toString();
		}
		
		return _viewMsg;
	}
	
//	public static List<BhvForView> extractViewList(List<BlockedUserBhv> blockedBhvInfoList) {
//		if(blockedBhvInfoList == null)
//			return Collections.emptyList();
//		
//		List<BhvForView> res = new ArrayList<BhvForView>();
//		for(BlockedUserBhv blockedUBhv : blockedBhvInfoList){
//			res.add(new BlockedBhvForView(blockedUBhv));
//		}
//		return res;
//	}
		
	public static List<BhvForView> extractViewList(List<PredictedBhv> predictedBhvInfoList) {
		if(predictedBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		Set<BlockedUserBhv> blockedUserBhvSet = UserBhvManager.getInstance().getBlockedBhvSet();
		for(BlockedUserBhv blockedUBhv : blockedUserBhvSet){
			if(predictedBhvInfoList.contains(blockedUBhv))
				new BlockedBhvForView(new BlockedUserBhv(blockedUBhv, blockedUBhv.
				blockedUBhv.set
				res.add()
			else
				res.add(new BlockedBhvForView(blockedUBhv));
		}
		for(BlockedUserBhv blockedUBhv : blockedBhvInfoList){
			res.add(new BlockedBhvForView(blockedUBhv));
		}
		return res;
	}
}
