package it.topnet.aliseo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class Utils {
	public static String stackTraceToString(Exception e) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        e.printStackTrace(ps);
        String ret = os.toString();
        ps.close();
        return ret;
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "?";
        }
    }

    public static void loadLocale(Context context) {
        String lang = PreferenceManager.getDefaultSharedPreferences(context).getString(Preferences.KEY_LANGUAGE, Preferences.LANGUAGE_DEFAULT);
        Configuration config = new Configuration();
        if (!lang.equals(Preferences.LANGUAGE_DEFAULT)) {
            config.locale = new Locale(lang);
        } else {
            config.locale = Locale.getDefault();
        }
        context.getResources().updateConfiguration(config, null);
    }

    public static void setEnableBroadcastReceiver(Context context, boolean enabled) {
        ComponentName receiver = new ComponentName(context, WifiStatusReceiver.class);
        int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        context.getPackageManager().setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
    }

    public static boolean isConnectedToCorrectSSID(Context context) {
		String ssid = cleanSSID((((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo()).getSSID());
		return ssid != null && ssid.equals(Constants.SSID);
	}

    private static String cleanSSID(String ssid) {
		String res = null;
		if (ssid != null) {
			res = ssid.replace("\"", "");
		}
		return res;
	}

}
