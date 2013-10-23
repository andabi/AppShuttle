package lab.davidahn.appshuttle.view;

import static lab.davidahn.appshuttle.AppShuttleApplication.recentPredictedBhvInfoList;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PredictedFragment extends ListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("test", "createView");

		View v = inflater.inflate(android.R.layout.list_content,
				container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d("test", "activityCreated");

		setEmptyText("No results");

		List<String> contents = new ArrayList<String>();
		
		if(recentPredictedBhvInfoList == null) {
			setListShown(false);
		} else {
			for(PredictedBhvInfo predictedBhvInfo : recentPredictedBhvInfoList){
				contents.add(predictedBhvInfo.getUserBhv().getBhvName());
			}
			
			setListAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1,
					contents
					));
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
        Log.d("test", "onDestroyView()");
    }
}