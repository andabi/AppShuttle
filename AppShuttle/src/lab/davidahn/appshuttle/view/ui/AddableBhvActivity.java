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
import lab.davidahn.appshuttle.view.AddableBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class AddableBhvActivity extends Activity implements OnItemClickListener {
	private SearchListAdapter adapter;
	private List<AddableBhv> totalBhvListSorted = new ArrayList<AddableBhv>();
	private List<AddableBhv> adapterBhvList = new ArrayList<AddableBhv>();
	private LoadBhvList loadingBhvListTask = new LoadBhvList();
	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.addable_view);
	    TextView emptyText = (TextView)findViewById(android.R.id.empty);
	    mListView = (ListView) findViewById(android.R.id.list);
	    mListView.setOnItemClickListener(this);
	    adapter = new SearchListAdapter(this, R.layout.addable_listview, adapterBhvList);
	    mListView.setAdapter(adapter);
	    mListView.setEmptyView(emptyText);

	    loadingBhvListTask.execute();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parentView, View v, int pos, long id) {
		if (!adapterBhvList.get(pos).isValid())
			return;
		
		Message msg = new Message();
		msg.what = getIntent().getIntExtra("actionOnItemClick", -1);
		msg.obj = adapterBhvList.get(pos);
		AppShuttleMainActivity.userActionHandler.sendMessage(msg);
		finish();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_actionmode, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
        	@Override
        	public boolean onQueryTextSubmit(String query) {
        		loadingBhvListTask.cancel(true);
        	    loadingBhvListTask = new LoadBhvList();
        	    loadingBhvListTask.execute(query);
        	    return true;
        	}
        	
        	@Override
        	public boolean onQueryTextChange(String newText) {
        	    return false;
        	}
		});

        menu.findItem(R.id.search).setOnActionExpandListener(new OnActionExpandListener() {
			
        	@Override
        	public boolean onMenuItemActionCollapse(MenuItem menu) {
        		loadingBhvListTask.cancel(true);
        	    loadingBhvListTask = new LoadBhvList();
        	    loadingBhvListTask.execute();
        		return true;
        	}

        	@Override
        	public boolean onMenuItemActionExpand(MenuItem menu) {
        		return true;
        	}
    	});

        return true;
    }
	
	public class SearchListAdapter extends ArrayAdapter<AddableBhv> {
		private List<AddableBhv> items;
		
		public SearchListAdapter(Context context, int textViewResourceId, List<AddableBhv> items) 
        {
            super(context, textViewResourceId, items);
            this.items = items;
        }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View itemView = inflater.inflate(R.layout.addable_listview, parent, false);
			AddableBhv addableBhv = this.items.get(position);

			//info msg
			if (!addableBhv.isValid()) {
				View infoView = inflater.inflate(R.layout.listview_info_msg, parent, false);

				TextView infoSubject = (TextView) infoView.findViewById(R.id.listview_info_subject);
				infoSubject.setText(R.string.addable_empty_msg_subject);
				
				TextView infoText = (TextView) infoView.findViewById(R.id.listview_info_text);
				infoText.setText(R.string.addable_empty_msg_text);

				return infoView;
			}

			
			ImageView iconView = (ImageView) itemView.findViewById(R.id.add_listview_image);
			iconView.setImageDrawable(addableBhv.getIcon());
			
			TextView textView = (TextView) itemView.findViewById(R.id.add_listview_text);
			textView.setText(addableBhv.getBhvNameText());

			TextView subTextView = (TextView) itemView.findViewById(R.id.add_listview_subtext);
			subTextView.setText(addableBhv.getViewMsg());

			return itemView;
		}
	}

	public class LoadBhvList extends AsyncTask<String, Void, Void> {
	    ProgressDialog progressDialog;

	    @Override
	    protected void onPreExecute()
	    {
	        progressDialog = new ProgressDialog(AddableBhvActivity.this);
	        progressDialog.setMessage(getResources().getString(R.string.add_bhv_loading_msg));
	        progressDialog.setCancelable(false);
	        progressDialog.show();
	    };      
	    @Override
	    protected Void doInBackground(String... queries)
	    {   
	    	if(totalBhvListSorted.isEmpty())
	    		totalBhvListSorted = getTotalAddableBhvListSorted();
	    	
	    	String query = null;
	    	if(queries.length > 0)
	    		query = queries[0];
	    	
	    	updateAddableBhvListMatching(query);

	    	return null;
	    }       
	    @Override
	    protected void onPostExecute(Void result)
	    {
	        super.onPostExecute(result);
	        progressDialog.dismiss();

			// add dummy for info msg
			if(adapterBhvList.isEmpty())
				adapterBhvList.add(new AddableBhv(BaseUserBhv.create(UserBhvType.NONE, ""), 0));
	        
	        adapter.clear();
			adapter.addAll(adapterBhvList);
			adapter.notifyDataSetChanged();
	    };
	    
		private void updateAddableBhvListMatching(String text){
			if(text == null || text.isEmpty()) {
				adapterBhvList = new ArrayList<AddableBhv>(totalBhvListSorted);
			} else {
				adapterBhvList = getAddableBhvListMatchSorted(totalBhvListSorted, text);
			}
		}
		
		private List<AddableBhv> getAddableBhvListMatchSorted(List<AddableBhv> list, String text) {
			List<AddableBhv> res = new ArrayList<AddableBhv>();
			for(AddableBhv bhv : list) {
				if(bhv.getBhvNameText().toLowerCase().contains(text.toLowerCase()))
					res.add(bhv);
			}
		    Collections.sort(res, Collections.reverseOrder());
			return res;
		}
		
		private List<AddableBhv> getTotalAddableBhvListSorted() {
			List<AddableBhv> res = new ArrayList<AddableBhv>();

		    AppBhvCollector appBhvCollector = AppBhvCollector.getInstance();
		    List<String> installedAppList = appBhvCollector.getInstalledAppList(false);
		    for(String packageName : installedAppList){
		    	UserBhv base = create(UserBhvType.APP, packageName);
		    	if(base.isValid())
			    	res.add(new AddableBhv(base,
			    			appBhvCollector.getLastUpdatedTime(packageName)));
		    }
		    
		    CallBhvCollector callBhvCollector = CallBhvCollector.getInstance();
		    List<String> phoneNumList = callBhvCollector.getContactList();
		    for(String phoneNum : phoneNumList){
		    	UserBhv base = create(UserBhvType.CALL, phoneNum);
		    	if(base.isValid())
			    	res.add(new AddableBhv(
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
	 }
}
