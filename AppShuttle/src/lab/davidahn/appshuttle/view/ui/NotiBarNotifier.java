package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.PresentBhv;
import lab.davidahn.appshuttle.view.Viewable;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class NotiBarNotifier {

	private static final int UPDATE_NOTI_VIEW = 1;
	
	private NotificationManager notificationManager;
//	private LayoutInflater layoutInflater;

	private AppShuttleApplication cxt = AppShuttleApplication.getContext();

	private static NotiBarNotifier notifier = new NotiBarNotifier();
	private NotiBarNotifier(){
		notificationManager = (NotificationManager)cxt.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	public static NotiBarNotifier getInstance(){
		return notifier;
	}
	
	public void updateNotification() {
		if(AppShuttlePreferences.isSleepMode())
			hideNotibar();
		else
			updateNotibar();
	}
	
	public void updateNotibar() {
		List<ViewableUserBhv> viewableUserBhvList = new ArrayList<ViewableUserBhv>();
		
		List<FavoriteBhv> notifiableFavoriteBhvList = FavoriteBhvManager.getInstance().getNotifiableFavoriteBhvList();
		viewableUserBhvList.addAll(notifiableFavoriteBhvList.subList(0, getNumFavoriteElem()));
		viewableUserBhvList.addAll(PresentBhv.getPresentBhvListFilteredSorted(getNumPresentElem()));
		viewableUserBhvList.addAll(PresentBhv.getPresentBhvListFilteredSorted(getNumPresentElem()));
		
		if(AppShuttleApplication.lastNotibarBhvs != null
				&& viewableUserBhvList.equals(AppShuttleApplication.lastNotibarBhvs)) {
			//skip update
			Log.d("test", "skip update");
		} else {
			Notification noti;
			RemoteViews notiView = createNotiRemoteViews(viewableUserBhvList);
				noti = new NotificationCompat.Builder(cxt)
					.setSmallIcon(R.drawable.appshuttle)
					.setContent(notiView)
					.setOngoing(true)
					.setPriority((AppShuttlePreferences.isSystemAreaIconHidden()) ? Notification.PRIORITY_MIN : Notification.PRIORITY_MAX)
					.build();
			notificationManager.notify(UPDATE_NOTI_VIEW, noti);
			AppShuttleApplication.lastNotibarBhvs = viewableUserBhvList;
			Log.d("test", "update noti view");
		}
		
//		Log.d("notifier", "notibar updated.");
	}
	
	public int getNumElem() {
//		int maxNumElem = cxt.getPreferences().getInt("viewer.noti.max_num", 24);
		int NotibarIconAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_icon_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		int NotibarBhvAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_bhv_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		return (getNotibarWidth() - NotibarIconAreaWidth) / NotibarBhvAreaWidth;
	}
	
	public int getNumFavoriteElem() {
		return Math.min(FavoriteBhvManager.getInstance().getNotifiableFavoriteBhvList().size(), getNumElem());
	}
	
	public int getNumPresentElem() {
		return getNumElem() - getNumFavoriteElem();
	}
	
	public void hideNotibar() {
		notificationManager.cancel(UPDATE_NOTI_VIEW);
		AppShuttleApplication.lastNotibarBhvs = null;
	}
	
	private <T extends UserBhv & Viewable> RemoteViews createNotiRemoteViews(List<T> viewableUserBhvList) {
		RemoteViews notiRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar);

		//clean
		notiRemoteView.removeAllViews(R.id.noti_favorite_container);
		notiRemoteView.removeAllViews(R.id.noti_present_container);
		
		notiRemoteView.setOnClickPendingIntent(R.id.noti_icon, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));

		if(viewableUserBhvList.isEmpty()){
			RemoteViews noResultRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar_no_result);
			notiRemoteView.addView(R.id.noti_present_container, noResultRemoteView);
			notiRemoteView.setOnClickPendingIntent(R.id.noti_present_container, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));
			return notiRemoteView;
		}
		
		for(T viewableUserBhv : viewableUserBhvList) {
			UserBhvType bhvType = viewableUserBhv.getBhvType();
			RemoteViews notiElemRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar_element);
			
			Intent intent = viewableUserBhv.getLaunchIntent();
			if(intent != null){
				PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 0, intent, 0);
				if(pendingIntent != null)
					notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, pendingIntent);
			}

			BitmapDrawable iconDrawable = (BitmapDrawable)viewableUserBhv.getIcon();
			notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_icon, iconDrawable.getBitmap());
			
			if (bhvType == UserBhvType.CALL){
//				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemRemoteView.setTextViewText(R.id.noti_elem_text, viewableUserBhv.getBhvNameText());

				float textSize = cxt.getResources().getDimension(R.dimen.notibar_text_size);
				final int sdkVersion = android.os.Build.VERSION.SDK_INT;
				if(sdkVersion < Build.VERSION_CODES.JELLY_BEAN) {
					notiElemRemoteView.setFloat(R.id.noti_elem_text, "setTextSize", textSize);
				} else {
					notiElemRemoteView.setTextViewTextSize(R.id.noti_elem_text, 
							TypedValue.COMPLEX_UNIT_PX, 
							textSize);
				}
			}
			
			Integer notibarContainerId = viewableUserBhv.getNotibarContainerId();
			if(notibarContainerId == null)
				continue;
			
			notiRemoteView.addView(notibarContainerId, notiElemRemoteView);

			int predictedBhvListSize = PresentBhv.getPresentBhvListFilteredSorted(Integer.MAX_VALUE).size();
			int extraNum = predictedBhvListSize - getNumPresentElem();
			if(extraNum > 0) {
				notiRemoteView.setViewVisibility(R.id.noti_icon, View.GONE);
				notiRemoteView.setTextViewText(R.id.noti_extra_num, "+" + extraNum);
				notiRemoteView.setViewVisibility(R.id.noti_extra_num, View.VISIBLE);
				notiRemoteView.setOnClickPendingIntent(R.id.noti_extra_num, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));
			}
		}

		return notiRemoteView;
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

