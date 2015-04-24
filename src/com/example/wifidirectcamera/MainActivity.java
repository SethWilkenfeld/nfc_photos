/*
	Project ... Final Project
	File ..	... MainActivity.java
	Name ..	... Gardner Seth Wilkenfeld

	Credits: Much of the included code comes from the Android API and other internet resources
		including answers to similar problems on StackOverflow
*/

package com.example.wifidirectcamera;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements PeerListListener, ConnectionInfoListener {

	final static String TAG = "WiFiDirectCamera";
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	private Uri fileUri;
	private WifiP2pInfo info;

	/* Device connection */
	private WifiP2pDevice my_device;
	private WifiP2pDevice device;
	private WifiP2pConfig config;

	/* List view and adapter */
	private DeviceListAdapter mAdapter;
	private ListView myListView;
	private ArrayList<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();

	/* These objects go with #3 in onCreate() */
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WiFiDirectBroadcastReceiver mReceiver;

	/* This goes with #4 in onCreeate */
	private IntentFilter mIntentFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
			3. In your activity's onCreate() method, obtain an instance of WifiP2pManager
			and register your application with the Wi-Fi P2P framework by calling initialize().
			You should also create an instance of your broadcast receiver with the WifiP2pManager
			and WifiP2pManager.Channel objects along with a reference to your activity.
		*/
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

		/* 4. Create an intent filter and add the same intents that your broadcast receiver checks for */
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		/* List view */
		myListView = (ListView) findViewById(R.id.device_list);

		/* Begin discovery process immediately */
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Toast.makeText(MainActivity.this, "Discovery Initiated",
				Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reasonCode) {
				Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
				Toast.LENGTH_SHORT).show();
			}
		});

		/* Disconnect button click listener */
		findViewById(R.id.btn_disconnect).setOnClickListener(
			new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mManager.removeGroup(mChannel, new ActionListener() {

						@Override
						public void onFailure(int reasonCode) {
							Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
						}

						@Override
						public void onSuccess() {
							// Placeholder
						}
	
					});
				}
			}
		);

		/* Open camera button click listener */
		findViewById(R.id.btn_start_camera).setOnClickListener(
			new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Allow user to pick an image from Gallery or other
					// registered apps
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
					intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

					// start the image capture Intent
					startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			}
		);

		findViewById(R.id.btn_start_gallery).setOnClickListener(
			new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Allow user to pick an image from Gallery or other
					// registered apps
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
				}
			}
		);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// User has picked an image. Transfer it to group owner i.e peer using
		// FileTransferService.
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(MainActivity.this, "Sending: " + fileUri, Toast.LENGTH_SHORT).show();
				Intent serviceIntent = new Intent(this, FileTransferService.class);
				serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
				serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, fileUri.toString());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
				info.groupOwnerAddress.getHostAddress());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
				startService(serviceIntent);
			}
		} else if(requestCode == CHOOSE_FILE_RESULT_CODE) {
			if(data != null) {
				fileUri = data.getData();
				Toast.makeText(MainActivity.this, "Sending: " + fileUri, Toast.LENGTH_SHORT).show();
				Intent serviceIntent = new Intent(this, FileTransferService.class);
				serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
				serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, fileUri.toString());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
				info.groupOwnerAddress.getHostAddress());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
				startService(serviceIntent);
			}
		}
	}

	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			"MyCameraApp"
		);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
				"IMG_"+ timeStamp + ".jpg");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	*	5. Register the broadcast receiver in the onResume() method of your activity
		and unregister it in the onPause() method of your activity:

	*	Register the broadcast receiver with the intent values to be matched
	*/
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mIntentFilter);
	}

	/* Unregister the broadcast receiver */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {

		mPeers.clear();
		mPeers.addAll(peerList.getDeviceList());
		mAdapter = new DeviceListAdapter(this, mPeers);
		myListView.setAdapter(mAdapter);

		/* Setup onClickListener for ListView */
		myListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				device = mAdapter.getItem(position);
				config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				mManager.connect(mChannel, config, new ActionListener() {

					@Override
					public void onSuccess() {
						//success logic
					}

					@Override
					public void onFailure(int reason) {
						//failure logic
					}
				});
			}
		});
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo i) {

		info = i;
		// After the group negotiation, we assign the group owner as the file
		// server. The file server is single threaded, single connection server
		// socket.
		if (info.groupFormed && info.isGroupOwner) {
			new FileServerAsyncTask(this, findViewById(R.id.device_details)).execute();
		} else if (info.groupFormed) {
			// The other device acts as the client. In this case, we enable the
			// get file button.
			findViewById(R.id.btn_start_camera).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_start_gallery).setVisibility(View.VISIBLE);
		}
	}

	public void resetData() {
		findViewById(R.id.btn_start_camera).setVisibility(View.GONE);
		findViewById(R.id.btn_start_gallery).setVisibility(View.GONE);
	}

	public void updateThisDevice(WifiP2pDevice device) {
		this.my_device = device;
		TextView view = (TextView) findViewById(R.id.my_device_name);
		view.setText(my_device.deviceName);
		view = (TextView) findViewById(R.id.my_device_details);
		view.setText(WiFiDirectBroadcastReceiver.getDeviceStatus(my_device.status));
	}

}