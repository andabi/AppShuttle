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
import lab.davidahn.appshuttle.view.ViewService;
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
		
		//add dummy for add button
		favoriteBhvList.add(new FavoriteBhv(BaseUserBhv.create(UserBhvType.NONE, ""), 0, false, 0));

		favoriteBhvList.addAll(FavoriteBhvManager.getInstance().getFavoriteBhvListSorted());
		
		//add dummy for info msg
		if(favoriteBhvList.size() <= 1)
			favoriteBhvList.add(new FavoriteBhv(BaseUserBhv.create(UserBhvType.NONE, ""), 0, false, 0));
		
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
			// add bhv activity
			intent = new Intent(AppShuttleApplication.getContext(), AddableBhvActivity.class);
			intent.putExtra("actionOnItemClick", AppShuttleMainActivity.ACTION_FAVORITE);
		} else {
			//info msg
			if (!favoriteBhvList.get(position).isValid())
				return;

			intent = adapter.getItem(position).getLaunchIntent();
		}
		
//		if(intent == null) return;
		StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		getActivity().startActivity(intent);
	}

	private void openMenu(View v, int pos) {
		if (!favoriteBhvList.get(pos).isValid())
			return;

		View menu = v.findViewById(R.id.listview_favorite_menu);
		menu.setVisibility(View.VISIBLE);
		posMenuOpened = pos;
	}
	
	private void closeMenu() {
		for(int i=0;i<getListView().getChildCount();i++){
			if (!favoriteBhvList.get(i).isValid())
				continue;
			View menu = getListView().getChildAt(i).findViewById(R.id.listview_favorite_menu);
			menu.setVisibility(View.GONE);
		}
		posMenuOpened = -1;
	}
	
	private boolean isMenuOpened(){
		if(posMenuOpened <= 0 ) 
			return false;
		else 
			return true;
	}
	
	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
		@Override
		public void drop(int fromPos, int toPos) {
			//pos for add 
			if(fromPos == 0 || toPos == 0) return;
				
			FavoriteBhv bhv = adapter.getItem(fromPos);
			
			adapter.remove(bhv);
			adapter.insert(bhv, toPos);

			for(int i=1; i<adapter.getCount(); i++){
				Message msg = new Message();
				msg.what = AppShuttleMainActivity.ACTION_FAVORITE_UPDATE_ORDER;
				msg.arg1 = i;
				msg.obj = adapter.getItem(i);
				AppShuttleMainActivity.userActionHandler.sendMessage(msg);
			}

			getActivity().startService(new Intent(getActivity(), ViewService.class).putExtra("isOnlyNotibar", true));
		}
	};
	
	public class FavoriteBhvInfoAdapter extends ArrayAdapter<FavoriteBhv> {

		public FavoriteBhvInfoAdapter() {
			super(getActivity(), R.layout.favorite_listview, favoriteBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			//add button
			if (position == 0) {
				View addView = inflater.inflate(R.layout.listview_add, parent, false);
				return addView;
			}

			View itemView = inflater.inflate(R.layout.favorite_listview, parent, false);
			FavoriteBhv favoriteUserBhv = favoriteBhvList.get(position);

			//info msg
			if (!favoriteUserBhv.isValid()) {
				View infoView = inflater.inflate(R.layout.listview_info_msg, parent, false);
				
				TextView infoSubject = (TextView) infoView.findViewById(R.id.listview_info_subject);
				infoSubject.setText(R.string.msg_manual_favorite_subject);
				
				TextView infoText = (TextView) infoView.findViewById(R.id.listview_info_text);
				infoText.setText(R.string.msg_manual_favorite_text);
				return infoView;
			}
			
			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_favorite_item_image);
			iconView.setImageDrawable(favoriteUserBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_favorite_item_firstline);
			firstLineView.setText(favoriteUserBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_favorite_item_secondline);
			secondLineView.setText(favoriteUserBhv.getViewMsg());
			
			ImageView rightSideImageView = (ImageView) itemView.findViewById(R.id.listview_favorite_item_image_grabber);
//			if(favoriteUserBhv.isNotifiable())
//				rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.notifiable_dark));
			rightSideImageView.setImageDrawable(getResources().getDrawable(R.drawable.grabber));
				
			View.OnClickListener menuItemListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Message msg = new Message();
					switch(v.getId()){
					case R.id.listview_favorite_menu_unfavorite:
						msg.what = AppShuttleMainActivity.ACTION_UNFAVORITE;
						break;
					case R.id.listview_favorite_menu_ignore:
						msg.what = AppShuttleMainActivity.ACTION_IGNORE;
						break;
					case R.id.listview_favorite_menu_share:
						msg.what = AppShuttleMainActivity.ACTION_SHARE;
						break;
					}
					
					msg.obj = favoriteBhvList.get(posMenuOpened);
					AppShuttleMainActivity.userActionHandler.sendMessage(msg);
				}
			};
			
			ImageView unfavoriteView = (ImageView) itemView.findViewById(R.id.listview_favorite_menu_unfavorite);
			unfavoriteView.setOnClickListener(menuItemListener);
					
			ImageView ignoreView = (ImageView) itemView.findViewById(R.id.listview_favorite_menu_ignore);
			ignoreView.setOnClickListener(menuItemListener);

			ImageView shareView = (ImageView) itemView.findViewById(R.id.listview_favorite_menu_share);
			if(favoriteUserBhv.isSharable()){
				shareView.setOnClickListener(menuItemListener);
			} else {
				shareView.setVisibility(View.GONE);
			}
			
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