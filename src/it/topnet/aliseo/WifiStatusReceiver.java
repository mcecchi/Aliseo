package it.topnet.aliseo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiStatusReceiver extends BroadcastReceiver {
    static final String TAG = "WifiStatusReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
    	if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
    		NetworkInfo.DetailedState detailedState = networkInfo.getDetailedState();
    		if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
       			if (Utils.isConnectedToCorrectSSID(context)) {
	   				Log.d(TAG, "NETWORK_STATE_CONNECTED (NETWORK_STATE_CHANGED_ACTION)");
					Intent loginIntent = new Intent(context, LoginService.class);
					loginIntent.putExtra(LoginService.EXTRA_ACTION, LoginService.ACTION_LOGIN);
					context.startService(new Intent(loginIntent));
       			}
	    	}
    		else {
        		if (detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
        			Log.d(TAG, "NETWORK_STATE_DISCONNECTED (NETWORK_STATE_CHANGED_ACTION)");
					Intent loginIntent = new Intent(context, LoginService.class);
					loginIntent.putExtra(LoginService.EXTRA_ACTION, LoginService.ACTION_DISCONNECT);
					context.startService(new Intent(loginIntent));
        		}
    		}
    	} else {
	    	if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
	    		int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
	    		if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
	    			Log.d(TAG, "WIFI_STATE_DISABLED (WIFI_STATE_CHANGED_ACTION)");
					Intent loginIntent = new Intent(context, LoginService.class);
					loginIntent.putExtra(LoginService.EXTRA_ACTION, LoginService.ACTION_DISCONNECT);
					context.startService(new Intent(loginIntent));
		    	}
	    	}
    	}
    }

}
