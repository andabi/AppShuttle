package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainService;
import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.report.StatCollector;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;

public class AppShuttleMainActivity extends Activity {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	
    private void handleExecutionIntent(Intent intent){
		if (intent == null)
			return;
		
		Bundle b = intent.getExtras();
		if (b == null || (b.getBoolean("doExec", false) == false))
			return;
		
		/* 이 아래의 코드들은 doExec 이 true일 때만 사용됨 */
		UserBhvType bhvType = UserBhvType.NONE;
		String bhvName = b.getString("bhvName");
		if (b.containsKey("bhvType"))
			bhvType = (UserBhvType)b.getSerializable("bhvType");
		
		if (bhvType == UserBhvType.NONE || bhvName == null)
			return;
		
		BaseUserBhv uBhv = new BaseUserBhv(bhvType, bhvName);
		
		Log.i("MainActivity", "Exec req " + uBhv.toString());

		StatCollector.getInstance().notifyBhvTransition(uBhv, true);	// 통계 데이터 전송
		
		Intent launchIntent = uBhv.getLaunchIntent();
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 전화 bhv 띄우기 위해서 필요
		this.startActivity(launchIntent);
		
		//close notification panel (method 2)
		sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		
		//close notification panel (method 3)
//		Object sbservice = c.getSystemService( "statusbar" );
//      Class<?> statusbarManager = Class.forName( "android.app.StatusBarManager" );
//      Method hidesb = statusbarManager.getMethod( "collapse" );
//      hidesb.invoke( sbservice );
		//nedded permission
//		<uses-permission android:name="android.permission.EXPAND_STATUS_BAR"/>
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		AppShuttlePreferences.setDefaultPreferences();

		if(!AppShuttleApplication.getContext().getPreferences().getBoolean("mode.debug", false))
			BugSenseHandler.initAndStartSession(this, "a3573081");

		/* Intent를 받아서 doExec 이면 해당 앱을 실행시킨다.
		 * 
		 * onNewIntent(~) 핸들러에서만 동작시키면, 앱셔틀이 메모리 부족으로 킬 됐을때
		 * 첫 인텐트가 제대로 동작하지 않음. (앱셔틀 켜지기만 하고 해당 앱이 실행이 안 됨)
		 * 
		 * 즉, 앱셔틀이 꺼졌다 켜질 때는 onCreate() 가 처리하고,
		 * 이미 켜져있는 상태에서는 onNewIntent() 가 처리함.
		 */
		Intent intent = getIntent();
		if (intent != null) {
			Log.i("MainActivity", "onCreate:" + intent.toString());
			handleExecutionIntent(intent);
		}

		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.UPDATE_VIEW");
		registerReceiver(updateViewReceiver, filter);
		
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.PROGRESS_VISIBILITY");
		registerReceiver(progressVisibilityReceiver, filter);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);
		
		setContentView(mViewPager);

		final ActionBar bar = getActionBar();
		bar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		bar.setStackedBackgroundDrawable(new ColorDrawable(Color.rgb(48, 48, 48)));
		bar.setDisplayUseLogoEnabled(false);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		Bundle bundle = new Bundle(); 
		bundle.putString("tag", "predicted");
		mTabsAdapter.addTab(bar.newTab()
				.setIcon(R.drawable.predicted),
				PresentBhvFragment.class, bundle);
		mTabsAdapter.addTab(bar.newTab()
				.setIcon(R.drawable.favorite),
				FavoriteBhvFragment.class, null);
		mTabsAdapter.addTab(bar.newTab()
				.setIcon(R.drawable.ignore),
				BlockedBhvFragment.class, null);
		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		bar.setTitle(getActionbarTitle(this, bar.getSelectedNavigationIndex()));
		
