/*
	Project ... Final Project
	File ..	... WiFiDirectBroadcastReceiver.java
	Name ..	... Gardner Seth Wilkenfeld

	Credits: Much of the included code comes from the Android API and other internet resources
		including answers to similar problems on StackOverflow
*/

package com.example.wifidirectcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private MainActivity mActivity;
	private PeerListListener myPeerListListener;

	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
		this.myPeerListListener = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			/*
			2. Check to see if Wi-Fi P2P is on and supported. A good place to check this is
			in your broadcast receiver when it receives the WIFI_P2P_STATE_CHANGED_ACTION
			intent. Notify your activity of the Wi-Fi P2P state and react accordingly:
			*/
			if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					// Wifi P2P is enabled
				} else {
					// Wi-Fi P2P is not enabled
				}
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// Call WifiP2pManager.requestPeers() to get a list of current peers
			if (mManager != null) {
				mManager.requestPeers(mChannel, myPeerListListener);
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

			// Respond to new connection or disconnections
			if (mManager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
				WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {

				// we are connected with the other device, request connection
				// info to find group owner IP

				mManager.requestConnectionInfo(mChannel, (ConnectionInfoListener) mActivity);
			} else {
				// It's a disconnect
				mActivity.resetData();
			}

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

			// Respond to this device's wifi state changing
			mActivity.updateThisDevice(
				(WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
			);
		}
	}

	/* Returns the String value equivalent to the status code provided */
	static CharSequence getDeviceStatus(int deviceStatus) {
		switch (deviceStatus) {
			case WifiP2pDevice.AVAILABLE:
				return "Available";
			case WifiP2pDevice.INVITED:
				return "Invited";
			case WifiP2pDevice.CONNECTED:
				return "Connected";
			case WifiP2pDevice.FAILED:
				return "Failed";
			case WifiP2pDevice.UNAVAILABLE:
				return "Unavailable";
			default:
				return "Unknown";
		}
	}
}
