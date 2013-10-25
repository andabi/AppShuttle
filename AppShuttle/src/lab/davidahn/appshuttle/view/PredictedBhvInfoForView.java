package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.mine.matcher.MatcherType;
import lab.davidahn.appshuttle.mine.matcher.MatcherTypeComparator;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class PredictedBhvInfoForView {

	private PredictedBhvInfo _predictedBhvInfo;
	private Drawable _icon;
	private String _bhvNameText;
	private String _viewMsg;
	private Intent _launchIntent;

	public PredictedBhvInfoForView(PredictedBhvInfo predictedBhvInfo) {
		_predictedBhvInfo = predictedBhvInfo;
	}
	
	public Drawable getIcon() {
		if(_icon == null) {
			_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
			
			BhvType bhvType = _predictedBhvInfo.getUserBhv().getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = _predictedBhvInfo.getUserBhv().getBhvName();
					try {
						_icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
					} catch (NameNotFoundException e) {}
					break;
				case CALL:
					_icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
			}
		}
		
		return _icon;
	}

	public String getBhvNameText() {
		if(_bhvNameText == null) {
			_bhvNameText = "no name";
			
			BhvType bhvType = _predictedBhvInfo.getUserBhv().getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = _predictedBhvInfo.getUserBhv().getBhvName();
					try {
						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
						_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
					} catch (NameNotFoundException e) {}
					break;
				case CALL:
					_bhvNameText = (String) _predictedBhvInfo.getUserBhv().getMeta("cachedName");
				}
		}
		
		return _bhvNameText;
	}

	public String getViewMsg() {
		if(_viewMsg == null) {
			List<MatcherType> matcherTypeList = new ArrayList<MatcherType>(_predictedBhvInfo.getMatchedResultMap().keySet());
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
	
	public Intent getLaunchIntent() {
		if(_launchIntent == null) {
			_launchIntent = new Intent();
			
			BhvType bhvType = _predictedBhvInfo.getUserBhv().getBhvType();
			String bhvName = _predictedBhvInfo.getUserBhv().getBhvName();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
					break;
				case CALL:
					_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
			}
		}
		
		return _launchIntent;
	}
	
	public PredictedBhvInfo getPredictedBhvInfo() {
		return _predictedBhvInfo;
	}
	
	public static List<PredictedBhvInfoForView> getPredictedBhvInfoListForView(List<PredictedBhvInfo> predictedBhvInfoList) {
		List<PredictedBhvInfoForView> res = new ArrayList<PredictedBhvInfoForView>();
		
		for(PredictedBhvInfo info : predictedBhvInfoList){
			res.add(new PredictedBhvInfoForView(info));
		}

		return res;
	}
}
