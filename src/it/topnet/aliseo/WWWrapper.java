package it.topnet.aliseo;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class WWWrapper extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_wwwrapper);
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		WebSettings webSettings = webView.getSettings();
		webSettings.setSupportZoom(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setAppCacheEnabled(false);
		webView.addJavascriptInterface(new JavascriptInterface(this, webView),
				getString(R.string.wrapper_name));
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());
		webView.loadUrl(getString(R.string.app_url));
	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onGeolocationPermissionsShowPrompt(final String origin,
				final GeolocationPermissions.Callback callback) {
			super.onGeolocationPermissionsShowPrompt(origin, callback);
			callback.invoke(origin, true, false);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final JsResult result) {
			new AlertDialog.Builder(WWWrapper.this)
					.setTitle(getString(R.string.dialog_title_alert))
					.setMessage(message)
					.setPositiveButton(getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									result.confirm();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									result.cancel();
								}
							}).show();
			return true;
		}

		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				final JsResult result) {
			new AlertDialog.Builder(WWWrapper.this)
					.setTitle(getString(R.string.dialog_title_confirm))
					.setMessage(message)
					.setPositiveButton(getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									result.confirm();
								}
							})
					.setNegativeButton(getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									result.cancel();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									result.cancel();
								}
							}).show();
			return true;
		}

		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, final JsPromptResult result) {
			final EditText editText = new EditText(WWWrapper.this);
			editText.setText(defaultValue);
			new AlertDialog.Builder(WWWrapper.this)
					.setTitle(getString(R.string.dialog_title_prompt))
					.setMessage(message)
					.setView(editText)
					.setPositiveButton(getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									result.confirm(editText.getText()
											.toString());
								}
							})
					.setNegativeButton(getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									result.cancel();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									result.cancel();
								}
							}).show();
			return true;
		}

		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceId) {
			String tag = getString(R.string.app_name);
			String logMessage = message + " [Source: "
					+ sourceId + "] [Line: "
					+ lineNumber + "]";
			Log.d(tag, logMessage);
		}

	}

	private class MyWebViewClient extends WebViewClient {
		ProgressDialog progressDialog;

		@Override
		public void onPageStarted(final WebView view, String url, Bitmap favicon) {
			progressDialog = new ProgressDialog(WWWrapper.this);
			progressDialog.setMessage(getString(R.string.progress_message));
			progressDialog.setIndeterminate(true);
			progressDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							view.stopLoading();
							finish();
						}
					});
			progressDialog.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			progressDialog.dismiss();

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (Uri.parse(url).getHost().equals(getString(R.string.app_host))) {
				return false;
			}
			return true;
		}

		@Override
		public void onReceivedError(final WebView view, int errorCode,
				String description, final String failingUrl) {
			progressDialog.dismiss();
			view.loadUrl("about:blank");
			new AlertDialog.Builder(WWWrapper.this)
					.setTitle(getString(R.string.dialog_title_error))
					.setMessage(getString(R.string.error_communication))
					.setPositiveButton(getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									dialog.dismiss();
									finish();
								}
							}).show();
		}
	}

	private class JavascriptInterface {

		Context mContext;
		WebView mWebView;

		JavascriptInterface(Context c, WebView w) {
			mContext = c;
			mWebView = w;
		}

		@SuppressWarnings("unused")
		public void readGPS(final String callbackGPS) {
			final LocationManager locationManager;
			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					locationManager.removeUpdates(this);
					mWebView.loadUrl("javascript:" + callbackGPS + "("
							+ location.getLatitude() + ","
							+ location.getLongitude() + ")");
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
				}

				@Override
				public void onProviderEnabled(String provider) {
				}

				@Override
				public void onProviderDisabled(String provider) {
				}
			};
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 20, locationListener);
		}

		@SuppressWarnings("unused")
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
		}

		@SuppressWarnings("unused")
		public void startNavigation(double lat, double lng) {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + lat + "," + lng));
			mContext.startActivity(intent);
		}
	}
}
