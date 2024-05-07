package com.example.nfcreaderfornpo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.http.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

interface FileManagePr{
	public void fileLoads(String path);
	public void setEventName(String name);
	public String getEventName();
}

interface MailLauncher{
	public void openMailer();
	public String loadOutputs();
}

public class LaunchedActivity extends Activity implements FileManagePr, MailLauncher{
	private final String DOWNLOAD_FILE_URL_EVENT = "http://27.120.105.182/event_and_member/event.csv";
	private final String DOWNLOAD_FILE_URL_MEMBERS = "http://27.120.105.182/event_and_member/member.csv";
	
	private DownloadService ds1;
	private DownloadService ds2;
	
	static String[] mailto = new String[1];
	//static String[] paths = new String[1];
	static String pathforsaving;
	static SQLiteDatabase evtdb;
	UploadAsyncTask uat;
	private ArrayList<String>[] event_list;
	private Button pmain, pconfig, pmailer;
	private String path;
	private String name;
	AlertDialog.Builder mListDlg;
	Cursor result_c, result_n;
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	boolean c_isEof;
	Globals glb;
	//LaunchedActivity(String path){
		//this.path = path;
	//}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startpage);
		
		fileLoads(this.path);
		glb = (Globals)this.getApplication();
		glb.g_init();
		if(!glb.connectflag){
			ArrayList<String>[] c_members = loadCSV(glb.members, 4);
			glb.mdbconn(c_members);
			glb.connectflag = true;//メール送信したらFalseに切り替え
		}
		//2週間前のものは消す.
		
		SharedPreferences spmail = PreferenceManager.getDefaultSharedPreferences(this);
		mailto[0] = spmail.getString("mailto", "");
		pmain = (Button)findViewById(R.id.button_main);
		pconfig = (Button)findViewById(R.id.button_config);
		pmailer = (Button)findViewById(R.id.button_mailer);
		
		
		
		pmain.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//start selecting an event
				ArrayList<String>[] event_list = loadCSV(glb.event, 3);
				
				String stri = event_list[1].toString();
				stri = stri.substring(1, stri.length() - 1);
				String[] id = event_list[0].toString().split(",");
				String[] name = stri.split(",");
				String[] date = event_list[2].toString().split(",");
				String[] event_names = new String[id.length];
				String[] ids = new String[id.length];
				int count = 0;
				
