package fr.mathis.morpion.tools;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.mathis.morpion.MainActivity;

public class ColorHolder {

	static ColorHolder instance;
	String colorBluePlayer = "";
	String colorRedPlayer = "";
	Context c;
	SharedPreferences settings;

	public ColorHolder(Context c) {
		this.c = c;
		settings = PreferenceManager.getDefaultSharedPreferences(c);
		colorBluePlayer = getAllColor().get(settings.getInt("colorblue", 0));
		colorRedPlayer = getAllColor().get(settings.getInt("colorred", 6));
	}

	public static ColorHolder getInstance(Context c) {
		if (instance == null)
			instance = new ColorHolder(c);
		return instance;
	}

	public String getColor(int player_id) {
		if (player_id == MainActivity.BLUE_PLAYER)
			return colorBluePlayer;
		else
			return colorRedPlayer;
	}

	public static ArrayList<String> getAllColor() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("#33B5E5");
		res.add("#0099CC");
		res.add("#AA66CC");
		res.add("#9933CC");
		res.add("#99CC00");
		res.add("#669900");
		res.add("#FFBB33");
		res.add("#FF8800");
		res.add("#FF4444");
		res.add("#F90000");
		res.add("#A8A8A8");
		res.add("#515151");
		return res;
	}
	
	public static String addAlphaFormColor(String color, String alpha)
	{
		return "#"+alpha+color.substring(1);
	}

	public void save(int player_id, int indexOfColors) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = settings.edit();
		if (player_id == MainActivity.BLUE_PLAYER)
			editor.putInt("colorblue", indexOfColors);
		else
			editor.putInt("colorred", indexOfColors);

		editor.commit();

		colorBluePlayer = getAllColor().get(settings.getInt("colorblue", 0));
		colorRedPlayer = getAllColor().get(settings.getInt("colorred", 6));
	}

	public int getColorIndex(int player_id) {
		if (player_id == MainActivity.BLUE_PLAYER)
			return settings.getInt("colorblue", 0);
		else
			return settings.getInt("colorred", 6);
	}

}
