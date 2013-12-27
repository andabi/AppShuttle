package lab.davidahn.appshuttle.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
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

public class PresentBhvFragment extends ListFragment {
	private PresentBhvAdapter adapter;
	private ActionMode actionMode;
	private List<ViewableUserBhv> presentBhvList;
	
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
		
		setEmptyText(getResources().getString(R.string.present_empty_msg));
		
		presentBhvList = getPresentUserBhvSorted(Integer.MAX_VALUE);
		
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
	    		}
	            return true;
	        }
	    });
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		getActivity().startActivity(adapter.getItem(position).getLaunchIntent());
	}
	
	public static List<ViewableUserBhv> getPresentUserBhvSorted(int topN) {
		List<ViewableUserBhv> res = new ArrayList<ViewableUserBhv>();

		List<PresentBhv> predictedPresent = new ArrayList<PresentBhv>();
		for(PresentBhv predictedBhv : AppShuttleApplication.recentPresentBhvs.values())
			if(isCandidatePresentUserBhv(predictedBhv))
				predictedPresent.add(predictedBhv);

		Collections.sort(predictedPresent, Collections.reverseOrder());
		
		for(PresentBhv uBhv : predictedPresent)
			res.add(UserBhvManager.getInstance().getViewableUserBhv(uBhv));
			
		return res.subList(0, Math.min(res.size(), topN));
	}
	
	private static boolean isCandidatePresentUserBhv(UserBhv uBhv){
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		if(userBhvManager.getNormalBhvSet().contains(uBhv))
			return true;
		else if(userBhvManager.getFavoriteBhvSet().contains(uBhv)
				&& !((FavoriteBhv)userBhvManager.getViewableUserBhv(uBhv)).isNotifiable())
			return true;
		else
			return false;
	}

	public class PresentBhvAdapter extends ArrayAdapter<ViewableUserBhv> {

		public PresentBhvAdapter() {
			super(getActivity(), R.layout.listview_item, presentBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			ViewableUserBhv presentBhv = presentBhvList.get(position);

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
		UserBhvManager uBhvManager = UserBhvManager.getInstance();
		
		switch(itemId) {
		case R.id.favorate:	
			uBhvManager.favorite((presentBhvList.get(pos)));
			return getResources().getString(R.string.action_msg_favorite);
		case R.id.block:
			uBhvManager.block((presentBhvList.get(pos)));
			return getResources().getString(R.string.action_msg_block);
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