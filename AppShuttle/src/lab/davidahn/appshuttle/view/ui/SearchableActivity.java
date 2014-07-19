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
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchableActivity extends Activity implements OnItemClickListener, SearchView.OnQueryTextListener {
	private SearchListAdapter adapter;
	private List<CandidateFavoriteBhv> bhvList;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_view);
	    
	    bhvList = new ArrayList<CandidateFavoriteBhv>();

	    //TODO
	    AppBhvCollector appBhvCollector = AppBhvCollector.getInstance();
	    List<String> installedAppList = appBhvCollector.getInstalledAppList(false);
	    for(String packageName : installedAppList){
	    	UserBhv base = create(UserBhvType.APP, packageName);
	    	if(base.isValid())
		    	bhvList.add(new CandidateFavoriteBhv(base,
		    			appBhvCollector.getLastUpdatedTime(packageName)));
	    }
	    
	    CallBhvCollector callBhvCollector = CallBhvCollector.getInstance();
	    List<String> phoneNumList = callBhvCollector.getContactList();
	    for(String phoneNum : phoneNumList){
	    	UserBhv base = create(UserBhvType.CALL, phoneNum);
	    	if(base.isValid())
		    	bhvList.add(new CandidateFavoriteBhv(
		    			BaseUserBhv.create(UserBhvType.CALL, phoneNum),
		    			callBhvCollector.getLastCallTime(phoneNum)));
	    }
	    
	    Set<UserBhv> toBeFiltered = new HashSet<UserBhv>();
	    toBeFiltered.addAll(FavoriteBhvManager.getInstance().getFavoriteBhvSet());
	    toBeFiltered.addAll(BlockedBhvManager.getInstance().getBlockedBhvSet());
	    bhvList.removeAll(toBeFiltered);
	    
	    Collections.sort(bhvList, Collections.reverseOrder());
	    
	    mListView = (ListView) findViewById(R.id.list_search_results);
	    mListView.setOnItemClickListener(this);
	    adapter = new SearchListAdapter(this, R.layout.add_listview, bhvList);
	    mListView.setAdapter(adapter);
	}
	
//	@Override
//    protected void onNewIntent(Intent intent) {
//        handleIntent(intent);
//    }
//	
//	private void handleIntent(Intent intent) {
//        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
//            // handles a click on a search suggestion; launches activity to show word
////            Intent wordIntent = new Intent(this, WordActivity.class);
////            wordIntent.setData(intent.getData());
////            startActivity(wordIntent);
//        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            // handles a search query
//            String query = intent.getStringExtra(SearchManager.QUERY);
////            showResults(query);
//        }
//    }
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View v, int pos, long id) {
		Message msg = new Message();
		msg.what = AppShuttleMainActivity.ACTION_FAVORITE;
		msg.obj = bhvList.get(pos);
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

        return true;
    }
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		//TODO
		return true;
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		//TODO
		return true;
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
}
