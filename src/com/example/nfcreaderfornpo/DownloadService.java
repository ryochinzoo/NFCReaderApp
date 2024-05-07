package com.example.nfcreaderfornpo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadService extends AsyncTask<String, Void, Boolean>{
	static final String TAG = "Download1";
	private final int TIMEOUT_READ = 5000;
	private final int TIMEOUT_CONNECT = 30000;
	
	public Activity owner;
	private final int BUFFER_SIZE = 1024;
	
	private String urlString;
	private File outputFile;
	private FileOutputStream fileOutStream;
	private InputStream inputStream;
	private BufferedInputStream bufferedInputStream;
	
	private int totalByte = 0;
	private int currentByte = 0;
	
	private byte[] buffer = new byte[BUFFER_SIZE];
	
	private URL url;
	private URLConnection urlConn;
	
	public DownloadService(Activity activity, String url, File oFile){
		owner = activity;
		urlString = url;
		outputFile = oFile;
	}
	

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		try{
			connect();
		}catch(IOException e){
			Log.d(TAG ,"ConnectError:" + e.toString());
			cancel(true);
		}
		if(isCancelled()){
			return false;
		}
		if(bufferedInputStream != null){
			try{
				int len;
				while((len = bufferedInputStream.read(buffer)) != -1){
					fileOutStream.write(buffer, 0, len);
					currentByte += len;
					if(isCancelled()){
						break;
					}
				}
			}catch(IOException e){
				Log.d(TAG, e.toString());
				return false;
			}
		}else{
			Log.d(TAG, "bufferedInputStream is null");
		}
		try{
			close();
		}catch(IOException e){
			Log.d(TAG,"CloseError:" + e.toString());
		}
		return true;
	}


	private void close() throws IOException {
		// TODO Auto-generated method stub
			fileOutStream.flush();
			fileOutStream.close();
			bufferedInputStream.close();
	}


	private void connect() throws IOException{
		// TODO Auto-generated method stub
		url = new URL(urlString);
		urlConn = url.openConnection();
		urlConn.setReadTimeout(TIMEOUT_READ);
		urlConn.setConnectTimeout(TIMEOUT_CONNECT);
		inputStream = urlConn.getInputStream();
		bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
		fileOutStream = new FileOutputStream(outputFile);
		
		totalByte = urlConn.getContentLength();
		currentByte = 0;
	}
	
	
}
