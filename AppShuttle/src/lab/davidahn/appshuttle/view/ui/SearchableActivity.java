package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.CallBhvCollector;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchableActivity extends Activity {
	private SearchListAdapter adapter;
	private List<SearchListItem> searchList;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.searchlayout);
	    
	    searchList = new ArrayList<SearchListItem>();
	    
	    // get appsList
	    List<Pair<String, Drawable>> appInfos = AppBhvCollector.getInstalledAppList(false);
	    int size_appInfos = appInfos.size();
	    for (int i = 0; i < size_appInfos; i++) {
	    	Pair<String, Drawable> appInfo = appInfos.get(i);
	    	searchList.add(new SearchListItem(appInfo.getKey(), appInfo.getValue()));
	    }
	    
	    // get contactsList
	    List<Pair<String, Drawable>> contactInfos = CallBhvCollector.getContactList();
	    int size_contactInfos = contactInfos.size();
	    for (int i = 0; i < size_contactInfos; i++) {
	    	Pair<String, Drawable> contactInfo = contactInfos.get(i);
            searchList.add(new SearchListItem(contactInfo.getKey(), contactInfo.getValue()));
        }
	    
	    onSearchRequested();
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
	    adapter = new SearchListAdapter(this, R.layout.listview_contact_item, searchList);
	    mListView.setAdapter(adapter);
		
		return super.onSearchRequested();
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
	
	public class SearchListAdapter extends ArrayAdapter<SearchListItem> {
		private List<SearchListItem> items;
		
		public SearchListAdapter(Context context, int textViewResourceId, List<SearchListItem> items) 
        {
                super(context, textViewResourceId, items);
                this.items = items;
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_contact_item, parent, false);
			SearchListItem contact = this.items.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_contact_item_image);
			iconView.setImageDrawable(contact.getIcon());
			
			TextView nameView = (TextView) itemView.findViewById(R.id.listview_contact_item_name);
			nameView.setText(contact.getName());

			return itemView;
		}
	}
	
	class SearchListItem 
    {
        private String name;
        private Drawable icon;
         
        public SearchListItem(String _Name, Drawable _Icon)
        {
            this.name = _Name;
            this.icon = _Icon;
        }
         
        public String getName() 
        {
            return name;
        }
 
        public Drawable getIcon() 
        {
            return icon;
        }        
    }	
}
