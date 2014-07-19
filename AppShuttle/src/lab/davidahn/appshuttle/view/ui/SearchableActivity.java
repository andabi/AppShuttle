package lab.davidahn.appshuttle.view.ui;

import static lab.davidahn.appshuttle.collect.bhv.BaseUserBhv.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.CandidateFavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchableActivity extends Activity implements OnItemClickListener, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Object> {
	private SearchListAdapter adapter;
	private List<CandidateFavoriteBhv> allBhvListSorted = new ArrayList<CandidateFavoriteBhv>();
	private List<CandidateFavoriteBhv> adapterBhvList = new ArrayList<CandidateFavoriteBhv>();
	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.add_view);
	    setProgressBarIndeterminateVisibility(true);
	    
	    mListView = (ListView) findViewById(R.id.list_search_results);
	    mListView.setOnItemClickListener(this);
	    adapter = new SearchListAdapter(this, R.layout.add_listview, adapterBhvList);
	    mListView.setAdapter(adapter);
	    
		allBhvListSorted = getAllCandidateFavoriteBhvListSorted();
	    updateCandidateFavoriteBhvListMatching(null);

//	    getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View v, int pos, long id) {
		Message msg = new Message();
		msg.what = AppShuttleMainActivity.ACTION_FAVORITE;
		msg.obj = adapterBhvList.get(pos);
		AppShuttleMainActivity.userActionHandler.sendMessage(msg);
		finish();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_actionmode, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        menu.findItem(R.id.search).setOnActionExpandListener(new OnActionExpandListener() {
			
        	@Override
        	public boolean onMenuItemActionCollapse(MenuItem menu) {
        		return updateCandidateFavoriteBhvListMatching(null);
        	}

        	@Override
        	public boolean onMenuItemActionExpand(MenuItem menu) {
        		return true;
        	}
    	});

        return true;
    }
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		return updateCandidateFavoriteBhvListMatching(query);
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		if(allBhvListSorted.size() <= 200)
			return updateCandidateFavoriteBhvListMatching(newText);
		else
			return false;
	}
	
	private boolean updateCandidateFavoriteBhvListMatching(String text){
		if(text == null || text.isEmpty()) {
			adapterBhvList = new ArrayList<CandidateFavoriteBhv>(allBhvListSorted);
		} else {
			adapterBhvList = getCandidateFavoriteBhvListMatchSorted(allBhvListSorted, text);
		}
		adapter.clear();
		adapter.addAll(adapterBhvList);
		adapter.notifyDataSetChanged();
		
		return true;
	}
	
	private List<CandidateFavoriteBhv> getCandidateFavoriteBhvListMatchSorted(List<CandidateFavoriteBhv> list, String text) {
		List<CandidateFavoriteBhv> res = new ArrayList<CandidateFavoriteBhv>();
		for(CandidateFavoriteBhv bhv : list) {
			if(bhv.getBhvNameText().toLowerCase().contains(text.toLowerCase()))
				res.add(bhv);
		}
	    Collections.sort(res, Collections.reverseOrder());
		return res;
	}
	
	private List<CandidateFavoriteBhv> getAllCandidateFavoriteBhvListSorted() {
		List<CandidateFavoriteBhv> res = new ArrayList<CandidateFavoriteBhv>();

	    AppBhvCollector appBhvCollector = AppBhvCollector.getInstance();
	    List<String> installedAppList = appBhvCollector.getInstalledAppList(false);
	    for(String packageName : installedAppList){
	    	UserBhv base = create(UserBhvType.APP, packageName);
	    	if(base.isValid())
		    	res.add(new CandidateFavoriteBhv(base,
		    			appBhvCollector.getLastUpdatedTime(packageName)));
	    }
	    
	    CallBhvCollector callBhvCollector = CallBhvCollector.getInstance();
	    List<String> phoneNumList = callBhvCollector.getContactList();
	    for(String phoneNum : phoneNumList){
	    	UserBhv base = create(UserBhvType.CALL, phoneNum);
	    	if(base.isValid())
		    	res.add(new CandidateFavoriteBhv(
		    			BaseUserBhv.create(UserBhvType.CALL, phoneNum),
		    			callBhvCollector.getLastCallTime(phoneNum)));
	    }
	    
	    Set<UserBhv> toBeFiltered = new HashSet<UserBhv>();
	    toBeFiltered.addAll(FavoriteBhvManager.getInstance().getFavoriteBhvSet());
	    toBeFiltered.addAll(BlockedBhvManager.getInstance().getBlockedBhvSet());
	    res.removeAll(toBeFiltered);
	    
	    Collections.sort(res, Collections.reverseOrder());

	    return res;
	}
	
	public class SearchListAdapter extends ArrayAdapter<CandidateFavoriteBhv> {
		private List<CandidateFavoriteBhv> items;
		
		public SearchListAdapter(Context context, int textViewResourceId, List<CandidateFavoriteBhv> items) 
        {
            super(context, textViewResourceId, items);
            this.items = items;
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.add_listview, parent, false);
			CandidateFavoriteBhv contact = this.items.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.add_listview_image);
			iconView.setImageDrawable(contact.getIcon());
			
			TextView textView = (TextView) itemView.findViewById(R.id.add_listview_text);
			textView.setText(contact.getBhvNameText());

//			TextView subTextView = (TextView) itemView.findViewById(R.id.add_listview_subtext);
//			subTextView.setText(contact.getViewMsg());

			return itemView;
		}
	}

	@Override
	public Loader<Object> onCreateLoader(int id, Bundle args) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Object> arg0, Object arg1) {}

	@Override
	public void onLoaderReset(Loader<Object> arg0) {}
}
