package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.report.StatCollector;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class InfoFragment extends ListFragment {
	private ListAdapter adapter;
	private List<Map<String, String>> infoList;
	private static final String INFO_KEY = "key";
	private static final String INFO_VALUE = "value";

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
		infoList = new ArrayList<Map<String,String>>();
		
		// Context info
		infoList.add(getContextInfo());
		
		// Stat info
		Map<String, String> keyValueStat = new HashMap<String, String>();
		keyValueStat.put(INFO_KEY, "Statistics");
		keyValueStat.put(INFO_VALUE, StatCollector.getInstance().toString());
		infoList.add(keyValueStat);
		
		adapter = new SimpleAdapter(getActivity(), infoList, android.R.layout.simple_list_item_2,
				new String[]{INFO_KEY, INFO_VALUE},
				new int[]{android.R.id.text1, android.R.id.text2});
		setListAdapter(adapter);
	}

	private Map<String, String> getContextInfo() {
		Map<String, String> keyValue = new HashMap<String, String>();
		SnapshotUserCxt cxt = AppShuttleApplication.currUserCxt;
		keyValue.put(INFO_KEY, "Context");
		keyValue.put(INFO_VALUE, cxt.toString());
		return keyValue;
	}
}