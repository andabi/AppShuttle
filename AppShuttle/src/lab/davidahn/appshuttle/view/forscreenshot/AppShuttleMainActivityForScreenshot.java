package lab.davidahn.appshuttle.view.forscreenshot;

import java.util.ArrayList;

import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.view.SettingsActivity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

public class AppShuttleMainActivityForScreenshot extends Activity {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		AppShuttlePreferences.setDefaultPreferences();

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);
		
		setContentView(mViewPager);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		Bundle bundle = new Bundle(); 
		bundle.putString("tag", "predicted");
		mTabsAdapter.addTab(bar.newTab()
				.setText(" " + getResources().getString(R.string.actionbar_tab_text_predicted))
				.setIcon(R.drawable.predicted),
				PresentBhvFragmentForScreenshot.class, bundle);
		mTabsAdapter.addTab(bar.newTab()
				.setText(" " + getResources().getString(R.string.actionbar_tab_text_favorite))
				.setIcon(R.drawable.favorite),
				FavoriteBhvFragmentForScreenshot.class, null);
		mTabsAdapter.addTab(bar.newTab()
				.setText(" " + getResources().getString(R.string.actionbar_tab_text_blocked))
				.setIcon(R.drawable.ignore),
				BlockedBhvFragmentForScreenshot.class, null);

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
//		startService(new Intent(this, AppShuttleMainService.class));
		NotiBarNotifierForscreenshot.getInstance().doNotification();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbarmenu, menu);

		return true;
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
					cxt.sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PREDICT"));
				}
			});
			
			ImageView preferences = (ImageView)layout.findViewById(R.id.preferences);
			preferences.setOnClickListener(new ImageView.OnClickListener(){
				public void onClick(View v) {
					cxt.startActivity(new Intent(cxt, SettingsActivity.class));
				}
			});
			
			return layout;
		}
	}
}