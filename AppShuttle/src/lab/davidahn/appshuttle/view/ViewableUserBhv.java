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

	protected UserBhv uBhv;
	
	protected Drawable icon;
	protected String bhvNameText;
	protected String viewMsg;
	protected Intent launchIntent;

	public ViewableUserBhv(UserBhv bhvInfo) {
		uBhv = bhvInfo;
	}
	
	public UserBhv getUserBhv() {
		return uBhv;
	}
	
	@Override
	public BhvType getBhvType() {
		return uBhv.getBhvType();
	}
	@Override
	public void setBhvType(BhvType bhvType) {
		uBhv.setBhvType(bhvType);
	}
	@Override
	public String getBhvName() {
		return uBhv.getBhvName();
	}
	@Override
	public void setBhvName(String bhvName) {
		uBhv.setBhvName(bhvName);
	}
	@Override
	public Object getMeta(String key) {
		return uBhv.getMeta(key);
	}
	@Override
	public void setMeta(String key, Object val){
		uBhv.setMeta(key, val);
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
		return uBhv.hashCode();
	}
	
	@Override
	public Drawable getIcon() {
		if(icon == null) {
			icon = AppShuttleApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
			
			BhvType bhvType = uBhv.getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = uBhv.getBhvName();
					try {
						icon = (BitmapDrawable) packageManager.getApplicationIcon(packageName);
					} catch (NameNotFoundException e) {}
					break;
				case CALL:
					icon = AppShuttleApplication.getContext().getResources().getDrawable(android.R.drawable.sym_action_call);
				case NONE:
					;
				default:
					;
			}
		}
		
		return icon;
	}

	@Override
	public String getBhvNameText() {
		if(bhvNameText == null) {
			bhvNameText = "no name";
			
			BhvType bhvType = uBhv.getBhvType();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					String packageName = uBhv.getBhvName();
					try {
						ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
						bhvNameText = (String) (ai != null ? packageManager.getApplicationLabel(ai) : "no name");
					} catch (NameNotFoundException e) {}
					break;
				case CALL:
					bhvNameText = (String) (uBhv).getMeta("cachedName");
				case NONE:
					;
				default:
					;
				}
		}
		
		return bhvNameText;
	}

	@Override
	public String getViewMsg() {
		viewMsg = "";
		return viewMsg;
	}
	
	@Override
	public Intent getLaunchIntent() {
		if(launchIntent == null) {
			launchIntent = new Intent();
			
			BhvType bhvType = uBhv.getBhvType();
			String bhvName = uBhv.getBhvName();
			switch(bhvType){
				case APP:
					PackageManager packageManager = AppShuttleApplication.getContext().getPackageManager();
					launchIntent = packageManager.getLaunchIntentForPackage(bhvName);
					break;
				case CALL:
					launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				case NONE:
					;
				default:
					;
			}
		}
		
		return launchIntent;
	}

	@Override
	public Integer getNotibarContainerId() {
		return null;
	}
}