		startService(new Intent(this, AppShuttleMainService.class));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PREDICT"));
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
		unregisterReceiver(updateViewReceiver);
		unregisterReceiver(progressVisibilityReceiver);
	}
	
	protected void updateView() {
		NotiBarNotifier.getInstance().updateNotification();
		mTabsAdapter.notifyDataSetChanged();
//		Log.d("view", "view updated.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbarmenu, menu);

		return true;
	}
	
	@Override
	public void onNewIntent(Intent intent){
		if (intent != null) {
			Log.i("MainActivity", "onNewIntent:" + intent.toString());
			handleExecutionIntent(intent);
		}
	}
	
	public static CharSequence getActionbarTitle(Context cxt, int position) {
		String title;
		switch(position){
		case 0:
			title = cxt.getResources().getString(R.string.actionbar_tab_text_present);
			break;
		case 1:
			title = cxt.getResources().getString(R.string.actionbar_tab_text_favorite);
			break;
		case 2:
			title = cxt.getResources().getString(R.string.actionbar_tab_text_blocked);
			break;
		default:
			title = "";
		}
		return title;
	}

	public static void doEmphasisChildViewInListView(ListView listview, int position){
		for(int i=0;i<listview.getChildCount();i++){
			if(i == position)
				listview.getChildAt(i).setAlpha(1);
			else
				listview.getChildAt(i).setAlpha(0.2f);
		}
	}

	public static void cancelEmphasisInListView(ListView listview){
		for(int i=0;i<listview.getChildCount();i++){
			listview.getChildAt(i).setAlpha(1);
		}
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
		
		public void hideTabs(){
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		public void showTabs(){
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
		
		public View onCreateActionView(){
			LayoutInflater inflator = LayoutInflater.from(cxt);
			View layout = inflator.inflate(R.layout.actionbarlayout, null);
			
			ImageView refresh = (ImageView)layout.findViewById(R.id.refresh);
			refresh.setOnClickListener(new ImageView.OnClickListener(){
				public void onClick(View v) {
					cxt.sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PREDICT").putExtra("isForce", true));
				}
			});
			
			ImageView preferences = (ImageView)layout.findViewById(R.id.settings);
			preferences.setOnClickListener(new ImageView.OnClickListener(){
				public void onClick(View v) {
					cxt.startActivity(new Intent(cxt, SettingsActivity.class));
				}
			});
			
			/* If debug mode is ON, add debug button */
			if(AppShuttleApplication.getContext().getPreferences().getBoolean("mode.debug", false)){
				ViewGroup actionbar_llayout = (ViewGroup)layout.findViewById(R.id.actionbar);
				
				ImageView debug_image = new ImageView(cxt);
				debug_image.setLayoutParams(layout.findViewById(R.id.settings).getLayoutParams());
				debug_image.setBackgroundResource(R.drawable.notifiable);	// TODO: New debug image resource is needed
				
				debug_image.setOnClickListener(new ImageView.OnClickListener(){
    				public void onClick(View v) {
    					// TODO: 디버그 정보를 위한 새로운 액티비티 생성
    					AlertDialog.Builder alert = new AlertDialog.Builder(cxt);
    					alert.setTitle("통계정보");
    					alert.setMessage(StatCollector.getInstance().toString());
    					alert.show();
					}
				});
				
				FrameLayout debug_frame = new FrameLayout(cxt);
				debug_frame.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				debug_frame.setLayoutParams(layout.findViewById(R.id.settings_frame).getLayoutParams());
				debug_frame.addView(debug_image);
				
				actionbar_llayout.addView(debug_frame);
			}
			
			return layout;
		}
	}

	BroadcastReceiver updateViewReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	updateView();
	    }
	};
	
	BroadcastReceiver progressVisibilityReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	boolean isOn = intent.getBooleanExtra("isOn", false);
	    	
			ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
			if(progress != null)
				progress.setVisibility((isOn) ? View.VISIBLE : View.INVISIBLE);
			
			ImageView refresh = (ImageView)findViewById(R.id.refresh);
			if(refresh != null)
				refresh.setVisibility((isOn) ? View.INVISIBLE : View.VISIBLE);
	    }
	};
}

//@Override
//public void onWindowFocusChanged(boolean hasFocus)
//{
//    try
//    {
//        if(!hasFocus)
//        {
//            Object service  = getSystemService("statusbar");
//            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
//            Method collapse = statusbarManager.getMethod("collapse");
//            collapse .setAccessible(true);
//            collapse .invoke(service);
//        }
//    }
//    catch(Exception ex)
//    {
//        if(!hasFocus)
//        {
//            try {
//                Object service  = getSystemService("statusbar");
//                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
//                Method collapse = statusbarManager.getMethod("collapse");
//                collapse .setAccessible(true);
//                collapse .invoke(service);
//
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();                
//            }
//            ex.printStackTrace();
//        }
//    }
//}