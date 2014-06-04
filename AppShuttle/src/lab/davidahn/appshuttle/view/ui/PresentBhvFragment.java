package lab.davidahn.appshuttle.view.ui;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.PresentBhv;
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

public class PresentBhvFragment extends ListFragment {
	private PresentBhvAdapter adapter;
	private List<PresentBhv> presentBhvList;
	private int posMenuOpened = -1; //menu closed
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.present_view, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
		TextView emptyMsgView = (TextView)getView().findViewById(R.id.present_empty_msg);
		if(System.currentTimeMillis() - AppShuttleApplication.launchTime < 3000)
			emptyMsgView.setText(R.string.msg_wait);

		presentBhvList = PresentBhv.getPresentBhvListFilteredSorted(Integer.MAX_VALUE);
		
		adapter = new PresentBhvAdapter();
		setListAdapter(adapter);

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
		
		StatCollector.getInstance().notifyBhvTransition(adapter.getItem(position).getUserBhv(), true);
		getActivity().startActivity(intent);
	}
	
	private void openMenu(View v, int pos) {
		if(pos < 0) return;
		View menu = v.findViewById(R.id.listview_present_menu);
		menu.setVisibility(View.VISIBLE);
		posMenuOpened = pos;
	}
	
	private void closeMenu() {
		for(int i=0;i<getListView().getChildCount();i++){
			View menu = getListView().getChildAt(i).findViewById(R.id.listview_present_menu);
			menu.setVisibility(View.GONE);
		}
		posMenuOpened = -1;
	}
	
	private boolean isMenuOpened(){
		if(posMenuOpened < 0 ) return false;
		else return true;
	}
	
	public class PresentBhvAdapter extends ArrayAdapter<PresentBhv> {

		public PresentBhvAdapter() {
			super(getActivity(), R.layout.present_listview, presentBhvList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.present_listview, parent, false);
			PresentBhv presentBhv = presentBhvList.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_present_item_image);
			iconView.setImageDrawable(presentBhv.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_present_item_firstline);
			firstLineView.setText(presentBhv.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_present_item_secondline);
			secondLineView.setText(presentBhv.getViewMsg());

			View.OnClickListener menuItemListener = new View.OnClickListener() {
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
					case R.id.listview_present_menu_share:
						msg.what = AppShuttleMainActivity.ACTION_SHARE;
						break;
					}

					msg.obj = presentBhvList.get(posMenuOpened);
					AppShuttleMainActivity.userActionHandler.sendMessage(msg);
				}
			};
			ImageView favoriteView = (ImageView) itemView.findViewById(R.id.listview_present_menu_favorite);
			favoriteView.setOnClickListener(menuItemListener);

			ImageView ignoreView = (ImageView) itemView.findViewById(R.id.listview_present_menu_ignore);
			ignoreView.setOnClickListener(menuItemListener);
			
			ImageView shareView = (ImageView) itemView.findViewById(R.id.listview_present_menu_share);
			if(presentBhv.isSharable()){
				shareView.setOnClickListener(menuItemListener);
			} else {
				shareView.setVisibility(View.GONE);
			}
			
			ImageView cancelView = (ImageView) itemView.findViewById(R.id.listview_present_menu_cancel);
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

//			ViewGroup menu = (ViewGroup)itemView.findViewById(R.id.listview_menu);
//			menu.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
//					ViewGroup.LayoutParams.MATCH_PARENT));

//			ImageView favoriteView = new ImageView(getContext());
//			favoriteView.setLayoutParams(new android.view.ViewGroup.LayoutParams(30,30));
//			favoriteView.setImageResource(R.id.favorate);
//			menu.addView(favoriteView);
//
//			ImageView ignoreView = new ImageView(getContext());
//			ignoreView.setLayoutParams(new android.view.ViewGroup.LayoutParams(30,30));
//			ignoreView.setImageResource(R.id.block);
//			menu.addView(ignoreView);