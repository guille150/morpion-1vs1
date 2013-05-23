package fr.mathis.morpion;

import android.view.MotionEvent;

public interface HoverHandler {

	void give(MotionEvent ev, GameView gv);
}