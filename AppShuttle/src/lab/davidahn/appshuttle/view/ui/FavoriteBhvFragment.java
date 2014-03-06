package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
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

public class FavoriteBhvFragment extends ListFragment {
	private FavoriteBhvInfoAdapter adapter;
	private ActionMode actionMode;
	private List<FavoriteBhv> favoriteBhvList;
	
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

		setEmptyText(getResources().getString(R.string.msg_manual_favorite));

		favoriteBhvList = new ArrayList<FavoriteBhv>(FavoriteBhvManager.getInstance().getFavoriteBhvListSorted());
		
		adapter = new FavoriteBhvInfoAdapter();
		setListAdapter(adapter);

		setListShown(true);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	    		if(actionMode == null) {
	    			if(favoriteBhvList.get(position).isNotifiable())
	    				actionMode = getActivity().startActionMode(notifiableActionModeCallback);
	    			else
	    				actionMode = getActivity().startActionMode(unnotifiableActionModeCallback);
	    				
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

		getActivity().startActivity(intent);
	}

	public class FavoriteBhvInfoAdapter extends ArrayAdapter<FavoriteBhv> {

		public FavoriteBhvInfoAdapter() {
			super(getActivity(), R.layout.listview_item, favoriteBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			FavoriteBhv favoriteUserBhv = favoriteBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_image);
			iconView.setImageDrawable(favoriteUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstline);
			firstLineView.setText(favoriteUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondline);
			secondLineView.setText(favoriteUserBhv.getViewMsg());
			
			ImageView rightSideImageView = (ImageView) itemView.findViewById(R.id.listview_item_image_rightside);
			if(favoriteUserBhv.isNotifiable())
				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.notifiable_dark));
				
			return itemView;
		}
	}
	
	ActionMode.Callback notifiableActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.notifiable_favorite_actionmode, menu);
			
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

			if(favoriteBhvList.size() < pos + 1)
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
			inflater.inflate(R.menu.unnotifiable_favorite_actionmode, menu);
			
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

			if(favoriteBhvList.size() < pos + 1)
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
		FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
		FavoriteBhv favoriteUserBhv = favoriteBhvList.get(pos);
		switch(itemId) {
		case R.id.unfavorite:
			favoriteBhvManager.unfavorite(favoriteUserBhv);
			return favoriteUserBhv.getBhvNameText() + getResources().getString(R.string.action_msg_unfavorite);
		case R.id.favorite_notify:
			boolean isSuccess = favoriteBhvManager.trySetNotifiable(favoriteUserBhv);

			if(isSuccess){
				String msg = favoriteUserBhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_notifiable);
				if(favoriteBhvManager.isFullProperNumFavorite()){
					msg += " " 
						+ favoriteBhvManager.getProperNumFavorite() 
						+ getResources().getString(R.string.action_msg_favorite_notifiable_num_proper);
				}
				return msg;
			} else {
				return getResources().getString(R.string.action_msg_favorite_notifiable_failure);
			}
		case R.id.favorite_unnotify:
			favoriteBhvManager.setUnNotifiable(favoriteUserBhv);
			return favoriteUserBhv.getBhvNameText() + getResources().getString(R.string.action_msg_favorite_unnotifiable);
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