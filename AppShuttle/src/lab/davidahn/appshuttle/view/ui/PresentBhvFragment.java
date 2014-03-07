package lab.davidahn.appshuttle.view.ui;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.PresentBhv;
import lab.davidahn.appshuttle.view.ViewService;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PresentBhvFragment extends ListFragment {
	private PresentBhvAdapter adapter;
	private ActionMode actionMode;
	private List<PresentBhv> presentBhvList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(android.R.layout.list_content, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(System.currentTimeMillis() - AppShuttleApplication.launchTime < 3000)
			setEmptyText(getResources().getString(R.string.msg_wait));
		else
			setEmptyText(getResources().getString(R.string.msg_no_results));
		
		presentBhvList = PresentBhv.getPresentBhvListFilteredSorted(Integer.MAX_VALUE);
		
		adapter = new PresentBhvAdapter();
		setListAdapter(adapter);

		if (presentBhvList == null) {
			setListShown(false);
		} else {
			setListShown(true);
		}
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	    		if(actionMode == null) {
	    			actionMode = getActivity().startActionMode(actionCallback);
	    			actionMode.setTag(position);
//	    			actionMode.setTitle("")
//	    			AppShuttleMainActivity.doEmphasisChildViewInListView(getListView(), position);
	    			
	    		}
	            return true;
	        }
	    });
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = adapter.getItem(position).getLaunchIntent();
		if(intent == null)
			return;

		StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		getActivity().startActivity(intent);
	}
	
	public class PresentBhvAdapter extends ArrayAdapter<PresentBhv> {

		public PresentBhvAdapter() {
			super(getActivity(), R.layout.listview_item, presentBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			PresentBhv presentBhv = presentBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_image);
			iconView.setImageDrawable(presentBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstline);
			firstLineView.setText(presentBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondline);
			secondLineView.setText(presentBhv.getViewMsg());

			return itemView;
		}
	}
	
	ActionMode.Callback actionCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.present_actionmode, menu);
			return true;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
//			AppShuttleMainActivity.cancelEmphasisInListView(getListView());
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int pos = (Integer)actionMode.getTag();

			if(presentBhvList.size() < pos + 1)
				return false;

			String actionMsg = doActionAndGetMsg(pos, item.getItemId());
			doPostAction();
			showToastMsg(actionMsg);
			
			if(actionMode != null)
				actionMode.finish();
			
			return true;
		}
	};
	
	private String doActionAndGetMsg(int pos, int itemId) {
		PresentBhv presentBhv = presentBhvList.get(pos);
		switch(itemId) {
		case R.id.favorate:	
			FavoriteBhvManager.getInstance().favorite(presentBhv);
			return presentBhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite);
		case R.id.block:
			BlockedBhvManager.getInstance().block(presentBhv);
			return presentBhv.getBhvNameText() + getResources().getString(R.string.action_msg_block);
		default:
			return null;
		}
	}

	private void doPostAction() {
		getActivity().startService(new Intent(getActivity(), ViewService.class));
	}
	
	private void showToastMsg(String actionMsg){
		if(actionMsg == null)
			return ;
		
		Toast t = Toast.makeText(getActivity(), actionMsg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}