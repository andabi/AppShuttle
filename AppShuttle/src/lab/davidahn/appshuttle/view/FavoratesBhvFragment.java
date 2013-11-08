package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
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

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_icon);
			iconView.setImageDrawable(favoratesUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstLine);
			firstLineView.setText(favoratesUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondLine);
			secondLineView.setText(favoratesUserBhv.getViewMsg());
			
			ImageView rightSideImageView = (ImageView) itemView.findViewById(R.id.listview_item_image_rightside);
			if(favoratesUserBhv.isNotifiable())
				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_alarms));
			
			return itemView;
		}
	}
	
	ActionMode.Callback actionCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//			int pos = (Integer)mode.getTag();

			MenuInflater inflater = mode.getMenuInflater();
//			if(favoratesBhvList.get(pos).isNotifiable())
				inflater.inflate(R.menu.favorates_notifiable_actionmode, menu);
//			else
//				inflater.inflate(R.menu.favorates_not_notifiable_actionmode, menu);
			
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
			
			String actionMsg = "";
			UserBhvManager uBhvManager = UserBhvManager.getInstance();

			FavoratesUserBhv favoratesUBhv = favoratesBhvList.get(pos);
			switch(item.getItemId()) {
			case R.id.unfavorates:
				uBhvManager.unfavorates(favoratesUBhv);
				actionMsg = getResources().getString(R.string.action_msg_unfavorates);
				break;
			case R.id.favorates_not_notify:
				
				if(!favoratesUBhv.isNotifiable())
					return true;
				
				FavoratesUserBhv.setNotNotifiable(favoratesUBhv);
				actionMsg = getResources().getString(R.string.action_msg_favorates_not_notifiable);
				break;
			case R.id.favorates_notify:
				
				if(favoratesUBhv.isNotifiable())
					return true;

				boolean isSuccess = FavoratesUserBhv.trySetNotifiable(favoratesUBhv);
				if(isSuccess)
					actionMsg = getResources().getString(R.string.action_msg_favorates_notifiable);
				else
					actionMsg = getResources().getString(R.string.action_msg_favorates_notifiable_failure);
				break;
			default:
				;
			}
			
			getActivity().sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_VISIBLE"));
			getActivity().startService(new Intent(getActivity(), UpdateService.class));
			
			Toast t = Toast.makeText(getActivity(), actionMsg, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			
			if(actionMode != null)
				actionMode.finish();
			
			return true;
		}
	};
}