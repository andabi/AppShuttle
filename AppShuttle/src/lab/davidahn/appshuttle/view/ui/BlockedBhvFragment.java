package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.BlockedBhv;
import lab.davidahn.appshuttle.view.BlockedBhvManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BlockedBhvFragment extends ListFragment {
	private BlockedBhvInfoAdapter adapter;
	private List<BlockedBhv> blockedBhvList;
	private int posMenuOpened = -1; //menu closed
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.ignore_view, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
//		setEmptyText(getResources().getString(R.string.msg_manual_ignore));

		blockedBhvList = new ArrayList<BlockedBhv>();
		
		// add dummy Bhv for add button
		blockedBhvList.add(new BlockedBhv(BaseUserBhv.create(UserBhvType.NONE, ""), 0));

		blockedBhvList.addAll(BlockedBhvManager.getInstance().getBlockedBhvListSorted());
		
		adapter = new BlockedBhvInfoAdapter();
		setListAdapter(adapter);

//		setListShown(true);
		
		posMenuOpened = -1;
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	        	if(!isMenuOpened()){
	        		openMenu(v, position);
	        	} else {
	        		if(posMenuOpened != position) {
	        			closeMenu();
		        		openMenu(v, position);
	        		}
	        	}
	            return true;
	        }
	    });
		
		getListView().setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
						closeMenu();
			}
		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if(posMenuOpened == position)
			return;
		
		Intent intent = null;
		if (position == 0) {
			// add bhv activity
			intent = new Intent(AppShuttleApplication.getContext(), AddBhvActivity.class);
			intent.putExtra("actionOnItemClick", AppShuttleMainActivity.ACTION_IGNORE);
		} else {
			intent = adapter.getItem(position).getLaunchIntent();
		}

		StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		getActivity().startActivity(intent);
	}
	
	private void openMenu(View v, int pos) {
		if(pos <= 0) return;
		View menu = v.findViewById(R.id.listview_ignore_menu);
		menu.setVisibility(View.VISIBLE);
		posMenuOpened = pos;
	}
	
	private void closeMenu() {
		for(int i=1;i<getListView().getChildCount();i++){
			View menu = getListView().getChildAt(i).findViewById(R.id.listview_ignore_menu);
			menu.setVisibility(View.GONE);
		}
		posMenuOpened = -1;
	}
	
	private boolean isMenuOpened(){
		if(posMenuOpened <= 0 ) return false;
		else return true;
	}
	

	public class BlockedBhvInfoAdapter extends ArrayAdapter<BlockedBhv> {

		public BlockedBhvInfoAdapter() {
			super(getActivity(), R.layout.ignore_listview, blockedBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if (position == 0) {
				View addView = inflater.inflate(R.layout.listview_add, parent, false);
				return addView;
			}
			
			View itemView = inflater.inflate(R.layout.ignore_listview, parent, false);
			BlockedBhv blockedUserBhv = blockedBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_ignore_item_image);
			iconView.setImageDrawable(blockedUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_ignore_item_firstline);
			firstLineView.setText(blockedUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_ignore_item_secondline);
			secondLineView.setText(blockedUserBhv.getViewMsg());

			TextView presentView = (TextView) itemView.findViewById(R.id.listview_ignore_menu_unignore);
			presentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Message msg = new Message();
					switch(v.getId()){
					case R.id.listview_ignore_menu_unignore:
						msg.what = AppShuttleMainActivity.ACTION_UNIGNORE;
						break;
					}
					msg.obj = blockedBhvList.get(posMenuOpened);
					AppShuttleMainActivity.userActionHandler.sendMessage(msg);
				}
			});
			
			ImageView cancelView = (ImageView) itemView.findViewById(R.id.listview_ignore_menu_cancel);
			cancelView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					closeMenu();
				}
			});
			
			return itemView;
		}
	}
}