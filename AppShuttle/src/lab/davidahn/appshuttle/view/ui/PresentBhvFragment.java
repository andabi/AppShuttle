package lab.davidahn.appshuttle.view.ui;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.AppBhvCollector;
import lab.davidahn.appshuttle.collect.bhv.BaseUserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.report.StatCollector;
import lab.davidahn.appshuttle.view.PredictedPresentBhv;
import lab.davidahn.appshuttle.view.PresentBhv;
import android.app.AlarmManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	
		presentBhvList = PresentBhv.getPresentBhvListFilteredSorted(Integer.MAX_VALUE);

    	final long now = System.currentTimeMillis();
		long readyMsgExpirationTime = AppShuttleApplication.launchTime + 3000;
		long firstInstalledTime = AppBhvCollector.getInstance().getFirstInstalledTime(getActivity().getPackageName());
		long learningMsgExpirationTime = firstInstalledTime + 3 * AlarmManager.INTERVAL_DAY;
		final LinearLayout notiMsgView = (LinearLayout)getView().findViewById(R.id.present_noti_msg);
    	final TextView subject = (TextView)getView().findViewById(R.id.present_noti_msg_subject);
//		final TextView text = (TextView)getView().findViewById(R.id.present_noti_msg_text);
		if (presentBhvList.isEmpty() && now < readyMsgExpirationTime) {
			subject.setText(R.string.msg_wait);
		} else if (now < learningMsgExpirationTime) {
			new CountDownTimer(learningMsgExpirationTime - now, 1000) {
			     public void onTick(long millisUntilFinished) {
			    	if(getView() == null)
			    		return;
			    	CharSequence relativeTimeSpan = DateUtils.getRelativeTimeSpanString(now + millisUntilFinished, 
			    			now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
					subject.setText(String.format(getActivity().getString(R.string.msg_learning_subject), relativeTimeSpan + " (" + DateUtils.formatElapsedTime(millisUntilFinished / 1000) + ")").toString());
//					text.setText(String.format(getActivity().getString(R.string.msg_learning_text), relativeTimeSpan).toString());
			     }
			     public void onFinish() {
			    	if(getView() == null)
			    		return;
					notiMsgView.setVisibility(View.GONE);
			     }
			  }.start();
		} else {
			notiMsgView.setVisibility(View.GONE);
		}
	
		// add dummy for info msg
		if(presentBhvList.isEmpty())
			presentBhvList.add(new PredictedPresentBhv(BaseUserBhv.create(UserBhvType.NONE, "")));
		
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
			
		if (!presentBhvList.get(position).isValid())
			return;

		Intent intent = adapter.getItem(position).getLaunchIntent();
		
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

			//info msg
			if (!presentBhv.isValid()) {
				View infoView = inflater.inflate(R.layout.listview_info_msg, parent, false);

				TextView infoSubject = (TextView) infoView.findViewById(R.id.listview_info_subject);
				infoSubject.setText(R.string.msg_no_results_subject);
				TextView infoText = (TextView) infoView.findViewById(R.id.listview_info_text);
				infoText.setText(R.string.msg_no_results_text);

				return infoView;
			}
			
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