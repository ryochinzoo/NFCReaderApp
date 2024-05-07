package com.example.nfcreaderfornpo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.net.ParseException;
import android.util.Log;

public class Globals extends Application {
	static final String mDB = "members_csv.db";
	static final String DB = "result.db";
	static final int DB_VERSION = 5;
	static final String CREATE_TABLE = "create table result(event_id text, event_name text,point_id INTEGER, point_name text, member_id text,member_name text, date datetime, felicaId text)";
	static final String CREATE_MEMBER_TABLE = "create table member_table(id text,felicaId text,name text,update_time text)";
	static final String DROP_MTABLE = "drop table member_table";
	static final String DROP_TABLE =  "drop table result";
	static SQLiteDatabase r_db;
	static SQLiteDatabase mdb;
	String members = "/members.csv";
	String event = "/event.csv";
	String evnt_nm = "";
	String evnt_id = "";
	SQLiteDatabase members_db, result;
	Cursor c, c_name, c_date, c_id, c_m;
	boolean isEof, is_nameEof, ismEof, valEof;
	boolean connectflag = false;
	DBConnection dbc;//, mdbc;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.JAPAN);
	SimpleDateFormat setToday = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
	String dateCountSql = "SELECT count(*) FROM result WHERE result.date >= CURRENT_DATE";
	//String dateCountSql = "SELECT count(*) FROM Savedata WHERE Savedata.date >= DATE('now')";
	Integer cdate;
	int dialogue_count = 0;
	String[] view_name = new String[]{"役員会員", "一般会員", "ポイント１", "ポイント２", "ゴール"};
	String[] column_names = new String[]{"イベントid","イベント名","種別id","種別","会員id","会員名","日時"};
	//SoundPool sp;
	Calendar cal;
	int year, month, day;
	Date two_weeks_before,last_week,yesterday;
	
	public boolean mdbconn(ArrayList<String>[] members){
				
		c_m = members_db.query("member_table", new String[]{"id", "felicaId", "name", "update_time"}, null, null, null, null, null, null);
		
		String sr0 = members[0].toString().replace("'", "");
		String sr1 = members[1].toString().replace("'", "");
		String sr2 = members[2].toString().replace("'", "");
		String sr3 = members[3].toString().replace("'", "");
		sr0 = sr0.substring(1, sr0.length() - 1);
		sr1 = sr1.substring(1, sr1.length() - 1);
		sr2 = sr2.substring(1, sr2.length() - 1);
		sr3 = sr3.substring(1, sr3.length() - 1);
		
		String[] id = sr0.split(",");
		String[] felicaId = sr1.split(",");
		String[] name = sr2.split(",");
		String[] update = sr3.split(",");
		
		if(c_m.getCount() >= 0){
			members_db.delete("member_table", null, null);
			//return false;
		}
		
		members_db.beginTransaction();
		try{
			for(int i = 1; i < id.length; i++){
			members_db.execSQL("INSERT INTO member_table(id, felicaId, name, update_time) values('" + id[i] + "','" + felicaId[i] +  "','" + name[i] + "','" + update[i]+ "')");
			}
			members_db.setTransactionSuccessful();
		}catch(SQLiteException e){
				e.printStackTrace();
		}finally{
			members_db.endTransaction();
		}
			
		return true;
	}
	
	public boolean dbconnect(){
		//connect with DB
		//result.delete("result", null, null);
		result.execSQL("DELETE FROM result WHERE date <= date('now', '-14 day')");
		c = result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, " date LIKE ?", new String[]{"%" + setToday.format(new Date()) + "%"}, null, null, null, null);
		c_name = result.query("result", new String[]{"event_id", "event_name","point_id", "point_name", "member_id", "member_name", "date", "felicaId"}, " date LIKE ?", new String[]{"%" + setToday.format(new Date()) + "%"}, null, null, null, null);
		c_id = result.query("result", new String[]{"event_id", "event_name", "point_id","point_name", "member_id", "member_name","date", "felicaId"}, " date LIKE ?", new String[]{"%" + setToday.format(new Date()) + "%"}, null, null, null, null);
		c_date = result.query("result", new String[]{"event_id", "event_name", "point_id", "point_name", "member_id", "member_name","date", "felicaId"}, " date LIKE ?", new String[]{"%" + setToday.format(new Date()) + "%"}, null, null, null, null);

		
		//c = db.rawQuery(dateCountSql, null);
		if(c.getCount() == 0){
			return false;
		}
		isEof = c.moveToFirst();
		while(isEof){
			//addItem(c.getString(3), c.getString(0), Integer.parseInt(c.getString(1)));//, c.getString(2));
			isEof = c.moveToNext();
		}
						
