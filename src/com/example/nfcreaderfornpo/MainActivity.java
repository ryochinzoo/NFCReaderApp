package com.example.nfcreaderfornpo;

import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;

interface ListManaging{
	public void createListData();
	public void changeList();
}

public class MainActivity extends Activity implements OnClickListener, OnLoadCompleteListener{
	
	static final String TAG = "ListViewTest";
	static customadapter cadapter, regular_adapter, f_adapter, s_adapter, t_adapter;
	static List<nfclistdata> objects, regular_objects, f_objects, s_objects, t_objects;
	nfclistdata item, regular_item, f_item, s_item, t_item;
	Globals glb;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTagLists;
	private int currentSoundID;
	NfcAdapter mNfcAdapter;
	TextView point_name, today, notificationView;
	TextView mem_counter, ordinal_counter, f_counter, s_counter, t_counter;
	SoundPool sp;
	Spinner event_name;
	String[] event_items;
	String[] eids;
	String ename;
	ListView listView, regular_list, point_1st, point_2nd, point_3rd;
	Button start;
	Button end;
	int validation_code, ok, ng, ng2;
	int counter,ordinal_c,f_c,s_c,t_c;
	
	SimpleDateFormat sdf, format;
	ArrayList<String> strstore = new ArrayList<String>();
	int viewId, objectId, position;
	Integer test;
	File m_event, m_members;
	String mm,dd,ss;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//takefile();
		counter = 0;
		ordinal_c = 0;
		f_c = 0;
		s_c = 0;
		t_c = 0;
		
