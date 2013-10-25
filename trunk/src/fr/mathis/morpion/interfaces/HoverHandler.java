package fr.mathis.morpion.interfaces;

import fr.mathis.morpion.views.GameView;
import android.view.MotionEvent;

public interface HoverHandler {

	void give(MotionEvent ev, GameView gv);
}