		c.close();
		return true;
	}
	
	public long db_count(){
		c = result.rawQuery(dateCountSql, null);
		c.moveToLast();
		long cint = c.getLong(0);
		c.close();
		return cint;
	}
	
	public void g_init(){
		MySQLiteOpenHelper result_dbc = new MySQLiteOpenHelper(getApplicationContext());
		result = result_dbc.getWritableDatabase();
		MySQLiteOpenHelper2 mdbc = new MySQLiteOpenHelper2(getApplicationContext());
		members_db = mdbc.getWritableDatabase();
		cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(year, month, day);
		cal.add(Calendar.DAY_OF_MONTH, -7);
		last_week = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, +7);
		
		cal.add(Calendar.DAY_OF_MONTH, -14);
		two_weeks_before = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, +14);
	}
	
	public int validation(String uid, int id) {
		// TODO Auto-generated method stub
		Cursor c_v = result.query("result", new String[]{"event_id", "event_name","point_id", "point_name", "member_id", "member_name","date", "felicaId"}, "date like ?", new String[]{"%"+setToday.format(new Date()) +"%"}, null, null, null, null);
		Cursor c_member_validation = members_db.query("member_table", new String[]{"id", "felicaId", "name", "update_time"}, null, null, null, null, null, null);
		
		Date td = new Date();
		isEof = c_v.moveToFirst();
		while(isEof){
			//既にタッチしているかどうか
			if(uid.equals(c_v.getString(7).trim()) && id == Integer.parseInt(c_v.getString(2))){
				return 1;
			}
			isEof = c_v.moveToNext();
		}
		valEof = c_member_validation.moveToFirst();
		
		while(valEof){
			if(uid.equals(c_member_validation.getString(1).trim())){ //&& sdf.format(td).equals(c_member_validation)){
				try {
					if(td.compareTo(setToday.parse(c_member_validation.getString(3).trim())) < 0){
						//会員リストにあって、契約更新必要なし。
						c_v.close();
						c_member_validation.close();
						return 0;
					}else{
						//会員リストにあって、契約更新されていない
						c_v.close();
						c_member_validation.close();
						return 3;
					}
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			valEof = c_member_validation.moveToNext();
		}
		c_v.close();
		c_member_validation.close();
				
		return 2;
	}
	public String getId(){
		String memberId = c_name.getString(4);
		return memberId;
	}
	public String getName() {
		// TODO Auto-generated method stub
		String name = c_name.getString(5);
		is_nameEof = c_name.moveToNext();
		
		return name;
	}
	public int getViewId() {
		// TODO Auto-generated method stub
		int viewid = Integer.valueOf(c_id.getString(2));
		c_id.moveToNext();
		return viewid;
	}
	public Date getDate() {
		// TODO Auto-generated method stub
		try{
			Date date = sdf.parse(c_date.getString(6));
			c_date.moveToNext();
			return date;
		} catch(ParseException e){
			e.getStackTrace();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}return null;
	}
	private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
		public MySQLiteOpenHelper(Context c){
			super(c, DB, null, DB_VERSION);
		}
		public void onCreate(SQLiteDatabase mdb){
			mdb.execSQL(CREATE_TABLE);
		}
		public void onUpgrade(SQLiteDatabase mdb, int oldVersion, int newVersion){
			mdb.execSQL(DROP_TABLE);
			onCreate(mdb);
		}
	}
	private static class MySQLiteOpenHelper2 extends SQLiteOpenHelper {
		public MySQLiteOpenHelper2(Context c){
			super(c, mDB, null, DB_VERSION);
		}
		public void onCreate(SQLiteDatabase mdb){
			mdb.execSQL(CREATE_MEMBER_TABLE);
		}
		public void onUpgrade(SQLiteDatabase mdb, int oldVersion, int newVersion){
			mdb.execSQL(DROP_MTABLE);
			onCreate(mdb);
		}
	}
}
