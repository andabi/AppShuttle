package lab.davidahn.appshuttle.context.bhv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.mine.matcher.MatcherType;
import lab.davidahn.appshuttle.mine.matcher.MatcherTypeComparator;
import lab.davidahn.appshuttle.mine.matcher.PredictionInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import lab.davidahn.appshuttle.view.Viewable;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;


public class FavoratesUserBhv implements UserBhv, Viewable, Comparable<FavoratesUserBhv> {
	private UserBhv _uBhv;
	private long _setTime;
	
	protected Drawable _icon;
	protected String _bhvNameText;
	protected String _viewMsg;
	protected Intent _launchIntent;
	
	public FavoratesUserBhv(UserBhv uBhv, long setTime){
		_uBhv = uBhv;
		_setTime = setTime;
	}
	
	public UserBhv getUserBhv() {
		return _uBhv;
	}

	@Override
	public BhvType getBhvType() {
		return _uBhv.getBhvType();
	}
	@Override
	public void setBhvType(BhvType bhvType) {
		_uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return _uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		_uBhv.setBhvName(bhvName);
	}
	@Override
	public Object getMeta(String key) {
		return _uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		_uBhv.setMeta(key, val);
	}
	
	public long getSetTime() {
		return _setTime;
	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& _uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& _uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	@Override
	public int compareTo(FavoratesUserBhv uBhv) {
		if(_setTime > uBhv._setTime)
			return 1;
		else if(_setTime == uBhv._setTime)
			return 0;
		else
			return -1;
	}
	
	public Drawable getIcon() {
		_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
		
		BhvType bhvType = getBhvType();
		switch(bhvType){
			case APP:
				PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
				String packageName = getBhvName();
				try {
					_icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
				} catch (NameNotFoundException e) {}
				break;
			case CALL:
				_icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
			case NONE:
				;
			default:
				;
		}
		
		return _icon;
	}

	public String getBhvNameText() {
		_bhvNameText = "(no name)";
		
		BhvType bhvType = getBhvType();
		switch(bhvType){
			case APP:
				PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
				String packageName = getBhvName();
				try {
					ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
					_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
				} catch (NameNotFoundException e) {}
				break;
			case CALL:
				_bhvNameText = (String) getMeta("cachedName");
				break;
			case NONE:
				break;
			default:
				;
			}
		
		return _bhvNameText;
	}

	public String getViewMsg() {
		StringBuffer msg = new StringBuffer();
		_viewMsg = msg.toString();

		Predictor predictor = Predictor.getInstance();
		PredictionInfo predictedBhv = predictor.getPredictedBhv(_uBhv);
		
		if(predictedBhv == null) {
			return _viewMsg;
		}
		
		List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(predictedBhv.getMatchedResultMap().keySet());
		Collections.sort(matcherTypeList, new MatcherTypeComparator());
		
		for (MatcherType matcherType : matcherTypeList) {
			msg.append(matcherType.viewMsg).append(", ");
		}
		msg.delete(msg.length() - 2, msg.length());
		_viewMsg = msg.toString();
		
		return _viewMsg;
	}
	
//	@Override
//	public String getViewMsg() {
//		if(_viewMsg == null) {
////			long blockedTime = ((BlockedUserBhv)_uBhv).getBlockedTime();
//			StringBuffer msg = new StringBuffer();
//			_viewMsg = msg.toString();
//			
//			msg.append(DateUtils.getRelativeTimeSpanString(_setTime, 
//					System.currentTimeMillis(), 
//					DateUtils.MINUTE_IN_MILLIS, 
//					0
//					));
//			_viewMsg = msg.toString();
//		}
//		
//		return _viewMsg;
//	}
	
	public Intent getLaunchIntent() {
		_launchIntent = new Intent();
		
		BhvType bhvType = getBhvType();
		String bhvName = getBhvName();
		switch(bhvType){
			case APP:
				PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
				_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
				break;
			case CALL:
				_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				break;
			case NONE:
				break;
			default:
				;
		}
		
		return _launchIntent;
	}
	
//	public static List<FavoratesUserBhv> extractViewList(List<PredictionInfo> predictedBhvInfoList) {
//		if(predictedBhvInfoList == null)
//			return Collections.emptyList();
//		
//		List<FavoratesUserBhv> res = new ArrayList<FavoratesUserBhv>();
//		Set<FavoratesUserBhv> favoratesUserBhvSet = UserBhvManager.getInstance().getFavoratesBhvSet();
//		for(FavoratesUserBhv blockedUserBhv : favoratesUserBhvSet){
//			res.add(blockedUserBhv);
//		}
////		for(PredictedBhv predictedBhv : predictedBhvInfoList){
////			if(blockedUserBhvSet.contains(predictedBhv))
////				res.add(predictedBhv);
////		}
//		return res;
//	}
}
