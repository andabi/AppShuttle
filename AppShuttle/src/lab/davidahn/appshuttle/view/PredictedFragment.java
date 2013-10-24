package lab.davidahn.appshuttle.view;

import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import android.app.ListFragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PredictedFragment extends ListFragment {
	private PredictedBhvInfoListAdapter adapter;

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
			adapter = new PredictedBhvInfoListAdapter(
					AppShuttleApplication.recentPredictedBhvInfoList);
			setListAdapter(adapter);
			setListShown(true);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentComplexList", "Item clicked: " + id);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class PredictedBhvInfoListAdapter extends
			ArrayAdapter<PredictedBhvInfo> {
		private final List<PredictedBhvInfo> predictedBhvInfoList;

		public PredictedBhvInfoListAdapter(
				List<PredictedBhvInfo> predictedBhvInfoList) {
			super(getActivity(), R.layout.listview_item, predictedBhvInfoList);
			this.predictedBhvInfoList = predictedBhvInfoList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.listview_item, parent,
					false);

			PredictedBhvInfo predictedBhvInfo = predictedBhvInfoList
					.get(position);
			String packageName = predictedBhvInfo.getUserBhv().getBhvName();

			try {
				PackageManager packageManager = getActivity()
						.getPackageManager();

				ImageView iconView = (ImageView) itemView
						.findViewById(R.id.listview_item_icon);
				BitmapDrawable iconDrawable = (BitmapDrawable) packageManager
						.getApplicationIcon(packageName);
				iconView.setImageDrawable(iconDrawable);

				TextView firstLineView = (TextView) itemView
						.findViewById(R.id.listview_item_firstLine);
				ApplicationInfo ai = packageManager.getApplicationInfo(
						packageName, 0);
				String applicationName = (String) (ai != null ? packageManager
						.getApplicationLabel(ai) : "no name");
				firstLineView.setText(applicationName);

				TextView secondLineView = (TextView) itemView
						.findViewById(R.id.listview_item_secondLine);
				secondLineView.setText(predictedBhvInfo.makeViewMessage());

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			return itemView;
		}
	}
}