package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
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

public class FavoratesBhvFragment extends ListFragment {
	private FavoratesBhvInfoAdapter adapter;
	private ActionMode actionMode;
	private List<FavoratesUserBhv> favoratesBhvList;
	
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
		
		setEmptyText(getResources().getString(R.string.favorates_fragment_empty_msg));

		UserBhvManager uBhvManager = UserBhvManager.getInstance();
		favoratesBhvList = new ArrayList<FavoratesUserBhv>(uBhvManager.getFavoratesBhvSetSorted());
		
		adapter = new FavoratesBhvInfoAdapter();
		setListAdapter(adapter);

		setListShown(true);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	    		if(actionMode == null) {
	    			if(favoratesBhvList.get(position).isNotifiable())
	    				actionMode = getActivity().startActionMode(notifiableActionModeCallback);
	    			else
	    				actionMode = getActivity().startActionMode(unnotifiableActionModeCallback);
	    				
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class FavoratesBhvInfoAdapter extends ArrayAdapter<FavoratesUserBhv> {

		public FavoratesBhvInfoAdapter() {
			super(getActivity(), R.layout.listview_item, favoratesBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			FavoratesUserBhv favoratesUserBhv = favoratesBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_image);
			iconView.setImageDrawable(favoratesUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstline);
			firstLineView.setText(favoratesUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondline);
			secondLineView.setText(favoratesUserBhv.getViewMsg());
			
			ImageView rightSideImageView = (ImageView) itemView.findViewById(R.id.listview_item_image_rightside);
			if(favoratesUserBhv.isNotifiable())
				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important_dark));
			else
				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_important_dark));
				
			return itemView;
		}
	}
	
	ActionMode.Callback notifiableActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.notifiable_favorates_actionmode, menu);
			
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

			if(favoratesBhvList.size() < pos + 1)
				return false;

			String actionMsg = doActionAndGetMsg(pos, item.getItemId());
			doPostAction();
			showToastMsg(actionMsg);
			
			if(actionMode != null)
				actionMode.finish();
			
			return true;
		}
	};
	
	ActionMode.Callback unnotifiableActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.unnotifiable_favorates_actionmode, menu);
			
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

			if(favoratesBhvList.size() < pos + 1)
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
		FavoratesUserBhv favoratesUserBhv = favoratesBhvList.get(pos);
		switch(itemId) {
		case R.id.unfavorates:
			UserBhvManager uBhvManager = UserBhvManager.getInstance();
			uBhvManager.unfavorates(favoratesUserBhv);
			return getResources().getString(R.string.action_msg_unfavorates);
		case R.id.favorates_notify:
			boolean isSuccess = FavoratesUserBhv.trySetNotifiable(favoratesUserBhv);

			if(isSuccess){
				String msg = getResources().getString(R.string.action_msg_favorates_notifiable);
				if(FavoratesUserBhv.isFullProperNumFavorates()){
					msg += " " 
						+ FavoratesUserBhv.getProperNumFavorates() 
						+ getResources().getString(R.string.action_msg_favorates_notifiable_num_proper);
				}
				return msg;
			} else {
				return getResources().getString(R.string.action_msg_favorates_notifiable_failure);
			}
		case R.id.favorates_unnotify:
			FavoratesUserBhv.setUnNotifiable(favoratesUserBhv);
			return getResources().getString(R.string.action_msg_favorates_unnotifiable);
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