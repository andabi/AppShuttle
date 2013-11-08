package lab.davidahn.appshuttle.view;

import java.util.ArrayList;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainService;
import lab.davidahn.appshuttle.AppShuttleSettings;
import lab.davidahn.appshuttle.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.bugsense.trace.BugSenseHandler;

public class AppShuttleMainActivity extends Activity {
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	
	BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
    		mTabsAdapter.notifyDataSetChanged();
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!AppShuttleApplication.getContext().getPreferences().getBoolean("mode.debug", false))
			BugSenseHandler.initAndStartSession(this, "a3573081");

		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);
		AppShuttleSettings.setDefaultPreferences();

		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter();
		filter.addAction("lab.davidahn.appshuttle.REFRESH");
		registerReceiver(refreshReceiver, filter);
		
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);
		
		setContentView(mViewPager);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		Bundle bundle = new Bundle();
		bundle.putString("tag", "predicted");
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_menu_emoticons),
				OrdinaryBhvFragment.class, bundle);
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_menu_star),
				FavoratesBhvFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setIcon(android.R.drawable.ic_menu_delete),
				BlockedBhvFragment.class, null);
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_sysbar_quicksettings),
				SettingsFragment.class, null);

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		
		startService(new Intent(this, AppShuttleMainService.class));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}
	
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
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
		
//	  public void replace(int position, Fragment fragment) {
//	      // Get currently active fragment.
//	      Fragment old_fragment = getItem(position);
//	      if (old_fragment == null) {
//	        return;
//	      }
//
//	      // Replace the fragment using transaction and in underlaying array list.
//	      // NOTE .addToBackStack(null) doesn't work
//	      startUpdate(mViewPager);
//	      mFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//	        .remove(old_fragment).add(mViewPager.getId(), fragment)
//	        .commit();
//	      mFragments.set(position, fragment);
//	      notifyDataSetChanged();
//	      finishUpdate(mViewPager);
//	    }

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
}