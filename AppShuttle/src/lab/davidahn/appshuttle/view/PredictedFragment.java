package lab.davidahn.appshuttle.view;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PredictedFragment extends ListFragment {
	private PredictedBhvInfoAdapter adapter;

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

		setEmptyText("No results");

		if (AppShuttleApplication.recentPredictedBhvInfoList == null) {
			setListShown(false);
		} else {
			List<PredictedBhvInfoForView> predictedBhvInfoListForView = 
					PredictedBhvInfoForView.getPredictedBhvInfoListForView(AppShuttleApplication.recentPredictedBhvInfoList);

			adapter = new PredictedBhvInfoAdapter(
					predictedBhvInfoListForView);
			setListAdapter(adapter);
			setListShown(true);
		}
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

	public class PredictedBhvInfoAdapter extends ArrayAdapter<PredictedBhvInfoForView> {
		private final List<PredictedBhvInfoForView> predictedBhvInfoListForView;

		public PredictedBhvInfoAdapter(List<PredictedBhvInfoForView> _predictedBhvInfoListForView) {
			super(getActivity(), R.layout.listview_item, _predictedBhvInfoListForView);
			predictedBhvInfoListForView = _predictedBhvInfoListForView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent, false);
			PredictedBhvInfoForView predictedBhvInfoForView = predictedBhvInfoListForView.get(position);

			ImageView iconView = (ImageView) itemView.findViewById(R.id.listview_item_icon);
			iconView.setImageDrawable(predictedBhvInfoForView.getIcon());

			TextView firstLineView = (TextView) itemView.findViewById(R.id.listview_item_firstLine);
			firstLineView.setText(predictedBhvInfoForView.getBhvNameText());

			TextView secondLineView = (TextView) itemView.findViewById(R.id.listview_item_secondLine);
			secondLineView.setText(predictedBhvInfoForView.getViewMsg());

			return itemView;
		}
	}
}