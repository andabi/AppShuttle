package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchableActivity extends Activity implements OnItemClickListener {
	private SearchListAdapter adapter;
	private List<ViewableUserBhv> bhvList;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.searchlayout);
	    
	    bhvList = new ArrayList<ViewableUserBhv>();
	    
	    List<UserBhv> appUserBhvList = AppBhvCollector.getInstance().getInstalledAppList(false);
	    for(UserBhv bhv : appUserBhvList)
	    	bhvList.add(new ViewableUserBhv(bhv));
	    
	    List<UserBhv> callUserBhvList = CallBhvCollector.getContactList();
	    for(UserBhv bhv : callUserBhvList)
	    	bhvList.add(new ViewableUserBhv(bhv));
	    
	    onSearchRequested();
	    mListView.setOnItemClickListener(this);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }
	
	private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
//            Intent wordIntent = new Intent(this, WordActivity.class);
//            wordIntent.setData(intent.getData());
//            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            //showResults(query);
        }
    }
	
	@Override
	public boolean onSearchRequested() {
		//pauseSomeStuff();
	    mListView = (ListView) findViewById(R.id.list_search_results);
	    adapter = new SearchListAdapter(this, R.layout.listview_contact_item, bhvList);
	    mListView.setAdapter(adapter);
		
		return super.onSearchRequested();
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View v, int pos, long id) {
		Message msg = new Message();
		msg.what = AppShuttleMainActivity.ACTION_FAVORITE;
		msg.obj = bhvList.get(pos);
		AppShuttleMainActivity.userActionHandler.sendMessage(msg);
		finish();
	}	

	
/*	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable_actionmode, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
    
        return true;
    }*/
	
	public class SearchListAdapter extends ArrayAdapter<ViewableUserBhv> {
		private List<ViewableUserBhv> items;
		
		public SearchListAdapter(Context context, int textViewResourceId, List<ViewableUserBhv> items) 
        {
            super(context, textViewResourceId, items);
            this.items = items;
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_contact_item, parent, false);
			ViewableUserBhv contact = this.items.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_contact_item_image);
			iconView.setImageDrawable(contact.getIcon());
			
			TextView nameView = (TextView) itemView.findViewById(R.id.listview_contact_item_name);
			nameView.setText(contact.getUserBhv().getBhvName());

			return itemView;
		}
	}
}
