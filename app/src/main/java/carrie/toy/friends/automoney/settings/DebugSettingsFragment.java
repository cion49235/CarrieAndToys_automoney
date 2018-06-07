package carrie.toy.friends.automoney.settings;

import android.os.Bundle;

import carrie.toy.friends.automoney.R;


public class DebugSettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.debug_settings);
    }
}
