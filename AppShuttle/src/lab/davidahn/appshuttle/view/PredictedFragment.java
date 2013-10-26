package lab.davidahn.appshuttle.view;

import java.util.Collections;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

public class PredictedFragment extends ListFragment {
	private PredictedBhvInfoAdapter adapter;
	private ActionMode actionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new PredictedBhvInfoAdapter(Collections.<BhvInfoForView> emptyList());
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
		
		setEmptyText("No results");

		Predictor predictor = Predictor.getInstance();
		List<PredictedBhvInfo> predictedBhvInfoList = predictor.getRecentPredictedBhvInfo(Integer.MAX_VALUE);
		List<BhvInfoForView> predictedBhvInfoListForView = BhvInfoForView.convert(predictedBhvInfoList);
		
		adapter = new PredictedBhvInfoAdapter(
				predictedBhvInfoListForView);
		setListAdapter(adapter);

		if (predictedBhvInfoList == null) {
			setListShown(false);
		} else {
			setListShown(true);
		}
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
	    		if(actionMode == null) {
	    			actionMode = getActivity().startActionMode(actionCallback);
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class PredictedBhvInfoAdapter extends ArrayAdapter<BhvInfoForView> {
		private final List<BhvInfoForView> predictedBhvInfoListForView;

		public PredictedBhvInfoAdapter(List<BhvInfoForView> _predictedBhvInfoListForView) {
			super(getActivity(), R.layout.listview_item, _predictedBhvInfoListForView);
			predictedBhvInfoListForView = _predictedBhvInfoListForView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			BhvInfoForView predictedBhvInfoForView = predictedBhvInfoListForView.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_icon);
			iconView.setImageDrawable(predictedBhvInfoForView.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstLine);
			firstLineView.setText(predictedBhvInfoForView.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondLine);
			secondLineView.setText(predictedBhvInfoForView.getViewMsg());

			return itemView;
		}
	}
	
	ActionMode.Callback actionCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.predicted_bhv_action_mode, menu);
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
			Log.d("test","ok");
			return false;
		}
	};
}