				//dateによる判定-idと繋ぐ
				for(int i = 1; i < id.length; i++){
					try{
						String date1 = date[i].replace("'", "");
						String date2 = formatter.format(new Date());
						
						if(date2.equals(date1.trim())){
							ids[count] = id[i];
							event_names[count] = name[i];
							count++;
						}
						
					}catch(ParseException ex){
						ex.printStackTrace();
					} 
					
				}
				final String[] eids = new String[count];
				final String[] items = new String[count];
				int k = 0;
				while(event_names[k] != null){
					items[k] = event_names[k];
					eids[k] = ids[k];
					k++;
				}
				if(count > 1){
					AlertDialog.Builder listDlg = new AlertDialog.Builder(LaunchedActivity.this);
					listDlg.setTitle("イベント名を選んで下さい");
					listDlg.setItems(
							items,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									Intent intent = new Intent(LaunchedActivity.this, MainActivity.class);
									intent.putExtra("eventName", items[which].replace("'", ""));
									intent.putExtra("position", which);
									intent.putExtra("event_ids", eids);
									intent.putExtra("eventNames", items);
									glb.connectflag = false;
									startActivity(intent);
								}
							});
					listDlg.create().show();
				}else if(count == 1){
					Intent intent = new Intent(LaunchedActivity.this, MainActivity.class);
					intent.putExtra("eventName", items[0].replace("'", ""));
					intent.putExtra("position", 0);
					intent.putExtra("event_ids", eids);
					intent.putExtra("eventNames", items);
					startActivity(intent);
				}
			}
		});
		
		pconfig.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initFileLoader();
				
			}
		});
		
		
		pmailer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String[] items = new String[]{"今回分","前回分"};
				AlertDialog.Builder listDlg = new AlertDialog.Builder(LaunchedActivity.this);
				listDlg.setTitle("いつの情報を出力しますか？");
				listDlg.setItems(
						items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(Intent.ACTION_SEND);
								String csvHeader = "";
								String csvValues = "";
								//DBをCSVにする関数
								//このあと、日付ごとに調整いれる。
								switch(which){
									case 0:
										result_c = glb.result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, "date LIKE ?", new String[]{"%" + formatter.format(new Date()) + "%"}, null, null, null, null);
										result_n = glb.result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, "date LIKE ?", new String[]{"%" + formatter.format(new Date()) + "%"}, null, null, null, null);
										break;
									
									case 1:
										result_c = glb.result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, "date >= ? AND date < ?", new String[]{formatter.format(glb.last_week),formatter.format(new Date())}, null, null, null, null);
										result_n = glb.result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, "date >= ? AND date < ?", new String[]{formatter.format(glb.last_week),formatter.format(new Date())}, null, null, null, null);
										break;
								}
								if(result_n.getCount() > 0){
									result_n.moveToFirst();
									String tmpCsvPath;
									//ファイル名用に日付加工
									String str = result_n.getString(6);
									str = str.replaceAll(" ", "_");
									str = str.replaceAll("/", "_");
									str = str.replaceAll(":", "_");
									tmpCsvPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/result" + "_" + result_n.getString(0).trim() + "_" + str + ".csv";
								try{
										
									BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpCsvPath, false), "UTF-8"));
									for(int i = 0; i < glb.column_names.length; i++){
										if(csvHeader.length() > 0){
											csvHeader += ",";
										}
										csvHeader += "'" + glb.column_names[i] + "'";
									}
									csvHeader += "\n";
									c_isEof = result_c.moveToFirst();
									
									if(result_c != null){
										bw.write(csvHeader);
										
										while(c_isEof){
											csvValues = result_c.getString(0) + ",";
											csvValues += result_c.getString(1) + ",";
											csvValues += Integer.toString(result_c.getInt(2)) + ",";
											csvValues += result_c.getString(3) + ",";
											csvValues += result_c.getString(4) + ",";
											csvValues += result_c.getString(5) + ",";
											csvValues += result_c.getString(6) + "\n";
											bw.write(csvValues);
											c_isEof = result_c.moveToNext();
										}
									}
									result_c.close();
									result_n.close();
									bw.flush();
									bw.close();
									
									Uri attatchments = Uri.parse("file://" + tmpCsvPath);
									uat = new UploadAsyncTask(LaunchedActivity.this);
									uat.execute(tmpCsvPath);
								
									
									//intent.putExtra(Intent.EXTRA_STREAM, attatchments);
									//intent.putExtra(Intent.EXTRA_EMAIL, mailto);
									//intent.putExtra(Intent.EXTRA_SUBJECT, "件名を入れます");
									//intent.putExtra(Intent.EXTRA_TEXT, "本文を入れます");
									
									//intent.putExtra("eventName", items[which]);
									
									
									//intent.setType("message/rfc822");
									
									//intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
									try{
										//startActivity(intent);
									}catch(android.content.ActivityNotFoundException ex){
										ex.printStackTrace();
									}
								}catch(UnsupportedEncodingException e){
									e.printStackTrace();
								}catch(FileNotFoundException e){
									e.printStackTrace();
								}catch(IOException e){
									e.printStackTrace();
								} 
								}else{
									AlertDialog.Builder adb = new AlertDialog.Builder(LaunchedActivity.this);
										switch(which){
										case 0:
											adb.setTitle("まだ本日分のデータが登録されていません。");
											break;
										case 1:
											adb.setTitle("前回のイベントが２週間以上前のため、データが破棄されている可能性があります。");
											break;
										}
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
						});
				listDlg.create().show();
			}
		});
	}
	
	private void initFileLoader(){
		File store = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File outputfile1 = new File(store, "event.csv");
		File outputfile2 = new File(store, "members.csv");
		
		ds1 = new DownloadService(this, DOWNLOAD_FILE_URL_EVENT, outputfile1);
		ds2 = new DownloadService(this, DOWNLOAD_FILE_URL_MEMBERS, outputfile2);
		ds1.execute();
		ds2.execute();
		final String[] items = {"OK"};
		AlertDialog.Builder listDlg = new AlertDialog.Builder(LaunchedActivity.this);
		TextView msg = new TextView(LaunchedActivity.this);
		msg.setText("ダウンロードが完了しました。");
		msg.setGravity(Gravity.CENTER_HORIZONTAL);
		msg.setPadding(0, 25, 0, 0);
		msg.setTextSize(22);
		//listDlg.setTitle("ダウンロードが完了しました。");
		/*listDlg.setItems(
				items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});*/
		listDlg.setPositiveButton("OK", 
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = getIntent();
				overridePendingTransition(0,0);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				finish();
				
				overridePendingTransition(0,0);
				startActivity(intent);
				
			}
		});  
		listDlg.setView(msg);
		listDlg.create().show();
	}
	
	
	public void fileLoads(String path){
		
	}
	
	public String showEventData(){
		
		return null;
	}
	public void setEventName(String name){
		this.name = name;
	}
	public String getEventName(){
		return this.name;
	}
	
	@Override
	public void openMailer() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String loadOutputs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<String>[] loadCSV(String path, int column){
		try{
			int count = 0;
			event_list = new ArrayList[column];
			while(count < column){
				event_list[count] = new ArrayList<String>();
				count++;
			}
			FileReader fr = new FileReader(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + path);
			BufferedReader br = new BufferedReader(fr);
			String line ="";
			StringTokenizer token;
			while((line = br.readLine()) != null){
				
				token = new StringTokenizer(line, ",");
				int i = 0;
				while(token.hasMoreTokens()){
					String tmp = token.nextToken();
					event_list[i].add(tmp);
					i++;
				}
			}
			br.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return event_list;
	}
	@Override
	protected void onPause(){
		super.onPause();
		cancelLoad();
	}
	@Override
	protected void onStop(){
		super.onStop();
		cancelLoad();
	}
	private void cancelLoad(){
		if(ds1 != null){
			ds1.cancel(true);
		}
		if(ds2 != null){
			ds2.cancel(true);
		}
	}
}

