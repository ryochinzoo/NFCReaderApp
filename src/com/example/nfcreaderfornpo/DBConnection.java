package com.example.nfcreaderfornpo;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



interface DBConnector{
	public SQLiteDatabase connectDB();
}

public class DBConnection extends SQLiteOpenHelper implements DBConnector {

	private static String tag = "Mytag";
	
	private static String DB_PATH = "/data/data/com.example.nfcreaderfornpo/databases/";
	
	private static String DB_NAME = "registration.db";
	private Context context;
	SQLiteDatabase db;
	
	public DBConnection(Context context) {
		super(context, DB_NAME, null, 1);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public synchronized void close(){
		super.close();
		if(db != null){
			db.close();
		}
	}
	@Override
	public SQLiteDatabase connectDB() {
		// TODO Auto-generated method stub
		String myPath = DB_PATH + DB_NAME;
		File myFile = new File(myPath);
		if(!myFile.exists()){
			//copydatabase();//‚¢‚é‚©‚Ç‚¤‚©‚¯‚ñ‚Æ‚¤
		}
		try{
			db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		}catch(Exception e){
			Log.d("tag", e.toString());
		}
		
		return db;
	}

	private void copydatabase(){
		this.getWritableDatabase();
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE Savedata(uid text not null, view_id integer not null, name text not null, date text not null)";
		db.execSQL(sql);
		//test
		//db.execSQL("INSERT INTO Savedata(uid, view_id, name, date) values (111, 3, 'tanaka', '2014' )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
