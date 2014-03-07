package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
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

public class BlockedBhvFragment extends ListFragment {
	private BlockedBhvInfoAdapter adapter;
	private ActionMode actionMode;
	private List<BlockedBhv> blockedBhvList;
	
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
		
		setEmptyText(getResources().getString(R.string.msg_manual_blocked));

		blockedBhvList = new ArrayList<BlockedBhv>(BlockedBhvManager.getInstance().getBlockedBhvListSorted());
		
		adapter = new BlockedBhvInfoAdapter();
		setListAdapter(adapter);

		setListShown(true);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	    		if(actionMode == null) {
	    			actionMode = getActivity().startActionMode(actionCallback);
	    			actionMode.setTag(position);
//	    			actionMode.setTitle();
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

	public class BlockedBhvInfoAdapter extends ArrayAdapter<BlockedBhv> {

		public BlockedBhvInfoAdapter() {
			super(getActivity(), R.layout.listview_item, blockedBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			BlockedBhv blockedUserBhv = blockedBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_image);
			iconView.setImageDrawable(blockedUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstline);
			firstLineView.setText(blockedUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondline);
			secondLineView.setText(blockedUserBhv.getViewMsg());

			return itemView;
		}
	}
	
	ActionMode.Callback actionCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.blocked_actionmode, menu);
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
			int pos = (Integer)mode.getTag();

			if(blockedBhvList.size() < pos + 1)
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
		BlockedBhv blockedUserBhv = blockedBhvList.get(pos);
		switch(itemId) {
		case R.id.unblock:
			BlockedBhvManager.getInstance().unblock(blockedUserBhv);
			return blockedUserBhv.getBhvNameText() + getResources().getString(R.string.action_msg_unblock);
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