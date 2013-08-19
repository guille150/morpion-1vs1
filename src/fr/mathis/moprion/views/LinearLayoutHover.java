package fr.mathis.moprion.views;

import fr.mathis.morpion.interfaces.HoverHandler;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LinearLayoutHover extends LinearLayout {

	HoverHandler handler;

	public LinearLayoutHover(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setHoverHandler(HoverHandler handler) {
		this.handler = handler;
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent ev) {
		Log.d("Prova", "dispatchGenericMotionEvent: " + ev.getAction());

		handler.give(ev, null);

		return true;
	}

}
