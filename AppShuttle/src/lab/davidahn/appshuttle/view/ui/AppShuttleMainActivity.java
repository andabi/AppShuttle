package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainService;
import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.report.ShareUtils;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ActionProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;

public class AppShuttleMainActivity extends Activity {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	public static final int TAB_ICON_SIZE_DP = 20;
	public static UserActionHandler userActionHandler;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userActionHandler = new UserActionHandler();
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		AppShuttlePreferences.setDefaultPreferences();

		if (!AppShuttleApplication.getContext().getPreferences()
				.getBoolean("mode.debug", false))
			BugSenseHandler.initAndStartSession(this, "a3573081");

		/* Intent를 받아서 doExec 이면 해당 앱을 실행시킨다.
		 * 
		 * onNewIntent(~) 핸들러에서만 동작시키면, 앱셔틀이 메모리 부족으로 킬 됐을때
		 * 첫 인텐트가 제대로 동작하지 않음. (앱셔틀 켜지기만 하고 해당 앱이 실행이 안 됨)
		 * 
		 * 즉, 앱셔틀이 꺼졌다 켜질 때는 onCreate() 가 처리하고,
		 * 이미 켜져있는 상태에서는 onNewIntent() 가 처리함.
		 */
//		Intent intent = getIntent();
//		if (intent != null) {
//			Log.i("MainActivity", "onCreate:" + intent.toString());
//			handleExecutionIntent(intent);
//		}

		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter();
		filter.addAction(AppShuttleMainActivity.UPDATE_ACTIVITY);
		registerReceiver(updateReceiver, filter);

		filter = new IntentFilter();
		filter.addAction(AppShuttleMainActivity.PROGRESS_VISIBILITY);
		registerReceiver(progressVisibilityReceiver, filter);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);

		setContentView(mViewPager);

		final ActionBar bar = getActionBar();
		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));
		bar.setDisplayHomeAsUpEnabled(false);
	    bar.setDisplayShowCustomEnabled(false);
	    bar.setDisplayShowTitleEnabled(true);
	    bar.setDisplayUseLogoEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		Bundle bundle = new Bundle();
		bundle.putString("tag", "predicted");
		mTabsAdapter.addTab(bar.newTab().setIcon(getIcon(this, 0, TAB_ICON_SIZE_DP)),
				PresentBhvFragment.class, bundle);
		mTabsAdapter.addTab(bar.newTab().setIcon(getIcon(this, 1, TAB_ICON_SIZE_DP)),
				FavoriteBhvFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setIcon(getIcon(this, 2, TAB_ICON_SIZE_DP)),
				BlockedBhvFragment.class, null);
		
//		if(AppShuttleApplication.getContext().getPreferences().getBoolean("mode.debug", false)){
//			mTabsAdapter.addTab(bar.newTab()
//					.setIcon(R.drawable.info),
//					InfoFragment.class, null);
//		}
		
		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
		int selectedTabIndex = bar.getSelectedNavigationIndex();
		bar.setTitle(getActionbarTitle(this, selectedTabIndex));
		bar.getSelectedTab().setIcon(getIconSelected(this, selectedTabIndex, TAB_ICON_SIZE_DP));
		
		startService(new Intent(this, AppShuttleMainService.class));
		// sendBroadcast(new Intent().setAction(AppShuttleApplication.PREDICT));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public void onStart() {
		super.onStart();
		// FIXME: Is this okay?
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(updateReceiver);
		unregisterReceiver(progressVisibilityReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbarmenu, menu);

		return true;
	}

	public void updateView(){
		mTabsAdapter.notifyDataSetChanged();
	}
	
