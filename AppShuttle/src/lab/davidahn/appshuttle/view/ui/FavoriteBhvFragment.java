package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.commonsware.cwac.tlv.TouchListView;

public class FavoriteBhvFragment extends ListFragment {
	private FavoriteBhvInfoAdapter adapter;
	private List<FavoriteBhv> favoriteBhvList;
	private int posMenuOpened = -1; //menu closed
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.favorite_view, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		setEmptyText(getResources().getString(R.string.msg_manual_favorite));

		favoriteBhvList = new ArrayList<FavoriteBhv>();
		// add dummy Bhv
		favoriteBhvList.add(new FavoriteBhv(BaseUserBhv.create(UserBhvType.NONE, ""), 0, false, 0));

		favoriteBhvList.addAll(FavoriteBhvManager.getInstance().getFavoriteBhvListSorted());
		
		adapter = new FavoriteBhvInfoAdapter();
		setListAdapter(adapter);

		TouchListView touchListView = (TouchListView)getListView();
		touchListView.setDropListener(onDrop);

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
			// search activity
			intent = new Intent(AppShuttleApplication.getContext(), SearchableActivity.class);
		} else {
			intent = adapter.getItem(position).getLaunchIntent();
			StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		}
		
		getActivity().startActivity(intent);
	}

	private void openMenu(View v, int pos) {
		if(pos <= 0) return;
		View menu = v.findViewById(R.id.listview_favorite_menu);
		menu.setVisibility(View.VISIBLE);
		posMenuOpened = pos;
	}
	
	private void closeMenu() {
		for(int i=1;i<getListView().getChildCount();i++){
			View menu = getListView().getChildAt(i).findViewById(R.id.listview_favorite_menu);
			menu.setVisibility(View.GONE);
		}
		posMenuOpened = -1;
	}
	
	private boolean isMenuOpened(){
		if(posMenuOpened <= 0 ) return false;
		else return true;
	}
	
	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
		@Override
		public void drop(int fromPos, int toPos) {
			//pos for add 
			if(fromPos == 0 || toPos == 0) return;
				
			FavoriteBhv bhv = adapter.getItem(fromPos);
			
			Log.d("test", bhv.getBhvName());
			
			adapter.remove(bhv);
			adapter.insert(bhv, toPos);
			
			FavoriteBhvManager favoriteBhvManager = FavoriteBhvManager.getInstance();
			for(int i=1; i<adapter.getCount();i++)
				favoriteBhvManager.updateOrder(adapter.getItem(i), i);
			
			NotiBarNotifier.getInstance().updateNotification();
		}
	};
	
	public class FavoriteBhvInfoAdapter extends ArrayAdapter<FavoriteBhv> {

		public FavoriteBhvInfoAdapter() {
			super(getActivity(), R.layout.favorite_listview, favoriteBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (position == 0) {
				View addView = inflater.inflate(R.layout.listview_add, parent, false);
				return addView;
			}
			
			View itemView = inflater.inflate(R.layout.favorite_listview, parent, false);
			FavoriteBhv favoriteUserBhv = favoriteBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_favorite_item_image);
			iconView.setImageDrawable(favoriteUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_favorite_item_firstline);
			firstLineView.setText(favoriteUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_favorite_item_secondline);
			secondLineView.setText(favoriteUserBhv.getViewMsg());
			
			ImageView rightSideImageView = (ImageView) itemView.findViewById(R.id.listview_favorite_item_image_rightside);
//			if(favoriteUserBhv.isNotifiable())
//				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.notifiable_dark));
			rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.grabber));
				
			TextView unfavoriteView = (TextView) itemView.findViewById(R.id.listview_favorite_menu_unfavorite);
			unfavoriteView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AppShuttleMainActivity mainActivity = (AppShuttleMainActivity)getActivity();
					String actionMsg = mainActivity.doActionAndGetMsg(favoriteBhvList.get(posMenuOpened), v.getId());
					mainActivity.doPostAction();
					mainActivity.showToastMsg(actionMsg);
				}
			});
			
			ImageView cancelView = (ImageView) itemView.findViewById(R.id.listview_favorite_menu_cancel);
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