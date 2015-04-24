/*
	Project ... Final Project
	File ..	... FileServerAsyncTask.java
	Name ..	... Gardner Seth Wilkenfeld

	Credits: Much of the included code comes from the Android API and other internet resources
		including answers to similar problems on StackOverflow
*/

package com.example.wifidirectcamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

	private Context context;
	private TextView statusText;

	/*
		@param context
		@param statusText
	*/
	public FileServerAsyncTask(Context context, View statusText) {
		this.context = context;
		this.statusText = (TextView) statusText;
	}

	/*
		A simple server socket that accepts connection and writes some data on
		the stream.
	*/
	@Override
	protected String doInBackground(Void... params) {
		try {
			ServerSocket serverSocket = new ServerSocket(8988);
			Log.d(MainActivity.TAG, "Server: Socket opened");
			Socket client = serverSocket.accept();
			Log.d(MainActivity.TAG, "Server: connection done");
			final File f = new File(Environment.getExternalStorageDirectory() + "/"
				+ context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
				+ ".jpg");

			File dirs = new File(f.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			f.createNewFile();

			Log.d(MainActivity.TAG, "server: copying files " + f.toString());
			InputStream inputstream = client.getInputStream();
			copyFile(inputstream, new FileOutputStream(f));
			serverSocket.close();
			return f.getAbsolutePath();
		} catch (IOException e) {
			Log.e(MainActivity.TAG, e.getMessage());
			return null;
		}
	}

	/*
		(non-Javadoc)
		@see android.os.AsyncTask#onPostExecute(java.lang.Object)
	*/
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			statusText.setText("File copied - " + result);
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + result), "image/*");
			context.startActivity(intent);
		}
	}

	/*
		(non-Javadoc)
		@see android.os.AsyncTask#onPreExecute()
	*/
	@Override
	protected void onPreExecute() {
		statusText.setText("Opening a server socket");
	}

	/* File copy utility for the AsyncTask */
	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[1024];
		int len;

		try {

			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);
			}

			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, e.toString());
			return false;
		}
		return true;
	}
}