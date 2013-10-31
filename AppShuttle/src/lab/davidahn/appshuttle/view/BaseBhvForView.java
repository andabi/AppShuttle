//package lab.davidahn.appshuttle.view;
//
//import lab.davidahn.appshuttle.AppShuttleApplication;
//import lab.davidahn.appshuttle.R;
//import lab.davidahn.appshuttle.context.bhv.BhvType;
//import lab.davidahn.appshuttle.context.bhv.UserBhv;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//
//public class BaseBhvForView implements BhvForView {
//
//	protected UserBhv _uBhv;
//	protected Drawable _icon;
//	protected String _bhvNameText;
//	protected String _viewMsg;
//	protected Intent _launchIntent;
//
//	public BaseBhvForView(UserBhv bhvInfo) {
//		_uBhv = bhvInfo;
//	}
//	
//	public UserBhv getUserBhv() {
//		return _uBhv;
//	}
//	
//	public Drawable getIcon() {
//		if(_icon == null) {
//			_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
//			
//			BhvType bhvType = _uBhv.getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = _uBhv.getBhvName();
//					try {
//						_icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
//				case NONE:
//					;
//				default:
//					;
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
//			BhvType bhvType = _uBhv.getBhvType();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					String packageName = _uBhv.getBhvName();
//					try {
//						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
//						_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
//					} catch (NameNotFoundException e) {}
//					break;
//				case CALL:
//					_bhvNameText = (String) (_uBhv).getMeta("cachedName");
//				case NONE:
//					;
//				default:
//					;
//				}
//		}
//		
//		return _bhvNameText;
//	}
//
//	public String getViewMsg() {
//		_viewMsg = "";
////		if(_viewMsg == null) {
////			List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(_bhvInfo.getMatchedResultMap().keySet());
////			Collections.sort(matcherTypeList, new MatcherTypeComparator());
////			
////			StringBuffer msg = new StringBuffer();
////			for (MatcherType matcherType : matcherTypeList) {
////				msg.append(matcherType.viewMsg).append(", ");
////			}
////			msg.delete(msg.length() - 2, msg.length());
////			_viewMsg = msg.toString();
////		}
////		
//		return _viewMsg;
//	}
//	
//	public Intent getLaunchIntent() {
//		if(_launchIntent == null) {
//			_launchIntent = new Intent();
//			
//			BhvType bhvType = _uBhv.getBhvType();
//			String bhvName = _uBhv.getBhvName();
//			switch(bhvType){
//				case APP:
//					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
//					_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
//					break;
//				case CALL:
//					_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
//				case NONE:
//					;
//				default:
//					;
//			}
//		}
//		
//		return _launchIntent;
//	}
//	
////	public static List<? extends BhvForView> convert(List<? extends UserBhv> bhvInfoList) {
////		if(bhvInfoList == null)
////			return Collections.emptyList();
////		
////		List<BhvForView> res = new ArrayList<BhvForView>();
////		for(UserBhv info : bhvInfoList){
////			res.add(new BaseBhvForView(info));
////		}
////		return res;
////	}
////	
////	public static List<BaseBhvInfoForView> convert(List<UserBhv> bhvInfoList) {
////		if(bhvInfoList == null)
////			return Collections.emptyList();
////		
////		List<BaseBhvInfoForView> res = new ArrayList<BaseBhvInfoForView>();
////		for(PredictedBhvInfo info : bhvInfoList){
////			res.add(new BaseBhvInfoForView(info));
////		}
////		return res;
////	}
//}
