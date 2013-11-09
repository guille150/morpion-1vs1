package fr.mathis.morpion.tools;

import android.content.res.Resources;

public class Tools {

	public static int convertDpToPixel(float dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

}
