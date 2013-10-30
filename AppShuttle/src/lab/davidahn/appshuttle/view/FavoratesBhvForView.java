package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.context.bhv.FavoratesUserBhv;
import android.text.format.DateUtils;

public class FavoratesBhvForView extends BaseBhvForView {

	public FavoratesBhvForView(FavoratesUserBhv favoratesUserBhv) {
		super(favoratesUserBhv);
	}
	
	@Override
	public String getViewMsg() {
		if(_viewMsg == null) {
			long setTime = ((FavoratesUserBhv)_uBhv).getSetTime();
			StringBuffer msg = new StringBuffer();
			msg.append(DateUtils.getRelativeTimeSpanString(setTime, 
					System.currentTimeMillis(), 
					DateUtils.MINUTE_IN_MILLIS, 
					0
					));
			_viewMsg = msg.toString();
		}
		
		return _viewMsg;
	}
	
	public static List<BhvForView> convert(List<FavoratesUserBhv> favoratesBhvInfoList) {
		if(favoratesBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		for(FavoratesUserBhv favoratesUBhv : favoratesBhvInfoList){			
			res.add(new FavoratesBhvForView(favoratesUBhv));
		}
		return res;
	}
}