// FIXME: 노티바 사용 통계 대책
//private void trackLaunchingBhv(ViewableUserBhv viewableUserBhv) {
//	/*
//	 * 실행하고자 하는 앱 이름을 putExtra 에 넣어서
//	 * AppShuttleMainService 에 전달.
//	 */
//	Intent intent = new Intent(cxt, AppShuttleMainActivity.class);
//	Bundle extras = new Bundle();
//	extras.putBoolean("doExec", true);
//	extras.putSerializable("bhvType", viewableUserBhv.getBhvType());
//	extras.putString("bhvName", viewableUserBhv.getBhvName());
//	intent.putExtras(extras);
//	intent.setAction(Long.toString(System.currentTimeMillis()));		// (1)
//
//	/* (1)에 대한 추가 설명
//	 * 똑같은 Activity.class에 대해 extra 값만 다른 여러 intent를 만드는 건데,
//	 * 첫 intent는 전달이 제대로 되지만, 그 다음 intent는 동작이 되지 않음.
//	 * 그에 대한 해결책으로 매 intent마다 setAction을 다르게 주면 되는 듯.
//	 * 
//	 * 참고 링크:
//	 * http://stackoverflow.com/questions/3168484/pendingintent-works-correctly-for-the-first-notification-but-incorrectly-for-the
//	 */
//	
//	if(intent != null){
//		/* 인자 설명:
//		 * request ID. 
//		 * request ID 가 달라야, 같은 AppShuttleMainService 로 가는 intent 끼리 서로 다른 걸로 구분이 됨.
//		 * 다 0으로 하면 마지막 intent에 설정한 extra 값이 모든 애들에게 적용되어 버림.
//		 * 
//		 * FLAG.
//		 * 뒤에 저 플래그 안 달면, 기존의 extra 값을 그대로 쓰게 됨.
//		 * 예를 들면, 처음 1번 위치에 prediction 한 애의 extra가 게속 남아있음.
//		 * 그걸 방지하기 위해 업데이트를 하도록 설정.
//		 */
//		int intent_id = viewableUserBhvList.indexOf(viewableUserBhv) + 1;
//		PendingIntent pendingIntent = PendingIntent.getActivity(cxt, intent_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		// Log.i("Noti", "Intent added " + intent_id);
//		
//		if(pendingIntent != null)
//			notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, pendingIntent);
//	}
//}

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

