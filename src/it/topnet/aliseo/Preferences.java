package it.topnet.aliseo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.BaseAdapter;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    static final String KEY_START_NOW = "start_now";
    static final String KEY_LOGIN_NOW = "login_now";
    static final String KEY_LOGOUT_NOW = "logout_now";
    static final String KEY_ENABLED = "enabled";
    static final String KEY_STRINGAAUTH = "stringaauth";
    static final String KEY_NOTIFY = "notify";
    static final String KEY_NOTIFY_ERROR = "notify_error";
    static final String KEY_NOTIFY_SUCCESS = "notify_success";
    static final String KEY_NOTIFY_DISCONNECT = "notify_disconnect";
    static final String KEY_ERROR_NOTIFY = "error_notify";
    static final String KEY_ERROR_NOTIFY_SOUND = "error_notify_sound";
    static final String KEY_ERROR_NOTIFY_VIBRATE = "error_notify_vibrate";
    static final String KEY_ERROR_NOTIFY_LIGHTS = "error_notify_lights";
    static final String KEY_ERROR_NOTIFY_TOAST = "error_notify_toast";
    static final String KEY_SUCCESS_NOTIFY = "success_notify";
    static final String KEY_SUCCESS_NOTIFY_SOUND = "success_notify_sound";
    static final String KEY_SUCCESS_NOTIFY_VIBRATE = "success_notify_vibrate";
    static final String KEY_SUCCESS_NOTIFY_LIGHTS = "success_notify_lights";
    static final String KEY_SUCCESS_NOTIFY_TOAST = "success_notify_toast";
    static final String KEY_DISCONNECT_NOTIFY = "disconnect_notify";
    static final String KEY_DISCONNECT_NOTIFY_SOUND = "disconnect_notify_sound";
    static final String KEY_DISCONNECT_NOTIFY_VIBRATE = "disconnect_notify_vibrate";
    static final String KEY_DISCONNECT_NOTIFY_LIGHTS = "disconnect_notify_lights";
    static final String KEY_DISCONNECT_NOTIFY_TOAST = "disconnect_notify_toast";
    static final String KEY_LANGUAGE = "language";
    static final String KEY_VERSION = "version";
    static final String KEY_WEBSITE = "website";
    static final String KEY_AUTHOR = "author";
    static final String LANGUAGE_DEFAULT = "default";
    static final String EMAIL_TYPE = "message/rfc822";
    static final String EMAIL_AUTHOR = "support@topnet.it";
    static final String EMAIL_SUBJECT = "[ALISEO] ";
    static final String WEBSITE_URL = "http://www.topnet.it";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.loadLocale(this);
        addPreferencesFromResource(R.xml.preferences);
        updateNotificationSummary();
        updateErrorNotificationSummary();
        updateSuccessNotificationSummary();
        updateDisconnectNotificationSummary();
        updateLanguageSummary();
        String versionSummary = String.format(getString(R.string.pref_version_summary), Utils.getVersionName(this));
        findPreference(KEY_VERSION).setSummary(versionSummary);

        findPreference(KEY_START_NOW).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
        		Intent i = new Intent(Preferences.this, WWWrapper.class);
                startActivity(i);
                return true;
            }
        });

        findPreference(KEY_LOGIN_NOW).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            	doLogin();
                return true;
            }
        });

        findPreference(KEY_LOGOUT_NOW).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
            	doLogout();
                return true;
            }
        });

        findPreference(KEY_WEBSITE).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
                startActivity(i);
                return true;
            }
        });

        findPreference(KEY_AUTHOR).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType(EMAIL_TYPE);
                i.putExtra(Intent.EXTRA_EMAIL, new String[] {EMAIL_AUTHOR});
                i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
                startActivity(Intent.createChooser(i, ""));
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_ENABLED)) {
            updateEnabled();
        } else if (key.equals(KEY_NOTIFY_ERROR)
                || key.equals(KEY_NOTIFY_SUCCESS)
            	|| key.equals(KEY_NOTIFY_DISCONNECT)) {
            updateNotificationSummary();
            ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged(); // force update parent screen
        } else if (key.equals(KEY_ERROR_NOTIFY_SOUND)
                || key.equals(KEY_ERROR_NOTIFY_VIBRATE)
                || key.equals(KEY_ERROR_NOTIFY_LIGHTS)
            	|| key.equals(KEY_ERROR_NOTIFY_TOAST)) {
            updateErrorNotificationSummary();
            ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged(); // force update parent screen
        } else if (key.equals(KEY_SUCCESS_NOTIFY_SOUND)
                || key.equals(KEY_SUCCESS_NOTIFY_VIBRATE)
                || key.equals(KEY_SUCCESS_NOTIFY_LIGHTS)
            	|| key.equals(KEY_SUCCESS_NOTIFY_TOAST)) {
            updateSuccessNotificationSummary();
            ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged(); // force update parent screen
        } else if (key.equals(KEY_DISCONNECT_NOTIFY_SOUND)
                || key.equals(KEY_DISCONNECT_NOTIFY_VIBRATE)
                || key.equals(KEY_DISCONNECT_NOTIFY_LIGHTS)
            	|| key.equals(KEY_DISCONNECT_NOTIFY_TOAST)) {
            updateDisconnectNotificationSummary();
            ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged(); // force update parent screen
        } else if (key.equals(KEY_LANGUAGE)) {
            updateLanguageSummary();
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    private void doLogin() {
        Intent i = new Intent(Preferences.this, LoginService.class);
        i.putExtra(LoginService.EXTRA_ACTION, LoginService.ACTION_LOGIN);
		startService(i);
    }

    private void doLogout() {
        Intent i = new Intent(Preferences.this, LoginService.class);
        i.putExtra(LoginService.EXTRA_ACTION, LoginService.ACTION_LOGOUT);
		startService(i);
    }
    
    private void updateEnabled() {
        boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean(KEY_ENABLED, false);
        Utils.setEnableBroadcastReceiver(this, enabled);
        if (enabled) {
        	doLogin();
        }
    }

    private void updateNotificationSummary() {
        ArrayList<String> methods = new ArrayList<String>();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (prefs.getBoolean(Preferences.KEY_NOTIFY_ERROR, false)) {
            methods.add(getString(R.string.pref_notify_error));
        }
        if (prefs.getBoolean(Preferences.KEY_NOTIFY_SUCCESS, false)) {
            methods.add(getString(R.string.pref_notify_success));
        }
        if (prefs.getBoolean(Preferences.KEY_NOTIFY_DISCONNECT, false)) {
            methods.add(getString(R.string.pref_notify_disconnect));
        }
        if (methods.size() == 0) {
            findPreference(KEY_NOTIFY).setSummary(R.string.pref_notify_none);
        } else {
            String summaryStr = join(methods, getString(R.string.pref_notify_deliminator));
            findPreference(KEY_NOTIFY).setSummary(summaryStr);
        }
    }

    private void updateErrorNotificationSummary() {
        ArrayList<String> methods = new ArrayList<String>();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_SOUND, false)) {
            methods.add(getString(R.string.pref_error_notify_sound));
        }
        if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_VIBRATE, false)) {
            methods.add(getString(R.string.pref_error_notify_vibrate));
        }
        if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_LIGHTS, false)) {
            methods.add(getString(R.string.pref_error_notify_lights));
        }
        if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_TOAST, false)) {
            methods.add(getString(R.string.pref_error_notify_toast));
        }
        if (methods.size() == 0) {
            findPreference(KEY_ERROR_NOTIFY).setSummary(R.string.pref_error_notify_none);
        } else {
            String summaryStr = join(methods, getString(R.string.pref_error_notify_deliminator));
            findPreference(KEY_ERROR_NOTIFY).setSummary(summaryStr);
        }
    }

    private void updateSuccessNotificationSummary() {
        ArrayList<String> methods = new ArrayList<String>();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (prefs.getBoolean(Preferences.KEY_SUCCESS_NOTIFY_SOUND, false)) {
            methods.add(getString(R.string.pref_success_notify_sound));
        }
        if (prefs.getBoolean(Preferences.KEY_SUCCESS_NOTIFY_VIBRATE, false)) {
            methods.add(getString(R.string.pref_success_notify_vibrate));
        }
        if (prefs.getBoolean(Preferences.KEY_SUCCESS_NOTIFY_LIGHTS, false)) {
            methods.add(getString(R.string.pref_success_notify_lights));
        }
        if (prefs.getBoolean(Preferences.KEY_SUCCESS_NOTIFY_TOAST, false)) {
            methods.add(getString(R.string.pref_success_notify_toast));
        }

        if (methods.size() == 0) {
            findPreference(KEY_SUCCESS_NOTIFY).setSummary(R.string.pref_success_notify_none);
        } else {
            String summaryStr = join(methods, getString(R.string.pref_success_notify_deliminator));
            findPreference(KEY_SUCCESS_NOTIFY).setSummary(summaryStr);
        }
    }

    private void updateDisconnectNotificationSummary() {
        ArrayList<String> methods = new ArrayList<String>();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        if (prefs.getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_SOUND, false)) {
            methods.add(getString(R.string.pref_disconnect_notify_sound));
        }
        if (prefs.getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_VIBRATE, false)) {
            methods.add(getString(R.string.pref_disconnect_notify_vibrate));
        }
        if (prefs.getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_LIGHTS, false)) {
            methods.add(getString(R.string.pref_disconnect_notify_lights));
        }
        if (prefs.getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_TOAST, false)) {
            methods.add(getString(R.string.pref_disconnect_notify_toast));
        }

        if (methods.size() == 0) {
            findPreference(KEY_DISCONNECT_NOTIFY).setSummary(R.string.pref_disconnect_notify_none);
        } else {
            String summaryStr = join(methods, getString(R.string.pref_disconnect_notify_deliminator));
            findPreference(KEY_DISCONNECT_NOTIFY).setSummary(summaryStr);
        }
    }

    private void updateLanguageSummary() {
        ListPreference listPref = (ListPreference) findPreference(KEY_LANGUAGE);
        listPref.setSummary(listPref.getEntry());
    }

    private static String join(Collection<String> col, String deliminator) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = col.iterator();
        sb.append(iter.next());
        while (iter.hasNext()) {
            sb.append(deliminator);
            sb.append(iter.next());
        }
        return sb.toString();
    }
}