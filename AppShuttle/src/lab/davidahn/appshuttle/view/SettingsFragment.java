package lab.davidahn.appshuttle.view;

import lab.davidahn.appshuttle.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("noti.view.enabled")) {
			getActivity().startService(new Intent(getActivity(), UpdateService.class));
        }
    }
	
	@Override
	public void onDestroy() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
}