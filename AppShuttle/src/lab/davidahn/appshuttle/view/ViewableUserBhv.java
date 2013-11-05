package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class ViewableUserBhv implements UserBhv, Viewable {

	protected UserBhv _uBhv;
	
	protected Drawable _icon;
	protected String _bhvNameText;
	protected String _viewMsg;
	protected Intent _launchIntent;

	public ViewableUserBhv(UserBhv bhvInfo) {
		_uBhv = bhvInfo;
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

	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return _uBhv.hashCode();
	}
	
	@Override
	public Drawable getIcon() {
		if(_icon == null) {
			_icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
			
			BhvType bhvType = _uBhv.getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = _uBhv.getBhvName();
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
		}
		
		return _icon;
	}

	@Override
	public String getBhvNameText() {
		if(_bhvNameText == null) {
			_bhvNameText = "no name";
			
			BhvType bhvType = _uBhv.getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = _uBhv.getBhvName();
					try {
						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
						_bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
					} catch (NameNotFoundException e) {}
					break;
				case CALL:
					_bhvNameText = (String) (_uBhv).getMeta("cachedName");
				case NONE:
					;
				default:
					;
				}
		}
		
		return _bhvNameText;
	}

	@Override
	public String getViewMsg() {
		_viewMsg = "";
		return _viewMsg;
	}
	
	@Override
	public Intent getLaunchIntent() {
		if(_launchIntent == null) {
			_launchIntent = new Intent();
			
			BhvType bhvType = _uBhv.getBhvType();
			String bhvName = _uBhv.getBhvName();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					_launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
					break;
				case CALL:
					_launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				case NONE:
					;
				default:
					;
			}
		}
		
		return _launchIntent;
	}

	@Override
	public Integer getNotibarContainerId() {
		return null;
	}
}
