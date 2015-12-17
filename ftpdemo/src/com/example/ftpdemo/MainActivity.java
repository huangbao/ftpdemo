package com.example.ftpdemo;

import java.io.File;
import java.net.InetAddress;


import org.swiftp.Defaults;
import org.swiftp.Globals;
import org.swiftp.TcpListener;
import org.swiftp.UiUpdater;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	public static final String TAG = "MainActivity";
	private Activity mActivity;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				removeMessages(0);
				updateUi();
				break;

			default:
				break;
			}
		};
	};
	TextView ipText, instructionText, instructionTextPre;
	View startStopButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		
		Globals.setContext(mActivity);
		
		setContentView(R.layout.activity_main);
		startStopButton = findViewById(R.id.start_stop_button);
		startStopButton.setOnClickListener(this);
		ipText = (TextView) findViewById(R.id.ip_address);
		instructionText = (TextView) findViewById(R.id.instruction);
		instructionTextPre = (TextView) findViewById(R.id.instruction_pre);
		updateUi();
		UiUpdater.registerClient(handler);

	}
    private void setText(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }
   @Override
protected void onStart() {
	super.onStart();
	updateUi();
}
	protected void updateUi() {

		WifiManager wifiMgr = (WifiManager) mActivity
				.getSystemService(Context.WIFI_SERVICE);
		int wifiState = wifiMgr.getWifiState();
		WifiInfo info = wifiMgr.getConnectionInfo();
		String wifiId = info != null ? info.getSSID() : null;
		boolean isWifiReady = FTPServerService.isWifiEnabled();

		setText(R.id.wifi_state, isWifiReady ? wifiId
				: getString(R.string.no_wifi_hint));
		ImageView wifiImg = (ImageView) findViewById(R.id.wifi_state_image);
		wifiImg.setImageResource(isWifiReady ? R.drawable.wifi_state4
				: R.drawable.wifi_state0);

		boolean running = FTPServerService.isRunning();
		if (running) {
			// Put correct text in start/stop button
			// Fill in wifi status and address
			InetAddress address = FTPServerService.getWifiIp();
			if (address != null) {
				String port = ":" + FTPServerService.getPort();
				ipText.setText("ftp://" + address.getHostAddress()
						+ (FTPServerService.getPort() == 21 ? "" : port));

			} else {
				// could not get IP address, stop the service
				Context context = mActivity.getApplicationContext();
				Intent intent = new Intent(context, FTPServerService.class);
				context.stopService(intent);
				ipText.setText("");
			}
		}

		startStopButton.setEnabled(isWifiReady);
		TextView startStopButtonText = (TextView) findViewById(R.id.start_stop_button_text);
		if (isWifiReady) {
			startStopButtonText.setText(running ? R.string.stop_server
					: R.string.start_server);
			startStopButtonText.setCompoundDrawablesWithIntrinsicBounds(
					running ? R.drawable.disconnect : R.drawable.connect, 0, 0,
					0);
			startStopButtonText.setTextColor(running ? getResources().getColor(
					R.color.remote_disconnect_text) : getResources().getColor(
					R.color.remote_connect_text));
		} else {
			if (FTPServerService.isRunning()) {
				Context context = mActivity.getApplicationContext();
				Intent intent = new Intent(context, FTPServerService.class);
				context.stopService(intent);
			}

			startStopButtonText.setText(R.string.no_wifi);
			startStopButtonText.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					0, 0);
			startStopButtonText.setTextColor(Color.GRAY);
		}

		ipText.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
		instructionText.setVisibility(running ? View.VISIBLE : View.GONE);
		instructionTextPre.setVisibility(running ? View.GONE : View.VISIBLE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UiUpdater.unregisterClient(handler);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.start_stop_button) {
			File file = new File(Defaults.chrootDir);
			if (!file.isDirectory()) {
				return;
			}
			Intent intent = new Intent(MainActivity.this,
					FTPServerService.class);
			if (!FTPServerService.isRunning()) {
				if (Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					startService(intent);
				} else {
					Toast.makeText(MainActivity.this, "外部存储不可用",
							Toast.LENGTH_LONG);
				}

			} else {
				stopService(intent);
			}
		}

	}

}
