package fr.mathis.morpion.tools;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.mathis.morpion.MainActivity;
import fr.mathis.morpion.R;

public class ColorHolder {

	static ColorHolder instance;
	int drawableBluePlayer = R.drawable.croix;
	int drawableRedPlayer = R.drawable.cercle;
	String colorBluePlayer = "";
	String colorRedPlayer = "";
	Context c;
	SharedPreferences settings;

	public ColorHolder(Context c) {
		this.c = c;
		settings = PreferenceManager.getDefaultSharedPreferences(c);
		drawableBluePlayer = getAllBlueDrawable().get(settings.getInt("colorblue", 0));
		drawableRedPlayer = getAllRedDrawable().get(settings.getInt("colorred", 9));
		colorBluePlayer = getAllColor().get(settings.getInt("colorblue", 0));
		colorRedPlayer = getAllColor().get(settings.getInt("colorred", 9));
	}

	public static ColorHolder getInstance(Context c) {
		if (instance == null)
			instance = new ColorHolder(c);
		return instance;
	}

	public int getDrawable(int player_id) {
		if (player_id == MainActivity.BLUE_PLAYER)
			return drawableBluePlayer;
		else
			return drawableRedPlayer;
	}

	public String getColor(int player_id) {
		if (player_id == MainActivity.BLUE_PLAYER)
			return colorBluePlayer;
		else
			return colorRedPlayer;
	}

	public static ArrayList<String> getAllColor() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("#33b5e5");
		res.add("#0099cc");
		res.add("#aa66cc");
		res.add("#9933cc");
		res.add("#99cc00");
		res.add("#669900");
		res.add("#ffbb33");
		res.add("#ff8800");
		res.add("#ff4444");
		res.add("#f90000");
		return res;
	}

	public static ArrayList<Integer> getAllBlueDrawable() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		res.add(R.drawable.croix);
		res.add(R.drawable.croix_bleu_fonce);
		res.add(R.drawable.croix_violet_clair);
		res.add(R.drawable.croix_violet_fonce);
		res.add(R.drawable.croix_vert_clair);
		res.add(R.drawable.croix_vert_fonce);
		res.add(R.drawable.croix_jaune_clair);
		res.add(R.drawable.croix_jaune_fonce);
		res.add(R.drawable.croix_rouge_clair);
		res.add(R.drawable.croix_rouge_fonce);
		return res;
	}

	public static ArrayList<Integer> getAllRedDrawable() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		res.add(R.drawable.cercle_bleu_clair);
		res.add(R.drawable.cercle_bleu_fonce);
		res.add(R.drawable.cercle_violet_clair);
		res.add(R.drawable.cercle_violet_fonce);
		res.add(R.drawable.cercle_vert_clair);
		res.add(R.drawable.cercle_vert_fonce);
		res.add(R.drawable.cercle_jaune_clair);
		res.add(R.drawable.cercle_jaune_fonce);
		res.add(R.drawable.cercle_rouge_clair);
		res.add(R.drawable.cercle_rouge_fonce);
		return res;
	}

	public void save(int player_id, int indexOfColors) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = settings.edit();
		if (player_id == MainActivity.BLUE_PLAYER)
			editor.putInt("colorblue", indexOfColors);
		else
			editor.putInt("colorred", indexOfColors);

		editor.commit();

		drawableBluePlayer = getAllBlueDrawable().get(settings.getInt("colorblue", 0));
		drawableRedPlayer = getAllRedDrawable().get(settings.getInt("colorred", 9));
		colorBluePlayer = getAllColor().get(settings.getInt("colorblue", 0));
		colorRedPlayer = getAllColor().get(settings.getInt("colorred", 9));
	}

	public int getColorIndex(int player_id) {
		if (player_id == MainActivity.BLUE_PLAYER)
			return settings.getInt("colorblue", 0);
		else
			return settings.getInt("colorred", 9);
	}

}
