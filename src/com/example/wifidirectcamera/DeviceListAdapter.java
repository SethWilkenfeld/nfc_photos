/*
	Project ... Final Project
	File ..	... DeviceListAdapter.java
	Name ..	... Gardner Seth Wilkenfeld

	Credits: Much of the included code comes from the Android API and other internet resources
		including answers to similar problems on StackOverflow
*/

package com.example.wifidirectcamera;

import java.util.ArrayList;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {

	private ArrayList<WifiP2pDevice> devices; // List of devices
	private Context context;

	public DeviceListAdapter(Context context, ArrayList<WifiP2pDevice> d) {
		super(context, R.layout.device_list_view, d);
		this.context = context;
		this.devices = d;
	}

	/* Populates the list view with the list item objects */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.device_list_view, parent, false);
		}

		/* Set the layout template */
		WifiP2pDevice device = devices.get(position);
		if (device != null) {
			TextView top = (TextView) convertView.findViewById(R.id.device_name);
			TextView bottom = (TextView) convertView.findViewById(R.id.device_details);
			if (top != null) {
				top.setText(device.deviceName);
			}
			if (bottom != null) {
				bottom.setText(WiFiDirectBroadcastReceiver.getDeviceStatus(device.status));
			}
		}

		return convertView;
	}
}
