package com.example.nfcreaderfornpo;
import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class UploadAsyncTask 
  extends AsyncTask<String, Integer, Integer> {
  ProgressDialog dialog;
  Context context;
  
  public UploadAsyncTask(Context context){
    this.context = context;
  }
  
  @Override
  protected Integer doInBackground(String... params) {

    try {
      String fileName = params[0];
      
      HttpClient httpClient = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost("http://27.120.105.182/phppost.php");
      ResponseHandler<String> responseHandler =
        new BasicResponseHandler();
      MultipartEntity multipartEntity =
        new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      
      File file = new File(fileName);
      FileBody fileBody = new FileBody(file);
      multipartEntity.addPart("f1", fileBody);
      
      httpPost.setEntity(multipartEntity);
      httpClient.execute(httpPost, responseHandler);
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return 0;
  }

  @Override
  protected void onPostExecute(Integer result) {
    if(dialog != null){
      dialog.dismiss();
      AlertDialog.Builder adb = new AlertDialog.Builder(this.context);
      adb.setTitle("送信が完了しました。");
      adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
      AlertDialog ad = adb.create();
      ad.show();
    }
  }

  @Override
  protected void onPreExecute() {
    dialog = new ProgressDialog(context);
    dialog.setTitle("Please wait");
    dialog.setMessage("Uploading...");
    dialog.show();
  }  
}