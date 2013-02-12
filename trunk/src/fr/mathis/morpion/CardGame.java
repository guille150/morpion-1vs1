package fr.mathis.morpion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

public class CardGame extends Card {

	private int _id = 0;
	private String _disposition = "";
	private String _title = "";
	private int _winner = 0;
	private Display _display;
	private Context _c = null;
	
	public CardGame(int winner, String disposition, String title, int id, Display display, Context c){
		super(title);
		
		_id = id;
		_disposition = disposition;
		_title = title;
		_winner = winner;
		_display = display;
		_c = c;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_disposition() {
		return _disposition;
	}

	public void set_disposition(String _disposition) {
		this._disposition = _disposition;
	}

	public String get_title() {
		return _title;
	}

	public void set_title(String _title) {
		this._title = _title;
	}

	public int get_winner() {
		return _winner;
	}

	public void set_winner(int _winner) {
		this._winner = _winner;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.cardgame, null);
		((TextView) view.findViewById(R.id.title)).setText(_title);
		
		String resultat = _disposition;
		
		DisplayMetrics metrics = new DisplayMetrics();
		_display.getMetrics(metrics);
		
		int w;
		w = metrics.widthPixels;
		
		if ((metrics.heightPixels) < w)
			w = (metrics.heightPixels);
		
		int ratio = 5;
		
		int[][] val = new int[3][3];
		int tooker = 0;
		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 3 ; j++)
			{
				val[i][j] = Integer.parseInt(resultat.split(",")[tooker]);
				tooker++;
			}
		}
		
		ImageButton[][] tabIB = new ImageButton[3][3];       	
		tabIB[0][0] = (ImageButton)view.findViewById(R.id.imageButton1);
		tabIB[0][1] = (ImageButton)view.findViewById(R.id.imageButton2);
		tabIB[0][2] = (ImageButton)view.findViewById(R.id.imageButton3);
		tabIB[1][0] = (ImageButton)view.findViewById(R.id.imageButton4);
		tabIB[1][1] = (ImageButton)view.findViewById(R.id.imageButton5);
		tabIB[1][2] = (ImageButton)view.findViewById(R.id.imageButton6);
		tabIB[2][0] = (ImageButton)view.findViewById(R.id.imageButton7);
		tabIB[2][1] = (ImageButton)view.findViewById(R.id.imageButton8);
		tabIB[2][2] = (ImageButton)view.findViewById(R.id.imageButton9);

		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 3 ; j++)
			{
				tabIB[i][j].setClickable(false);
				tabIB[i][j].setFocusable(false);
				tabIB[i][j].setMinimumWidth((w)/ratio);
				tabIB[i][j].setMinimumHeight((w)/ratio);
				tabIB[i][j].setMaxWidth((w)/ratio);
				tabIB[i][j].setMaxHeight((w)/ratio);
				
				tabIB[i][j].setEnabled(false);

				if(val[i][j] == MainActivity.BLUE_PLAYER)
				{
					Drawable d = _c.getResources().getDrawable(R.drawable.croix);
					tabIB[i][j].setImageDrawable(d);      			
				}
				if(val[i][j] == MainActivity.RED_PLAYER)
				{
					Drawable d = _c.getResources().getDrawable(R.drawable.cercle);
					tabIB[i][j].setImageDrawable(d);     
				}
			}
		}
		
		return view;
	}	
	
}