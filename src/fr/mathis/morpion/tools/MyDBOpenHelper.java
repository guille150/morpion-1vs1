package fr.mathis.morpion.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDBOpenHelper extends SQLiteOpenHelper {

	private static final String queryCreationBdd = "CREATE TABLE partie (id INTEGER PRIMARY KEY AUTOINCREMENT, winner INTEGER, disposition varchar(255))";
	
	
	
	public MyDBOpenHelper(Context context, String name, CursorFactory factory, int version) 
	{
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(queryCreationBdd);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE partie;");
		db.execSQL("DELETE FROM sqlite_sequence"); //table which contains the next incremented key value
		onCreate(db);
	}

}
