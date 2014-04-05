package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainService;
import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.predict.PredictionService;
import lab.davidahn.appshuttle.report.Share;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		bar.setStackedBackgroundDrawable(new ColorDrawable(Color
				.rgb(48, 48, 48)));
		bar.setDisplayUseLogoEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		Bundle bundle = new Bundle();
		bundle.putString("tag", "predicted");
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.present),
				PresentBhvFragment.class, bundle);
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.favorite),
				FavoriteBhvFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ignore),
				BlockedBhvFragment.class, null);
		
		if(AppShuttleApplication.getContext().getPreferences().getBoolean("mode.debug", false)){
			mTabsAdapter.addTab(bar.newTab()
					.setIcon(R.drawable.info),
					InfoFragment.class, null);
		}
		
		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		bar.setTitle(getActionbarTitle(this, bar.getSelectedNavigationIndex()));

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

	public static CharSequence getActionbarTitle(Context cxt, int position) {
		String title;
		switch (position) {
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
			title = "";
		}
		return title;
	}

	public String doActionAndGetMsg(ViewableUserBhv bhv, int itemId) {
		switch(itemId) {
		case R.id.listview_present_menu_favorite:
			FavoriteBhvManager.getInstance().favorite(bhv);
			return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite);
		case R.id.listview_present_menu_ignore:
			BlockedBhvManager.getInstance().block(bhv);
			return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_ignore);
		case R.id.listview_present_menu_share:
			String status;
			switch(bhv.getViewableBhvType()){
			case PREDICTED:
				status = bhv.getViewMsg();
				break;
			case FAVORITE:
				status = getResources().getString(R.string.action_favorite);
				break;
			default:
				status = null;
			}
			
			String link;
			switch(bhv.getBhvType()){
			case APP:
				link = "https://play.google.com/store/apps/details?id=" + bhv.getBhvName();
				break;
			case CALL:
				link = bhv.getBhvName();
				break;
			default :
				link = null;
			}
			
			Share.shareTextPlain(this, 
				"["+ getResources().getString(R.string.name) +"]",
				getMsgShareByCountry(bhv.getBhvNameText(), status, link));
			return null;
		case R.id.listview_favorite_menu_unfavorite:
			FavoriteBhvManager.getInstance().unfavorite((FavoriteBhv)bhv);
			return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_unfavorite);
		case R.id.listview_ignore_menu_unignore:
			BlockedBhvManager.getInstance().unblock((BlockedBhv)bhv);
			return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_unignore);
		case R.id.favorite_notify:
			FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
			boolean isSuccess = favoriteBhvManager.trySetNotifiable((FavoriteBhv)bhv);
			if(isSuccess){
				String msg = bhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_notifiable);
				if(favoriteBhvManager.isFullProperNumFavorite()){
					msg += " " 
							+ favoriteBhvManager.getProperNumFavorite() 
							+ getResources().getString(R.string.action_msg_favorite_notifiable_num_proper);
				}
				return msg;
			} else {
				return getResources().getString(R.string.action_msg_favorite_notifiable_failure);
			}
		case R.id.favorite_unnotify:
			FavoriteBhvManager.getInstance().setUnNotifiable((FavoriteBhv)bhv);
			return bhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_unnotifiable);
		default:
			return null;
		}
	}
	
	private String getMsgShareByCountry(String name, String status, String link) {
		//TODO: i13n
		String text = "친구가 ";
		if(status != null)
			text += status + "한 ";
		text += name + "을(를) 셔틀합니다.";
		if(link != null)
			text += "\n" + link;
		return text;
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

		public View onCreateActionView() {
			LayoutInflater inflator = LayoutInflater.from(cxt);
			View layout = inflator.inflate(R.layout.actionbarlayout, null);

			ImageView refresh = (ImageView) layout.findViewById(R.id.refresh);
			refresh.setOnClickListener(new ImageView.OnClickListener() {
				public void onClick(View v) {
					cxt.sendBroadcast(new Intent().setAction(
							PredictionService.PREDICT)
							.putExtra("isForce", true));
				}
			});

			ImageView preferences = (ImageView) layout
					.findViewById(R.id.settings);
			preferences.setOnClickListener(new ImageView.OnClickListener() {
				public void onClick(View v) {
					cxt.startActivity(new Intent(cxt, SettingsActivity.class));
				}
			});
			
			return layout;
		}
	}

	BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			updateView();
		}
	};

	BroadcastReceiver progressVisibilityReceiver = new BroadcastReceiver() {
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
}