package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.FavoratesUserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
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
	private List<BhvForView> favoratesBhvInfoForViewList;
	
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
		List<FavoratesUserBhv> favoratesBhvInfoList = new ArrayList<FavoratesUserBhv>(uBhvManager.getFavoratesBhvSet());
		favoratesBhvInfoForViewList = FavoratesBhvForView.convert(favoratesBhvInfoList);
		
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

	public class FavoratesBhvInfoAdapter extends ArrayAdapter<BhvForView> {

		public FavoratesBhvInfoAdapter() {
			super(getActivity(), R.layout.listview_item, favoratesBhvInfoForViewList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			BhvForView bhvInfoForView = favoratesBhvInfoForViewList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_icon);
			iconView.setImageDrawable(bhvInfoForView.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstLine);
			firstLineView.setText(bhvInfoForView.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondLine);
			secondLineView.setText(bhvInfoForView.getViewMsg());

			return itemView;
		}
	}
	
	ActionMode.Callback actionCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.favorates_bhv_action_mode, menu);
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
			int position = (Integer)mode.getTag();

			if(favoratesBhvInfoForViewList.size() < position + 1)
				return false;
			
			String actionMsg = "";
			UserBhvManager uBhvManager = UserBhvManager.getInstance();

			switch(item.getItemId()) {
			case R.id.unfavorates:
				uBhvManager.unfavorates((FavoratesUserBhv)(favoratesBhvInfoForViewList.get(position).getUserBhv()));
				actionMsg = getResources().getString(R.string.unfavorates);
				break;
			default:
				;
			}
			
			getActivity().startService(new Intent(getActivity(), UpdateService.class));
			Toast.makeText(getActivity(), actionMsg, Toast.LENGTH_SHORT).show();
			
			if(actionMode != null)
				actionMode.finish();
			
			return true;
		}
	};
}