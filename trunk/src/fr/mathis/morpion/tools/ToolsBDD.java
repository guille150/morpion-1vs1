package fr.mathis.morpion.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class ToolsBDD {

	public static ToolsBDD instance;
	private SQLiteDatabase bdd;
	private MyDBOpenHelper database;
	
	public ToolsBDD(Context context)
	{
		database = new MyDBOpenHelper(context, "morpion.db", null, 4);
		open();
	}
	
	public static ToolsBDD getInstance(Context context)
	{
		if(instance == null)
			instance = new ToolsBDD(context);
		return instance;		
	}
	
	public void open()
	{
		bdd = database.getWritableDatabase();
	}
 
	public void close()
	{
		bdd.close();
	}
	
	public SQLiteDatabase getBDD()
	{
		return bdd;
	}
	
	
	
	public long insertPartie(int winner, String disposition){
		ContentValues values = new ContentValues();
		values.put("winner", winner);
		values.put("disposition", disposition);
		return bdd.insert("partie", null, values);
	}
	
	public int removePartie(int id){
		return bdd.delete("partie", "id = " +id, null);
	}
	
	public int getNextId(int id)
	{
		int res = 0;
		Cursor c = bdd.query("partie", new String[] {"id"}, null, null, null, null, null);
		if (c.getCount() == 0)
			return 0;
		else 
		{
			c.moveToFirst();
			res = c.getInt(0);
			while(c.moveToNext())
			{
				if(id<c.getInt(0))
				{
					res = c.getInt(0);
					break;
				}
			}
		}
		return res;
	}
	
	public int getPreviousId(int id)
	{
		int res = 0;
		Cursor c = bdd.query("partie", new String[] {"id"}, null, null, null, null, null);
		if (c.getCount() == 0)
			return 0;
		else 
		{
			c.moveToLast();
			res = c.getInt(0);
			while(c.moveToPrevious())
			{
				if(id>c.getInt(0))
				{
					res = c.getInt(0);
					break;
				}
			}
		}
		return res;
	}
	
	public int getNbPartie(){
		int res = 0;
			Cursor c = bdd.query("partie", new String[] {"COALESCE(max(id),0)"}, null, null, null, null, null);
			if (c.getCount() == 0)
				return 0;
			else 
			{
				c.moveToFirst();
				res =  c.getInt(0);
			}
		
		return res;
	}
	
	public String getResultat(int id)
	{
		String res = "";
		Cursor c = bdd.query("partie", new String[] {"winner", "disposition"}, "id = "+id, null, null, null, null);
		if (c.getCount() == 0)
			return "vide";
		else 
		{
			c.moveToFirst();
			res =  c.getString(1);
		}	
		return res;	
	}
	
	public Cursor getAllParties()
	{
		Cursor c = bdd.query("partie", new String[] {"id","winner"}, null, null, null, null, null);
		return c;
	}
	
	public void resetTable()
	{
		bdd.execSQL("DELETE FROM partie;");
		bdd.execSQL("DELETE FROM sqlite_sequence;");
	}
	
	public static void backupDatabase() throws IOException {
	    //Open your local db as the input stream
	    String inFileName = "/data/data/fr.mathis.morpion/databases/morpion.db";
	    File dbFile = new File(inFileName);
	    FileInputStream fis = new FileInputStream(dbFile);

	    String outFileName = Environment.getExternalStorageDirectory()+"/morpion.db";
	    //Open the empty db as the output stream
	    OutputStream output = new FileOutputStream(outFileName);
	    //transfer bytes from the inputfile to the outputfile
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = fis.read(buffer))>0){
	        output.write(buffer, 0, length);
	    }
	    //Close the streams
	    output.flush();
	    output.close();
	    fis.close();
	}


	

 
	
}
