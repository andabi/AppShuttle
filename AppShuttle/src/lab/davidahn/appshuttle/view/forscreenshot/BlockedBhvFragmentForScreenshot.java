package lab.davidahn.appshuttle.view.forscreenshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.NotiBarNotifier;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
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

public class BlockedBhvFragmentForScreenshot extends ListFragment {
	private BlockedBhvInfoAdapter adapter;
	private ActionMode actionMode;
	private List<ViewableUserBhv> blockedBhvList;
	
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
		
		setEmptyText(getResources().getString(R.string.blocked_empty_msg));

		blockedBhvList = MockObjectForScreenshot.getFakeViewableUserBhvList(this.getClass());
		
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
	    		}
	            return true;
	        }
	    });
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		getActivity().startActivity(adapter.getItem(position).getLaunchIntent());
	}

	public static List<BlockedBhv> getBlockedBhvListSorted(){
		List<BlockedBhv> blockedBhvList = new ArrayList<BlockedBhv>(
				UserBhvManager.getInstance().getBlockedBhvSet());
		Collections.sort(blockedBhvList, Collections.reverseOrder());
		return Collections.unmodifiableList(blockedBhvList);
	}

	public class BlockedBhvInfoAdapter extends ArrayAdapter<ViewableUserBhv> {

		public BlockedBhvInfoAdapter() {
			super(getActivity(), R.layout.listview_item, blockedBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			BlockedBhv blockedUserBhv = (BlockedBhv)blockedBhvList.get(position);

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
		BlockedBhv blockedUserBhv = (BlockedBhv)blockedBhvList.get(pos);
		switch(itemId) {
		case R.id.unblock:
			UserBhvManager uBhvManager = UserBhvManager.getInstance();
			uBhvManager.unblock(blockedUserBhv);
			return getResources().getString(R.string.action_msg_unblock);
		default:
			return null;
		}
	}

	private void doPostAction() {
		NotiBarNotifier.getInstance().doNotification();
		getActivity().sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW"));
	}
	
	private void showToastMsg(String actionMsg){
		if(actionMsg == null)
			return ;
		
		Toast t = Toast.makeText(getActivity(), actionMsg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

}