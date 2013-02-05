package it.topnet.aliseo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class LoginService extends IntentService {
    static final String TAG = "LoginService";
    static final int ACTION_LOGIN = 1;
    static final int ACTION_LOGOUT = 2;
    static final int ACTION_DISCONNECT = 3;
    static final int NOTIFY_ONGOING_CONNECTED_ID = 1;
    static final int NOTIFY_MESSAGE_ID = 2;
    static final String EXTRA_ACTION = "it.topnet.aliseo.loginservice.extra";
    private Context mContext;
    private Handler mHandler;
    private LoginClient mLoginClient;

    public LoginService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.loadLocale(this);
        mContext = this;
        mHandler = new Handler();
    	mLoginClient = new LoginClient(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	switch(intent.getIntExtra(EXTRA_ACTION, ACTION_DISCONNECT)) {
		case ACTION_DISCONNECT: // From event wifi disabled or event network disconnect
        	Log.d(TAG, "ACTION_DISCONNECT");
        	cleanAllNotification();
        	createDisconnectedNotification();
    		break;
		case ACTION_LOGIN: // From manual login or event connect to correct ap
            Log.d(TAG, "ACTION_LOGIN");
   			if (Utils.isConnectedToCorrectSSID(mContext)) {
	            if (!mLoginClient.isLoggedIn()) {
	                Log.d(TAG, "NOT LOGGED IN, LOGIN REQUIRED");
	                switch(mLoginClient.login()) {
	                case LoginClient.LOGIN_SUCCESSFUL:
		            	Log.d(TAG, "LOGIN SUCCESSFUL");
		            	cleanAllNotification();
		                createLoginSuccessfulNotification();
		                break;
	                case LoginClient.LOGIN_REFUSED:
		            	Log.d(TAG, "LOGIN REFUSED");
		            	cleanAllNotification();
		            	createLoginRefusedNotification();
		            	break;
	                case LoginClient.LOGIN_IOERROR:
		            	Log.d(TAG, "LOGIN IOERROR");
		            	cleanAllNotification();
		            	createLoginIOErrorNotification();
		            	break;
	                }
		        } else {
		        	Log.d(TAG, "ALREADY LOGGED IN, LOGIN NOT REQUIRED");
	            	cleanAllNotification();
	                createLoginSuccessfulNotification();
		        }
   			} else {
	        	Log.d(TAG, "NOT CONNECTED TO ALISEO AP, LOGIN NOT POSSIBLE");
	        	cleanAllNotification();
	        	createDisconnectedNotification();
   			}
   			break;
		case ACTION_LOGOUT: // From manual logout
            Log.d(TAG, "ACTION_LOGOUT");
   			if (Utils.isConnectedToCorrectSSID(mContext)) {
	            if (mLoginClient.isLoggedIn()) {
	                switch(mLoginClient.logout()) {
	                case LoginClient.LOGOUT_SUCCESSFUL:
		            	Log.d(TAG, "LOGOUT SUCCESSFUL");
		                break;
	                case LoginClient.LOGOUT_REFUSED:
		            	Log.d(TAG, "LOGOUT REFUSED");
		            	break;
	                case LoginClient.LOGOUT_IOERROR:
		            	Log.d(TAG, "LOGOUT IOERROR");
		            	break;
	                }
	            } else {
		        	Log.d(TAG, "ALREADY LOGGED OUT, LOGOUT NOT REQUIRED");
		        }
   			} else {
 	        	Log.d(TAG, "NOT CONNECTED TO ALISEO AP, LOGOUT NOT POSSIBLE");
   			}
        	cleanAllNotification();
        	createDisconnectedNotification();
    		break;
        }
   }

    private void createLoginSuccessfulNotification() {
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_NOTIFY_SUCCESS, true)) {
           return;
        }
       	String tickerText = getString(R.string.login_successful_ticker);
    	String contentTitle = getString(R.string.login_successful_title);
    	String contentText = getString(R.string.login_successful_content);
		Intent appIntent = new Intent(mContext, WWWrapper.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, appIntent, 0);
		Notification notification = new Notification(R.drawable.ic_stat_aliseo, tickerText, System.currentTimeMillis());
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, pendingIntent);
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_SUCCESS_NOTIFY_SOUND, false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_SUCCESS_NOTIFY_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_SUCCESS_NOTIFY_LIGHTS, false)) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_SUCCESS_NOTIFY_TOAST, false)) {
            createToastNotification(mHandler, tickerText);
        }
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ONGOING_CONNECTED_ID, notification);
    }

    private void createDisconnectedNotification() {
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_NOTIFY_DISCONNECT, false)) {
            return;
        }
    	String tickerText = getString(R.string.login_disconnected_ticker);
    	String contentTitle = getString(R.string.login_disconnected_title);
    	String contentText = getString(R.string.login_disconnected_content);
    	PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
		Notification notification = new Notification(R.drawable.ic_stat_disconnected, tickerText, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, pendingIntent);
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_SOUND, false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_LIGHTS, false)) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_DISCONNECT_NOTIFY_TOAST, false)) {
            createToastNotification(mHandler, tickerText);
        }
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_MESSAGE_ID, notification);
    }

    private void createLoginRefusedNotification() {
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_NOTIFY_ERROR, false)) {
            return;
        }
    	String tickerText = getString(R.string.login_refused_ticker);
    	String contentTitle = getString(R.string.login_refused_title);
    	String contentText = getString(R.string.login_refused_content);
    	PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
		Notification notification = new Notification(R.drawable.ic_stat_error, tickerText, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, pendingIntent);
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_SOUND, false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_LIGHTS, false)) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_TOAST, false)) {
            createToastNotification(mHandler, tickerText);
        }
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_MESSAGE_ID, notification);
    }

    private void createLoginIOErrorNotification() {
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_NOTIFY_ERROR, false)) {
            return;
        }
    	String tickerText = getString(R.string.login_io_error_ticker);
    	String contentTitle = getString(R.string.login_io_error_title);
    	String contentText = getString(R.string.login_io_error_content);
    	Intent appIntent = new Intent(mContext, LoginService.class);
    	appIntent.putExtra(LoginService.EXTRA_ACTION, ACTION_LOGIN);
		PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, appIntent, 0);
		Notification notification = new Notification(R.drawable.ic_stat_retry, tickerText, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, pendingIntent);
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_SOUND, false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_LIGHTS, false)) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Preferences.KEY_ERROR_NOTIFY_TOAST, false)) {
            createToastNotification(mHandler, tickerText);
        }
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_MESSAGE_ID, notification);
    }
 
    private void createToastNotification(Handler handler, final String message) {
    	handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

	private void cleanAllNotification() {
		((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
	}

	@SuppressWarnings("unused")
	private void cleanLoginSuccessfulNotification() {
		((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFY_ONGOING_CONNECTED_ID);
	}

	@SuppressWarnings("unused")
	private void cleanMessageNotification() {
		((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFY_MESSAGE_ID);
	}

}
