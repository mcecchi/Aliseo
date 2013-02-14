package it.topnet.aliseo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

public class LoginClient {
    static final String TAG = "LoginClient";
    static final int CONNECTION_TIMEOUT = 3000;
    static final int SOCKET_TIMEOUT = 3000;
    static final int RETRY_COUNT = 3;
    static final int LOGIN_SUCCESSFUL = 1;
    static final int LOGIN_REFUSED = 2;
    static final int LOGIN_IOERROR = 3;
    static final int LOGOUT_SUCCESSFUL = 1;
    static final int LOGOUT_REFUSED = 2;
    static final int LOGOUT_IOERROR = 3;

    private Context mContext;
    private DefaultHttpClient mHttpClient;

    public LoginClient(Context context) {
    	mContext = context;
    	mHttpClient = getDefaultHttpClient();
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
    }

    private DefaultHttpClient getDefaultHttpClient() {
    	SSLSocketFactory sslFactory = null;
    	try {
			sslFactory = new NaiveSSLSocketFactory(null);
		} catch (KeyManagementException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (KeyStoreException e) {
		} catch (UnrecoverableKeyException e) {
		}
    	if (sslFactory != null) {
	    	sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    	HttpParams httpParams = new BasicHttpParams();
	    	SchemeRegistry registry = new SchemeRegistry();
	    	registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    	registry.register(new Scheme("https", sslFactory, 443));
	    	ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams, registry);
	    	return new DefaultHttpClient(ccm, httpParams);
    	} else {
        	return new DefaultHttpClient();
    	}
    }

    public int login() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String stringaAuth = prefs.getString(Preferences.KEY_STRINGAAUTH, null);
        try {
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAPPALISEO, Constants.STRINGAAPPALISEO));
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAUTH, stringaAuth));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            HttpPost httpPost = new HttpPost(Constants.LOGIN_URL);
            httpPost.setEntity(entity);
            HttpResponse response = mHttpClient.execute(httpPost);
		    String strRes = getResponseFragment(response, Constants.LOGIN_SUCCESSFUL_PATTERN.length());
            if (strRes.equals(Constants.LOGIN_SUCCESSFUL_PATTERN)) {
                return LOGIN_SUCCESSFUL;
            } else {
                return LOGIN_REFUSED;
            }
        } catch (IOException e) {
        	return LOGIN_IOERROR;
        }
    }

    public int logout() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String stringaAuth = prefs.getString(Preferences.KEY_STRINGAAUTH, null);
        try {
	        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAPPALISEO, Constants.STRINGAAPPALISEO));
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAUTH, stringaAuth));
	        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
	        HttpPost httppost = new HttpPost(Constants.LOGOUT_URL);
	        httppost.setEntity(entity);
	        HttpResponse response = mHttpClient.execute(httppost);
		    String strRes = getResponseFragment(response, Constants.LOGOUT_SUCCESSFUL_PATTERN.length());
	        if (strRes.equals(Constants.LOGOUT_SUCCESSFUL_PATTERN)) {
	            return LOGOUT_SUCCESSFUL;
	        } else {
	        	return LOGOUT_REFUSED;
	        }
        } catch (IOException e) {
        	return LOGOUT_IOERROR;
        }
    }

    public boolean isLoggedIn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String stringaAuth = prefs.getString(Preferences.KEY_STRINGAAUTH, null);
        try {
	        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAPPALISEO, Constants.STRINGAAPPALISEO));
            formparams.add(new BasicNameValuePair(Constants.FORM_STRINGAAUTH, stringaAuth));
	        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
	        HttpPost httppost = new HttpPost(Constants.CHECK_URL);
	        httppost.setEntity(entity);
	        HttpResponse response = mHttpClient.execute(httppost);
		    String strRes = getResponseFragment(response, Constants.CHECK_SUCCESSFUL_PATTERN.length());
	        if (strRes.equals(Constants.CHECK_SUCCESSFUL_PATTERN)) {
	            return true;
	        } else {
	        	return false;
	        }
        } catch (IOException e) {
        	return false;
        }
    }

    public boolean isInternetBlocked() {
		try {
		    HttpGet httpget = new HttpGet(Constants.PING_URL);
		    HttpResponse response = mHttpClient.execute(httpget);
		    String strRes = getResponseFragment(response, Constants.PING_OK_PATTERN.length());
            if (strRes.equals(Constants.PING_OK_PATTERN)) {
                return false;
            } else {
                return true;
            }
		} catch (IOException e) {
		    return true;
		}
    }

    private String getResponseFragment(HttpResponse response, int length) {
	    HttpEntity entity = response.getEntity();
	    InputStream is;
		try {
			is = entity.getContent();
		    byte[] buf = new byte[length];
		    is.read(buf, 0, length);
		    String strRes = new String(buf).trim();
		    Log.d(TAG, "HTTP RESPONSE: " + strRes);
		    return strRes;
		} catch (IOException e) {
			return "";
		}
    }
}