		viewId = 0;
		test = 0;
		glb = (Globals) this.getApplication();
		glb.g_init();
		glb.dbconnect();
		Button home = (Button)findViewById(R.id.button_home);
		start = (Button)findViewById(R.id.button_start);
		end = (Button)findViewById(R.id.button_end);
		Date tdy = new Date();
		sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.JAPAN);
		format = new SimpleDateFormat("yyyy/MM/dd");
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		mFilters = new IntentFilter[]{tech, };
		mTagLists = new String[][]{new String[]{ NfcF.class.getName()}};	
		
		//objects
		objects = new ArrayList<nfclistdata>();
		regular_objects = new ArrayList<nfclistdata>();
		f_objects = new ArrayList<nfclistdata>();
		s_objects = new ArrayList<nfclistdata>();
		t_objects = new ArrayList<nfclistdata>();
		
		
		//adapters
		cadapter = new customadapter(this, 0, objects);
		regular_adapter = new customadapter(this, 0, regular_objects);
		f_adapter =  new customadapter(this, 0, f_objects);
		s_adapter =  new customadapter(this, 0, s_objects);
		t_adapter =  new customadapter(this, 0, t_objects);
		
		//list views
		listView = (ListView) findViewById(R.id.listView);
		regular_list = (ListView) findViewById(R.id.regular_list);
		point_1st = (ListView) findViewById(R.id.point_1st);
		point_2nd = (ListView) findViewById(R.id.point_2nd);
		point_3rd = (ListView) findViewById(R.id.point_3rd);
		
		//textview and spinner
		event_name = (Spinner)findViewById(R.id.eventName);
		point_name = (TextView)findViewById(R.id.viewName);
		mem_counter = (TextView)findViewById(R.id.counter);
		ordinal_counter = (TextView)findViewById(R.id.ordinal_counter);
		f_counter = (TextView)findViewById(R.id.f_counter);
		s_counter = (TextView)findViewById(R.id.s_counter);
		t_counter = (TextView)findViewById(R.id.t_counter);
		notificationView = (TextView)findViewById(R.id.notification);
		
		//today = (TextView)findViewById(R.id.today);
		//この時点でイベントを選択できるようにしevent_itemsに代入
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		event_items = i.getStringArrayExtra("eventNames");
		position = i.getIntExtra("position", 0);
		eids = i.getStringArrayExtra("event_ids");
		ename = extras.getString("eventName").replace("'", "");
		
		
		glb.evnt_nm = ename;
		for(int r = 0; r < event_items.length; r++){
			event_items[r] = event_items[r].replace("'", "");
			if(event_items[r].equals(ename)){
				glb.evnt_id = eids[r];
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, event_items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//setting adapters into views
		listView.setAdapter(cadapter);
		regular_list.setAdapter(regular_adapter);
		point_1st.setAdapter(f_adapter);
		point_2nd.setAdapter(s_adapter);
		point_3rd.setAdapter(t_adapter);
		event_name.setAdapter(adapter);
		
		glb.is_nameEof = glb.c_name.moveToFirst();
		glb.c_date.moveToFirst();
		glb.c_id.moveToFirst();
		
		if(glb.db_count() > 0){
			loadDB();
		}else{
			
		}
		
		point_name.setText(glb.view_name[viewId]);
		//today.setText(format.format(tdy));
		viewSwitcher(viewId);		
		home.setOnClickListener(new View.OnClickListener(){
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//sp.release();
				
				Intent intent = new Intent(MainActivity.this, LaunchedActivity.class);
				startActivity(intent);
			}
		});
		
		if(mNfcAdapter == null){
			Toast.makeText(this, "NFC is not available ", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		event_name.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
				//イベント変えたら、IDちゃんと変わってるかどうか。
				
				Spinner spinner = (Spinner) arg0;
				glb.evnt_nm = spinner.getSelectedItem().toString();
				for(int s = 0; s < event_items.length; s++){
					if(event_items[s].equals(glb.evnt_nm)){
						glb.evnt_id = eids[s];
						position = arg2;
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		}
				
				);
		start.setOnClickListener(this);
		end.setOnClickListener(this);
		
	}
	
	public void takefile(){
		Intent event_intent = new Intent(Intent.ACTION_VIEW);
		event_intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + glb.event), "text/csv");
		startActivity(event_intent);
		
	}
	
	public void onResume(){
		mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTagLists);
		super.onResume();
		
		sp = new SoundPool(3,AudioManager.STREAM_MUSIC,0);
		ok = sp.load(this, R.raw.ok, 1);
		ng = sp.load(this, R.raw.ng, 1);
		ng2 = sp.load(this, R.raw.ng2, 1);
		sp.setOnLoadCompleteListener(this);
		
		event_name.setSelection(position);
		
	}
	public void onPause(){
		if(this.isFinishing()){
			mNfcAdapter.disableForegroundDispatch(this);
		}
		super.onPause();
		//sp.unload(ok);
		//sp.unload(ng);
		//sp.unload(ng2);
		//sp.release();
	}
	public void onDestroy(){
		super.onDestroy();
	}
	public void loadDB(){
		String name;
		Date date;
		String member_id;
		int id;
		while(glb.is_nameEof){
			member_id = glb.getId();
			name = glb.getName();
			date = glb.getDate();
			id = glb.getViewId();
			addItem(member_id, name, id);
		}
		glb.c_name.close();
		glb.c_date.close();
		glb.c_id.close();
		
	}
	@Override
	public void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		String action = intent.getAction();
		if(action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
			byte[] rawId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			String uid = bytesToText(rawId);
			Log.d("uid", uid);
			
			Time time = new Time("Asia/Tokyo");
			time.setToNow();
			if((time.month + 1) < 10){
				mm = "0" + (time.month + 1);
			}else{
				mm = Integer.toString(time.month + 1);
			}
			
			if(time.monthDay < 10){
				dd = "0" + time.monthDay;
			}else{
				dd = Integer.toString(time.monthDay);
			}
			
			if(time.second < 10){
				ss = "0" + time.second;
			}else{
				ss = Integer.toString(time.second);
			}
			String date = time.year + "/" + mm + "/" + dd + " " + time.hour + ":" + time.minute + ":" + ss;
			
			//validationいれる
			if((validation_code = glb.validation(uid, viewId)) == 0 || (validation_code = glb.validation(uid, viewId)) == 3){
				//felicaId照合
				glb.members_db.beginTransaction();
				Cursor c = glb.members_db.query("member_table", new String[]{"id", "felicaId", "name", "update_time"}, null, null, null, null, null, null);
				c = glb.members_db.rawQuery("SELECT * FROM member_table WHERE felicaId like ?;", new String[]{"%" + uid + "%"});
				c.moveToFirst();
				glb.members_db.setTransactionSuccessful();
				String member_id = c.getString(0);
				String felicaId = c.getString(1);
				String name = c.getString(2);
				String update_time = c.getString(3);
				
				c.close();
				
				glb.members_db.endTransaction();
				if(validation_code == 0){
					sp.play(ok, 1, 1, 0	, 0, 1);
					notificationView.setText("受付済");
				}else if(validation_code == 3){
					sp.play(ng2, 1, 1, 0, 0, 1);
					notificationView.setText("未更新" + " [" + update_time + " ]");
				}
				//glb.members_db.close();
				glb.result.execSQL("INSERT INTO result(event_id, event_name, point_id, point_name, member_id, member_name, date, felicaId) values ('" + glb.evnt_id + "', '" + glb.evnt_nm +"', " + viewId + ", '" + glb.view_name[viewId] + "', '" + member_id + "', '" + name + "', '" + date + "', '" + felicaId + "')");
				addItem(member_id, name, viewId);
				
			}else{
				
				//AlertDialog.Builder adb = new AlertDialog.Builder(this);
				//adb.setTitle("カードの読み取りを中止しました");
				switch(validation_code){
				case 1:
					sp.play(ng, 1, 1, 0, 0, 1);
					//adb.setMessage("既にカードが読み取られています。");
					notificationView.setText("受付済");
					break;
				case 2:
					sp.play(ng, 1, 1, 0, 0, 1);
					notificationView.setText("未登録");
					//ポイントなのか、会員チェックなのか	分岐させる場合は別途メッセージを作る
					//adb.setMessage("会員リストにいません。登録を行って下さい。");
					break;
				case 3:
					
					//adb.setMessage("契約更新されていません。更新を行って下さい。");
					break;
				}
				//adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				
					
			}
			//db.close();
			//sp.stop(currentSoundID);
			
		}
		
	}
	
	private String bytesToText(byte[] bytes) {
    	StringBuilder buffer = new StringBuilder();
    	for (byte b : bytes) {
    		String hex = String.format("%02X", b);
    		buffer.append(hex).append("");
    	}
    	
    	String text = buffer.toString().trim();
    	return text;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		
			if( id == R.id.button_start){
				viewId--;
				if(viewId < 0){
					viewId = 0;
				}
				point_name.setText(glb.view_name[viewId]);
				notificationView.setText("");
				//表示切り替え操作
				viewSwitcher(viewId);
			}else if( id == R.id.button_end){
				viewId++;
				if(viewId > 4){
					viewId = 4;
				}
				point_name.setText(glb.view_name[viewId]);
				notificationView.setText("");
				
				//表示切り替え操作
				viewSwitcher(viewId);
				
			
		}
		
	}
	public void viewSwitcher(int id){
		switch(id){
			case 0:
				listView.setVisibility(View.VISIBLE);
				regular_list.setVisibility(View.INVISIBLE);
				point_1st.setVisibility(View.INVISIBLE);
				point_2nd.setVisibility(View.INVISIBLE);
				point_3rd.setVisibility(View.INVISIBLE);
				mem_counter.setVisibility(View.VISIBLE);
				ordinal_counter.setVisibility(View.INVISIBLE);
				f_counter.setVisibility(View.INVISIBLE);
				s_counter.setVisibility(View.INVISIBLE);
				t_counter.setVisibility(View.INVISIBLE);
				break;
			case 1:
				listView.setVisibility(View.INVISIBLE);
				regular_list.setVisibility(View.VISIBLE);
				point_1st.setVisibility(View.INVISIBLE);
				point_2nd.setVisibility(View.INVISIBLE);
				point_3rd.setVisibility(View.INVISIBLE);
				mem_counter.setVisibility(View.INVISIBLE);
				ordinal_counter.setVisibility(View.VISIBLE);
				f_counter.setVisibility(View.INVISIBLE);
				s_counter.setVisibility(View.INVISIBLE);
				t_counter.setVisibility(View.INVISIBLE);
				break;
			case 2:
				listView.setVisibility(View.INVISIBLE);
				regular_list.setVisibility(View.INVISIBLE);
				point_1st.setVisibility(View.VISIBLE);
				point_2nd.setVisibility(View.INVISIBLE);
				point_3rd.setVisibility(View.INVISIBLE);
				mem_counter.setVisibility(View.INVISIBLE);
				ordinal_counter.setVisibility(View.INVISIBLE);
				f_counter.setVisibility(View.VISIBLE);
				s_counter.setVisibility(View.INVISIBLE);
				t_counter.setVisibility(View.INVISIBLE);
				break;
			case 3:
				listView.setVisibility(View.INVISIBLE);
				regular_list.setVisibility(View.INVISIBLE);
				point_1st.setVisibility(View.INVISIBLE);
				point_2nd.setVisibility(View.VISIBLE);
				point_3rd.setVisibility(View.INVISIBLE);
				mem_counter.setVisibility(View.INVISIBLE);
				ordinal_counter.setVisibility(View.INVISIBLE);
				f_counter.setVisibility(View.INVISIBLE);
				s_counter.setVisibility(View.VISIBLE);
				t_counter.setVisibility(View.INVISIBLE);
				break;
			case 4:
				listView.setVisibility(View.INVISIBLE);
				regular_list.setVisibility(View.INVISIBLE);
				point_1st.setVisibility(View.INVISIBLE);
				point_2nd.setVisibility(View.INVISIBLE);
				point_3rd.setVisibility(View.VISIBLE);
				mem_counter.setVisibility(View.INVISIBLE);
				ordinal_counter.setVisibility(View.INVISIBLE);
				f_counter.setVisibility(View.INVISIBLE);
				s_counter.setVisibility(View.INVISIBLE);
				t_counter.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	//データいれる
	public void addItem(String mem_id, String name, int id){
		//event_id, event_name, point_name, member_id, member_name, date
		//ローテートしたり、Homeボタンを押してしまうとデータが消えてしまうので対応必要
		item = new nfclistdata();
		
		switch(id){
			case 0:
				counter++;
				item.setId(mem_id);
				item.setName(name);
				//item.setDate(date);
				cadapter.insert(item, 0);
				mem_counter.setText(counter+"人");
				break;
			case 1:
				ordinal_c++;
				item.setId(mem_id);
				item.setName(name);
				//item.setDate(date);
				regular_adapter.insert(item, 0);
				ordinal_counter.setText(ordinal_c+"人");
				break;
			case 2:
				f_c++;
				item.setId(mem_id);
				item.setName(name);
				//item.setDate(date);
				f_adapter.insert(item, 0);
				f_counter.setText(f_c+"人");
				break;
			case 3:
				s_c++;
				item.setId(mem_id);
				item.setName(name);
				//item.setDate(date);
				s_adapter.insert(item, 0);
				s_counter.setText(s_c+"人");
				break;
			case 4:
				t_c++;
				item.setId(mem_id);
				item.setName(name);
				//item.setDate(date);
				t_adapter.insert(item, 0);
				t_counter.setText(t_c+"人");
				
				break;
		}
		
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		// TODO Auto-generated method stub
		//Log.d("notification", "loaded");
	}



	
}

