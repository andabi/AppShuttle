package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.mine.matcher.MatcherType;
import lab.davidahn.appshuttle.mine.matcher.MatcherTypeComparator;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhv;

public class PredictedBhvForView extends BaseBhvForView {

	public PredictedBhvForView(PredictedBhv bhvInfo) {
		super(bhvInfo);
	}
	
	public String getViewMsg() {
		if(_viewMsg == null) {
			List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(((PredictedBhv)_uBhv).getMatchedResultMap().keySet());
			Collections.sort(matcherTypeList, new MatcherTypeComparator());
			
			StringBuffer msg = new StringBuffer();
			for (MatcherType matcherType : matcherTypeList) {
				msg.append(matcherType.viewMsg).append(", ");
			}
			msg.delete(msg.length() - 2, msg.length());
			_viewMsg = msg.toString();
		}
		
		return _viewMsg;
	}
	
	public static List<BhvForView> convert(List<PredictedBhv> predictedBhvInfoList) {
		if(predictedBhvInfoList == null)
			return Collections.emptyList();
		
		List<BhvForView> res = new ArrayList<BhvForView>();
		for(PredictedBhv info : predictedBhvInfoList){
			res.add(new PredictedBhvForView(info));
		}
		return res;
	}
	
//	public Drawable getIcon() {
//		if(_icon == null) {
//			_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
//			
//			BhvType bhvType = _bhvInfo.getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = _bhvInfo.getBhvName();
//					try {
//						_icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
//			}
//		}
//		
//		return _icon;
//	}
//
//	public String getBhvNameText() {
//		if(_bhvNameText == null) {
//			_bhvNameText = "no name";
//			
//			BhvType bhvType = _bhvInfo.getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = _bhvInfo.getBhvName();
//					try {
//						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
//						_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_bhvNameText = (String) _bhvInfo.getUserBhv().getMeta("cachedName");
//				}
//		}
//		
//		return _bhvNameText;
//	}
//
//	public String getViewMsg() {
//		if(_viewMsg == null) {
//			List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(_bhvInfo.getMatchedResultMap().keySet());
//			Collections.sort(matcherTypeList, new MatcherTypeComparator());
//			
//			StringBuffer msg = new StringBuffer();
//			for (MatcherType matcherType : matcherTypeList) {
//				msg.append(matcherType.viewMsg).append(", ");
//			}
//			msg.delete(msg.length() - 2, msg.length());
//			_viewMsg = msg.toString();
//		}
//		
//		return _viewMsg;
//	}
//	
//	public Intent getLaunchIntent() {
//		if(_launchIntent == null) {
//			_launchIntent = new Intent();
//			
//			BhvType bhvType = _bhvInfo.getBhvType();
//			String bhvName = _bhvInfo.getBhvName();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
//					break;
//				case CALL:
//					_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
//			}
//		}
//		
//		return _launchIntent;
//	}
	
//	public UserBhv getBhvInfo() {
//		return _bhvInfo;
//	}
}
