package fr.mathis.morpion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.SimpleGestureFilter.SimpleGestureListener;
import fr.mathis.morpion.tools.ToolsBDD;




@SuppressLint("NewApi")
public class VisuActivity extends SherlockActivity implements SimpleGestureListener
{

	ImageButton[][] tabIB;
	int w;
	int h;
	int id;
	SimpleGestureFilter detector;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.visu);
		detector = new SimpleGestureFilter(this,this);

		id = Integer.parseInt(this.getIntent().getStringExtra("id"));

		TextView tv1 = (TextView)findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.resume)+id);
		
		String resultat = ToolsBDD.getInstance(this).getResultat(id);
		Display display = getWindowManager().getDefaultDisplay();
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		w = metrics.widthPixels;
		if((metrics.heightPixels)<w)
			w = (metrics.heightPixels);
		int ratio = 5;
		
		if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1)
		{
			if(display.getRotation() == Surface.ROTATION_0)
				ratio = 3;	
		}
		else {
			if(display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				ratio = 3;	
		}

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

		tabIB = new ImageButton[3][3];       	
		tabIB[0][0] = (ImageButton)findViewById(R.id.imageButton1);
		tabIB[0][1] = (ImageButton)findViewById(R.id.imageButton2);
		tabIB[0][2] = (ImageButton)findViewById(R.id.imageButton3);
		tabIB[1][0] = (ImageButton)findViewById(R.id.imageButton4);
		tabIB[1][1] = (ImageButton)findViewById(R.id.imageButton5);
		tabIB[1][2] = (ImageButton)findViewById(R.id.imageButton6);
		tabIB[2][0] = (ImageButton)findViewById(R.id.imageButton7);
		tabIB[2][1] = (ImageButton)findViewById(R.id.imageButton8);
		tabIB[2][2] = (ImageButton)findViewById(R.id.imageButton9);

		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 3 ; j++)
			{
				tabIB[i][j].setMinimumWidth((w)/ratio);
				tabIB[i][j].setMinimumHeight((w)/ratio);
				tabIB[i][j].setMaxWidth((w)/ratio);
				tabIB[i][j].setMaxHeight((w)/ratio);
				tabIB[i][j].setEnabled(false);

				if(val[i][j] == MainActivity.BLUE_PLAYER)
				{
					Drawable d = getResources().getDrawable(R.drawable.croix);
					tabIB[i][j].setImageDrawable(d);      			
				}
				if(val[i][j] == MainActivity.RED_PLAYER)
				{
					Drawable d = getResources().getDrawable(R.drawable.cercle);
					tabIB[i][j].setImageDrawable(d);     
				}
			}
		}
		recalculateSize();
	}
	
	public void recalculateSize()
	{
		final ScrollView sc = (ScrollView)findViewById(R.id.layoutswipe);
		ViewTreeObserver vto = sc.getViewTreeObserver();
		final Display display = getWindowManager().getDefaultDisplay();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				ViewTreeObserver obs = sc.getViewTreeObserver();

				w = sc.getWidth();
				
				int h = sc.getHeight() - (((TextView)findViewById(R.id.welcomeGame)).getHeight());
				
				if(h<w)
					w = h;
				int ratio = 3;

				if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1)
				{
					if(display.getRotation() == Surface.ROTATION_0)
						ratio = 3;	
				}
				else {
					if(display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
						ratio = 3;	
				}

				for(int i = 0 ; i < 3 ; i++)
				{
					for(int j = 0 ; j < 3 ; j++)
					{
						tabIB[i][j].setMinimumWidth((w)/ratio);
						tabIB[i][j].setMinimumHeight((w)/ratio);
						tabIB[i][j].setMaxWidth((w)/ratio);
						tabIB[i][j].setMaxHeight((w)/ratio);
						tabIB[i][j].invalidate();
					}
				}
				
				if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				{
					obs.removeOnGlobalLayoutListener(this);
				}
				else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, HistoryActivity.MENU_RESET, 0, R.string.previous).setIcon(R.drawable.navigation_previous_item).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);;
		menu.add(0, HistoryActivity.MENU_SHARE, 0, R.string.next).setIcon(R.drawable.navigation_next_item).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);;
	    return true;
	}	

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		case HistoryActivity.MENU_SHARE :
			nextGame();
			break;
		case HistoryActivity.MENU_RESET :
			previousGame();
			break;
		}
		return true;
	}


	public void previousGame() {
		int nextId = ToolsBDD.getInstance(this).getPreviousId(id);
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", ""+nextId);
		Intent intent = new Intent(VisuActivity.this, VisuActivity.class);
		intent.putExtras(objetbunble);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		finish();
	}



	public void nextGame() {
		int nextId = ToolsBDD.getInstance(this).getNextId(id);
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", ""+nextId);
		Intent intent = new Intent(VisuActivity.this, VisuActivity.class);
		intent.putExtras(objetbunble);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}


	@Override 
	public boolean dispatchTouchEvent(MotionEvent me){ 
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me); 
	}


	@Override
	public void onSwipe(int direction) {

		switch (direction) {

		case SimpleGestureFilter.SWIPE_RIGHT :
			previousGame();
		break;
		case SimpleGestureFilter.SWIPE_LEFT : 
			nextGame();
		break;

		} 

	}

	@Override
	public void onDoubleTap() {
		
	}


}
