package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface BhvForView {
	public UserBhv getUserBhv();
	public Drawable getIcon();
	public String getBhvNameText();
	public String getViewMsg();
	public Intent getLaunchIntent();
}
