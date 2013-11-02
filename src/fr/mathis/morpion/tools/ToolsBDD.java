package fr.mathis.morpion.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ToolsBDD {

	public static ToolsBDD instance;
	private SQLiteDatabase bdd;
	private MyDBOpenHelper database;

	public ToolsBDD(Context context) {
		database = new MyDBOpenHelper(context, "morpion.db", null, 5);
		open();
	}

	public static ToolsBDD getInstance(Context context) {
		if (instance == null)
			instance = new ToolsBDD(context);
		return instance;
	}

	public void open() {
		bdd = database.getWritableDatabase();
	}

	public void close() {
		bdd.close();
	}

	public SQLiteDatabase getBDD() {
		return bdd;
	}

	public long insertPartie(int winner, String disposition) {
		ContentValues values = new ContentValues();
		values.put("id", getNbPartieNumber() + 1);
		values.put("winner", winner);
		values.put("disposition", disposition);
		return bdd.insert("partie", null, values);
	}

	public long insertPartie(int id, int winner, String disposition) {
		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("winner", winner);
		values.put("disposition", disposition);
		return bdd.insert("partie", null, values);
	}

	public int removePartie(int id) {
		return bdd.delete("partie", "id = " + id, null);
	}

	public int getNextId(int id) {
		int res = -1;
		Cursor c = bdd.query("partie", new String[] { "id" }, null, null, null, null, null);
		if (c.getCount() == 0)
			return -1;
		else {
			c.moveToFirst();
			if (id < c.getInt(0)) {
				res = c.getInt(0);
			}
			if (c.getCount() > 1) {
				while (c.moveToNext()) {
					if (id < c.getInt(0)) {
						res = c.getInt(0);
						break;
					}
				}
			}
			c.close();
		}
		return res;
	}

	public int getPreviousId(int id) {
		int res = -1;
		Cursor c = bdd.query("partie", new String[] { "id" }, null, null, null, null, null);
		if (c.getCount() == 0)
			return -1;
		else {
			c.moveToLast();
			if (c.getInt(0) < id) {
				res = c.getInt(0);
			}
			if (c.getCount() > 1) {
				while (c.moveToPrevious()) {
					if (c.getInt(0) < id) {
						res = c.getInt(0);
						break;
					}
				}
			}
			c.close();
		}
		return res;

	}

	public int getNbPartie() {
		int res = 0;
		Cursor c = bdd.query("partie", new String[] { "COALESCE(count(id),0)" }, null, null, null, null, null);
		if (c.getCount() == 0)
			return 0;
		else {
			c.moveToFirst();
			res = c.getInt(0);
		}
		c.close();
		return res;
	}

	public int getNbPartieNumber() {
		int res = 0;
		Cursor c = bdd.query("partie", new String[] { "COALESCE(max(id),0)" }, null, null, null, null, null);
		if (c.getCount() == 0)
			return 0;
		else {
			c.moveToFirst();
			res = c.getInt(0);
		}
		c.close();
		return res;
	}

	public String getResultat(int id) {
		String res = "";
		Cursor c = bdd.query("partie", new String[] { "winner", "disposition" }, "id = " + id, null, null, null, null);
		if (c.getCount() == 0)
			return "vide";
		else {
			c.moveToFirst();
			res = c.getString(1);
		}
		c.close();
		return res;
	}

	public Cursor getAllParties() {
		Cursor c = bdd.query("partie", new String[] { "id", "winner", "disposition" }, null, null, null, null, null);
		return c;
	}

	public void resetTable() {
		bdd.execSQL("DELETE FROM partie;");
	}

	public int getWinner(int id) {
		int res = -1;
		Cursor c = bdd.query("partie", new String[] { "winner" }, "id = " + id, null, null, null, null);
		if (c.getCount() == 0)
			return res;
		else {
			c.moveToFirst();
			res = c.getInt(0);
		}
		c.close();
		return res;
	}

}
