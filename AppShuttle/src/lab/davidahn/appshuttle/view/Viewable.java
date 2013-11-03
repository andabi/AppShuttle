package lab.davidahn.appshuttle.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface Viewable {
//	public UserBhv getUserBhv();
	public Drawable getIcon();
	public String getBhvNameText();
	public String getViewMsg();
	public Intent getLaunchIntent();
	public int getNotibarContainerId();
}