//	@Override
//	public void onNewIntent(Intent intent){
//		if (intent != null) {
//			Log.i("MainActivity", "onNewIntent:" + intent.toString());
//			handleExecutionIntent(intent);
//		}
//	}
//
//	private void handleExecutionIntent(Intent intent){
//		if (intent == null)
//			return;
//		
//		Bundle b = intent.getExtras();
//		if (b == null || (b.getBoolean("doExec", false) == false))
//			return;
//		
//		/* 이 아래의 코드들은 doExec 이 true일 때만 사용됨 */
//		UserBhvType bhvType = UserBhvType.NONE;
//		String bhvName = b.getString("bhvName");
//		if (b.containsKey("bhvType"))
//			bhvType = (UserBhvType)b.getSerializable("bhvType");
//		
//		if (bhvType == UserBhvType.NONE || bhvName == null)
//			return;
//		
//		BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
//		
//		Log.i("MainActivity", "Exec req " + uBhv.toString());
//	
//		StatCollector.getInstance().notifyBhvTransition(uBhv, true);	// 통계 데이터 전송
//		
//		Intent launchIntent = uBhv.getLaunchIntent();
//		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 전화 bhv 띄우기 위해서 필요
//		this.startActivity(launchIntent);
//	}

	public static CharSequence getActionbarTitle(Context cxt, int index) {
		String title = "";
		switch (index) {
		case 0:
			title = cxt.getResources().getString(
					R.string.actionbar_tab_text_present);
			break;
		case 1:
			title = cxt.getResources().getString(
					R.string.actionbar_tab_text_favorite);
			break;
		case 2:
			title = cxt.getResources().getString(
					R.string.actionbar_tab_text_ignore);
			break;
		default:
		}
		return title;
	}
	
	public static Drawable getIcon(Context cxt, int index, int size) {
		int iconId = R.drawable.info;
		switch (index) {
		case 0:
			iconId = R.drawable.present;
			break;
		case 1:
			iconId = R.drawable.favorite;
			break;
		case 2:
			iconId = R.drawable.ignore;
			break;
		default:
		}
		
		Drawable drawable = cxt.getResources().getDrawable(iconId);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		float density = cxt.getResources().getDisplayMetrics().density;
		size *= density;
		Drawable d = new BitmapDrawable(cxt.getResources(), Bitmap.createScaledBitmap(bitmap, size, size, true));
		return d;
	}

	public static Drawable getIconSelected(Context cxt, int selectedIndex, int size) {
		int iconId = R.drawable.ic_launcher;
		switch (selectedIndex) {
		case 0:
			iconId = R.drawable.present_on;
			break;
		case 1:
			iconId = R.drawable.favorite_on;
			break;
		case 2:
			iconId = R.drawable.ignore_on;
			break;
		default:
		}
		
		Drawable drawable = cxt.getResources().getDrawable(iconId);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		float density = cxt.getResources().getDisplayMetrics().density;
		size *= density;
		Drawable d = new BitmapDrawable(cxt.getResources(), Bitmap.createScaledBitmap(bitmap, size, size, true));
		return d;
	}
	
	public void doPostAction() {
		updateView();
		NotiBarNotifier.getInstance().updateNotification();
	}
	
	public void showToastMsg(String actionMsg){
		if(actionMsg == null)
			return ;
		
		Toast t = Toast.makeText(this, actionMsg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(Activity activity, ViewPager pager) {
			super(activity.getFragmentManager());
			mContext = activity;
			mActionBar = activity.getActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void hideTabs() {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		public void showTabs() {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
			mActionBar.setTitle(getActionbarTitle(mContext, position));
			switchOnIconForSelectedTab(position);
		}
		
		private void switchOnIconForSelectedTab(int selectedPos){
			for(int pos = 0; pos < mActionBar.getTabCount(); pos++) {
				if(selectedPos == pos)
					mActionBar.getTabAt(pos).setIcon(getIconSelected(mContext, pos, TAB_ICON_SIZE_DP));
				else
					mActionBar.getTabAt(pos).setIcon(getIcon(mContext, pos, TAB_ICON_SIZE_DP));
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

	public static class AppShuttleActionProvider extends ActionProvider {
		Context cxt;

		public AppShuttleActionProvider(Context _cxt) {
			super(_cxt);
			cxt = _cxt;
		}

		@Override
		public View onCreateActionView() {
			LayoutInflater inflator = LayoutInflater.from(cxt);
			View layout = inflator.inflate(R.layout.actionbarlayout, null);

			ImageView refresh = (ImageView) layout.findViewById(R.id.refresh);
			refresh.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View v) {
					cxt.sendBroadcast(new Intent().setAction(
							PredictionService.PREDICT)
							.putExtra("isForce", true));
				}
			});

			ImageView settings = (ImageView) layout
					.findViewById(R.id.settings);
			settings.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View v) {
					cxt.startActivity(new Intent(cxt, SettingsActivity.class));
				}
			});
			
			ImageView share = (ImageView) layout
					.findViewById(R.id.share);
			share.setOnClickListener(new ImageView.OnClickListener() {
				@Override
				public void onClick(View v) {
					String subject = cxt.getResources().getString(R.string.name);
					String text = cxt.getResources().getString(R.string.share_msg);
	    			ShareUtils.shareTextPlain(cxt, subject, text);
				}
			});
			
			return layout;
		}
	}

	BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateView();
		}
	};

	BroadcastReceiver progressVisibilityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isOn = intent.getBooleanExtra("isOn", false);

			ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
			if (progress != null)
				progress.setVisibility((isOn) ? View.VISIBLE : View.INVISIBLE);

			ImageView refresh = (ImageView) findViewById(R.id.refresh);
			if (refresh != null)
				refresh.setVisibility((isOn) ? View.INVISIBLE : View.VISIBLE);
		}
	};
	public static final String UPDATE_ACTIVITY = "lab.davidahn.appshuttle.UPDATE_VIEW_ACTIVITY";
	public static final String PROGRESS_VISIBILITY = "lab.davidahn.appshuttle.PROGRESS_VISIBILITY";
	
	public static final int ACTION_FAVORITE = 0;
	public static final int ACTION_UNFAVORITE = 1;
	public static final int ACTION_IGNORE = 2;
	public static final int ACTION_UNIGNORE = 3;
	public static final int ACTION_SHARE = 4;
	public static final int ACTION_FAVORITE_UPDATE_ORDER = 5;
	
	public class UserActionHandler extends Handler {
		@Override
        public void handleMessage(Message msg) {
        	ViewableUserBhv bhv = (ViewableUserBhv)msg.obj;
        	String actionMsg = "";
            switch (msg.what) {
	    		case ACTION_FAVORITE:
	    			UserBhvManager.getInstance().registerIfNotExist(bhv.getUserBhv());
	    			FavoriteBhv favoriteBhv = FavoriteBhvManager.getInstance().favorite(bhv);
	    			
	    			if(bhv instanceof BlockedBhv)
	    				BlockedBhvManager.getInstance().unblock((BlockedBhv)bhv);
	    			
	    			actionMsg = favoriteBhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite);
	    			doPostAction();
	    			showToastMsg(actionMsg);
	    			break;
	    		case ACTION_UNFAVORITE:
	    			FavoriteBhvManager.getInstance().unfavorite((FavoriteBhv)bhv);
	    			actionMsg = ((FavoriteBhv)bhv).getBhvNameText() + getResources().getString(R.string.action_msg_unfavorite);
	    			doPostAction();
	    			showToastMsg(actionMsg);
	    			break;
	    		case ACTION_FAVORITE_UPDATE_ORDER:
	    			FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
	    			favoriteBhvManager.updateOrder((FavoriteBhv)bhv, msg.arg1);
	    			break;
	    		case ACTION_IGNORE:
	    			UserBhvManager.getInstance().registerIfNotExist(bhv.getUserBhv());
	    			BlockedBhv blockedBhv = BlockedBhvManager.getInstance().block(bhv);
	    			
	    			if(bhv instanceof FavoriteBhv)
	    				FavoriteBhvManager.getInstance().unfavorite((FavoriteBhv)bhv);
	    			
	    			actionMsg =  blockedBhv.getBhvNameText() + getResources().getString(R.string.action_msg_ignore);
	    			doPostAction();
	    			showToastMsg(actionMsg);
	    			break;
	    		case ACTION_UNIGNORE:
	    			BlockedBhvManager.getInstance().unblock((BlockedBhv)bhv);
	    			actionMsg = ((BlockedBhv)bhv).getBhvNameText() + getResources().getString(R.string.action_msg_unignore);
	    			doPostAction();
	    			showToastMsg(actionMsg);
	    			break;
	    		case ACTION_SHARE:
	    			ShareUtils.shareTextPlain(AppShuttleMainActivity.this, getResources().getString(R.string.name), bhv.getSharingMsg());
	    			break;
    			default:
            }
        }
    };
}

//case R.id.favorite_notify:
//FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
//boolean isSuccess = favoriteBhvManager.trySetNotifiable((FavoriteBhv)bhv);
//if(isSuccess){
//	String msg = bhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_notifiable);
//	if(favoriteBhvManager.isFullProperNumFavorite()){
//		msg += " " 
//				+ favoriteBhvManager.getProperNumFavorite() 
//				+ getResources().getString(R.string.action_msg_favorite_notifiable_num_proper);
//	}
//	return msg;
//} else {
//	return getResources().getString(R.string.action_msg_favorite_notifiable_failure);
//}
//case R.id.favorite_unnotify:
//FavoriteBhvManager.getInstance().setUnNotifiable((FavoriteBhv)bhv);
//return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_unnotifiable);


