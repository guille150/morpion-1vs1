package fr.mathis.morpion.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StateHolder {
	public static void MemorizeValue(String name, boolean value, Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static boolean GetMemorizedValue(String name, Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		return settings.getBoolean(name, false);
	}
	
	public static void MemorizeValue(String name, int value, Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(name, value);
		editor.commit();
	}
	
	public static int GetMemorizedValueInt(String name, Context c) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		return settings.getInt(name, -1);
	}
}
