package com.mahone.qtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String SMS_URI_ALL = "content://sms/";
	public static final String SMS_URI_INBOX = "content://sms/inbox";
	public static final String SMS_URI_SEND = "content://sms/sent";
	public static final String SMS_URI_DRAFT = "content://sms/draft";
	public static final String SMS_URI_OUTBOX = "content://sms/outbox";
	public static final String SMS_URI_FAILED = "content://sms/failed";
	public static final String SMS_URI_QUEUED = "content://sms/queued";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void saveSms(View view){
		TextView infoEle = (TextView) this.findViewById(R.id.info);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date beginTime = new Date();
		String filename = "sms_backup_" + sdf.format(beginTime) + ".json";
		FileOutputStream fos = this.getStorageFile(filename);
		int count = this.backupSms(fos);
		Date endTime = new Date();
		int seconds = (int)((endTime.getTime() - beginTime.getTime()) / 1000); 
		
		infoEle.setText("备份结束，耗时" + seconds + "秒，总统有" + count + "条信息，备份文件在目录 Qtool/" + filename);
	}
	
	@SuppressLint("NewApi")
	private int backupSms(FileOutputStream fos){
		int count = 0;
		try {
			Uri uri = Uri.parse(MainActivity.SMS_URI_ALL);
			Cursor cursor = this.getContentResolver().query(uri, null, null, null, "date desc");
						
			if (cursor.moveToFirst()){
				JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, "UTF-8"));
				writer.setIndent("    ");
				writer.beginArray();
				
				do {
					int columnCount = cursor.getColumnCount();
					writer.beginObject();
					for(int i=0; i<columnCount; i++){
						writer.name(cursor.getColumnName(i)).value(cursor.getString(i));
					}
					writer.endObject();
					count++;
				} while(cursor.moveToNext());
				
				cursor.close();
				writer.endArray();
				writer.close();
			}
		} catch (SQLiteException ex){
			Log.d("qtool", ex.getMessage()); 
		} catch (Exception ex){
			Log.d("qtool", ex.getMessage());
		}
		
		return count;
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public FileOutputStream getStorageFile(String filename) {
		if ( ! this.isExternalStorageWritable()){
	    	Log.d("qtool", "external storage can not write");
		}
		
		File path = Environment.getExternalStoragePublicDirectory("QTool");
	    path.mkdirs();
	    File file = new File(path, filename);
	    
	    FileOutputStream fos = null;
	    try {
	    	fos = new FileOutputStream(file);
	    } catch (IOException e) {
	    	Log.d("qtool", e.getMessage());
	    }
	    
	    return fos;
	}
}
