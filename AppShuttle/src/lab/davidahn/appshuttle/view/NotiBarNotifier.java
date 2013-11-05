package lab.davidahn.appshuttle.view;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class NotiBarNotifier {

	private static final int UPDATE_NOTI_VIEW = 1;
	
	private NotificationManager _notificationManager;
//	private LayoutInflater layoutInflater;

	private AppShuttleApplication cxt = AppShuttleApplication.getContext();
	
	public NotiBarNotifier(){
		_notificationManager = (NotificationManager)cxt.getSystemService(Context.NOTIFICATION_SERVICE);
//		private NotificationManager _notificationManager;
	}
	
	@SuppressLint("NewApi")
	public <T extends UserBhv & Viewable> void updateNotiView(List<T> viewableUserBhv) {
		RemoteViews notiView = createNotiRemoteViews(viewableUserBhv);

		Notification notiUpdate = new Notification.Builder(cxt)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiView)
			.setOngoing(true)
			.setWhen(AppShuttleApplication.launchTime)
			.setPriority(Notification.PRIORITY_HIGH)
			.build();
		_notificationManager.notify(UPDATE_NOTI_VIEW, notiUpdate);
	}

	private <T extends UserBhv & Viewable> RemoteViews createNotiRemoteViews(List<T> viewableUserBhvList) {
		RemoteViews notiRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar);

		//clean
		notiRemoteView.removeAllViews(R.id.noti_ordinary_container);
		notiRemoteView.removeAllViews(R.id.noti_favorates_container);
		
		notiRemoteView.setOnClickPendingIntent(R.id.noti_icon, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));

		for(T viewableUserBhv : viewableUserBhvList) {

			BhvType bhvType = viewableUserBhv.getBhvType();
			RemoteViews notiElemRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar_element);
			
			notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, PendingIntent.getActivity(cxt, 0, viewableUserBhv.getLaunchIntent(), 0));

			BitmapDrawable iconDrawable = (BitmapDrawable)viewableUserBhv.getIcon();
			notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_icon, iconDrawable.getBitmap());
			
			if (bhvType == BhvType.CALL){
//				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemRemoteView.setTextViewText(R.id.noti_elem_text, viewableUserBhv.getBhvNameText());
				notiElemRemoteView.setTextViewTextSize(R.id.noti_elem_text, 
						TypedValue.COMPLEX_UNIT_PX, 
						cxt.getResources().getDimension(R.dimen.notibar_text_size));
			}
			
			Integer notibarContainerId = viewableUserBhv.getNotibarContainerId();
			if(notibarContainerId == null)
				continue;
			
			notiRemoteView.addView(notibarContainerId, notiElemRemoteView);
		}

		return notiRemoteView;
	}
	
	public int getNumElem() {
		int maxNumElem = cxt.getPreferenceSettings().getInt("viewer.noti.max_num_ordinary", 8);
		int NotibarIconAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_icon_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		int NotibarBhvAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_bhv_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		return Math.min(maxNumElem, (getNotibarWidth() - NotibarIconAreaWidth) / NotibarBhvAreaWidth);
	}
	
	public int getNotibarWidth(){
		WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = cxt.getResources().getDisplayMetrics().density;
	    return (int)(outMetrics.widthPixels / density);
	}
}

//View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));
//LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

//private int getNotiViewWidth() {
//WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
//Display display = wm.getDefaultDisplay();
//Point size = new Point();
//display.getSize(size);
//return size.x;
//}

