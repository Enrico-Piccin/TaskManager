package fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taskmanager.DatabaseHelper;
import com.example.taskmanager.R;

import model.User;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    User u;
    DatabaseHelper db;

    // Chiavi univoche di identificazione delle preferenze
    private static final String USERNAME_KEY = "username";
    private static final String DAILY_GOAL_KEY = "dailyGoal";
    private static final String NOTIFICATIONS_KEY = "notifications";

    public SettingsFragment(User u) {
        this.u = u;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Impostazione del nome dell'utente
        db = new DatabaseHelper(getContext());
        Preference preference = findPreference(USERNAME_KEY);
        preference.setSummary(u.getNome());

        // Impostazione del numero di task giornaliere
        int numTask = getContext().getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).getInt("DAILY_TASKS", 0);
        preference = findPreference(DAILY_GOAL_KEY);
        preference.setSummary(numTask + "");
        preference.setDefaultValue(numTask + "");

        // Impostazione della preferenza di abilitazione delle notifiche
        int notifications = getContext().getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).getInt("NOTIFICATIONS", 1);
        preference = findPreference(NOTIFICATIONS_KEY);
        preference.setDefaultValue(notifications != 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equalsIgnoreCase(USERNAME_KEY)) {
            // Imposto il sommario come descrizione dell'utente per il valore selezionato
            Preference preference = findPreference(USERNAME_KEY);
            String username = sharedPreferences.getString(key, "");
            preference.setSummary(username);
            u.setNome(username);
            db.updateUser(u);
        }
        else if(key.equalsIgnoreCase(DAILY_GOAL_KEY)) {
            // Imposto il sommario come descrizione del DailyGoal per il valore selezionato
            Preference preference = findPreference(DAILY_GOAL_KEY);
            int dailyGoal = Integer.parseInt(sharedPreferences.getString(key, 5 + ""));
            preference.setSummary(dailyGoal + "");
            getContext().getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).edit().putInt("DAILY_TASKS", dailyGoal).apply();
        } else if(key.equalsIgnoreCase(NOTIFICATIONS_KEY)) {
            // Agggiornamento della preferenza
            Preference preference = findPreference(NOTIFICATIONS_KEY);
            int notifications = sharedPreferences.getBoolean(key, true) ? 1 : 0;
            preference.setDefaultValue(notifications != 0);
            getContext().getSharedPreferences(u.getEmail(), Context.MODE_PRIVATE).edit().putInt("NOTIFICATIONS", notifications).apply();
        }
    }
}