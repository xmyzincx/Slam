package universityofoulu.slam.PreferenceMenu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import universityofoulu.slam.R;

/**
 * Created by mYz on 21-Mar-16.
 */
public class PrefFragment extends PreferenceFragment {
    private static final String TAG = "PrefFragment";
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);

        Preference default_button = (Preference) findPreference("restore_default_btn");
        default_button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(PrefFragment.this.getActivity());
                SP.edit().clear().commit();
                onCreate(savedInstanceState);
                return true;
            }
        });
    }

/*    @Override
    public void onResume(){
        super.onResume();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.fragment_preference);
    }*/
}
