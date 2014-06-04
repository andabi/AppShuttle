package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.R;
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

		blockedBhvList = new ArrayList<BlockedBhv>(BlockedBhvManager.getInstance().getBlockedBhvListSorted());
		
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
		
		Intent intent = adapter.getItem(position).getLaunchIntent();
		if(intent == null)
			return;
		
		//TODO 수집시 캐치하는 새 앱과 중복되어 캐치될 수 있지 않나? isClick는 지금 당장 안쓰고 있으니 이 코드가 필요한지 확인 필요
		StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		getActivity().startActivity(intent);
	}
	
	private void openMenu(View v, int pos) {
		if(pos < 0) return;
		View menu = v.findViewById(R.id.listview_ignore_menu);
		menu.setVisibility(View.VISIBLE);
		posMenuOpened = pos;
	}
	
	private void closeMenu() {
		for(int i=0;i<getListView().getChildCount();i++){
			View menu = getListView().getChildAt(i).findViewById(R.id.listview_ignore_menu);
			menu.setVisibility(View.GONE);
		}
		posMenuOpened = -1;
	}
	
	private boolean isMenuOpened(){
		if(posMenuOpened < 0 ) return false;
		else return true;
	}
	

	public class BlockedBhvInfoAdapter extends ArrayAdapter<BlockedBhv> {

		public BlockedBhvInfoAdapter() {
			super(getActivity(), R.layout.ignore_listview, blockedBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
					case R.id.listview_present_menu_favorite:
						msg.what = AppShuttleMainActivity.ACTION_FAVORITE;
						break;
					case R.id.listview_present_menu_ignore:
						msg.what = AppShuttleMainActivity.ACTION_IGNORE;
						break;
					case R.id.listview_favorite_menu_unfavorite:
						msg.what = AppShuttleMainActivity.ACTION_UNFAVORITE;
						break;
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