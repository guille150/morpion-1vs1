package fr.mathis.morpion;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

import fr.mathis.morpion.tools.ToolsBDD;

public class MainActivity extends SherlockActivity implements OnNavigationListener, OnClickListener, OnCheckedChangeListener {

	public static final int RED_PLAYER = 4;
	public static final int BLUE_PLAYER = 3;
	public static final int NONE_PLAYER = 5;

	TextView playerText;
	ImageButton[][] tabIB;
	int[][] tabVal;
	int turn = BLUE_PLAYER;
	int nbGame;
	int w;
	ArrayList<String> annulerList;
	Menu m;
	SlidingMenu menu;
	boolean isMenuOpen = false;
	CheckBox cbSaveEqual;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.mainNavigationList, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);

        // configure the SlidingMenu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.menu);
        
		//this method is called by the action bar
		//createNewGame();
        

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		m=menu;
		menu.add(R.string.restart)
		.setIcon(R.drawable.images_rotate_right2)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.getItem(0).setVisible(false);

		menu.add(R.string.ctrlz)
		.setIcon(R.drawable.content_edit2 )
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.getItem(1).setVisible(false);

		menu.add(R.string.menupref)
		.setIcon(R.drawable.action_settings2)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if(item.getTitle().toString().compareTo(getString(R.string.restart))==0)
		{
			createNewGame();
		}
		if(item.getTitle().toString().compareTo(getString(R.string.ctrlz))==0)
		{
			annuler();
			if(annulerList.size()==0)
			{
				m.getItem(0).setVisible(false);
				m.getItem(1).setVisible(false);
			}
		}
		if(item.getTitle().toString().compareTo(getString(R.string.menupref))==0)
		{
			//Intent intent = new Intent(MainActivity.this, PrefActivity.class);
			//startActivity(intent);
		     if(isMenuOpen)
		     {
		    	 menu.toggle(true);
		    	 isMenuOpen = false;
		     }
		     else {
		    	 menu.toggle(true);
				isMenuOpen = true;
		     }

		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onBackPressed() 
	{
		
	     if(isMenuOpen)
	     {
	    	 menu.toggle(true);
	    	 isMenuOpen = false;
	     }
	     else {
	    	 super.onBackPressed();
	     }
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if(itemPosition==0)
		{
			createNewGame();
		}
		if(itemPosition==1)
		{
			if(0 != ToolsBDD.getInstance(this).getNbPartie())
			{
				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
				startActivityForResult(intent, 0);

			}
			else {Toast.makeText(this, R.string.nohistory, Toast.LENGTH_SHORT).show();}
		}
		if(itemPosition==2)
		{
			setContentView(R.layout.help);
			if(m != null)
			{
				m.getItem(0).setVisible(false);
				m.getItem(1).setVisible(false);
			}
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 0) {
			getSupportActionBar().setSelectedNavigationItem(0);
		}
	}

	public void displayNextTurn()
	{
		if(turn == RED_PLAYER)
		{
			playerText.setText(" "+getString(R.string.blue));
			turn = BLUE_PLAYER;
			playerText.setTextColor(Color.rgb(0, 148, 255));
		}
		else {
			playerText.setText(" "+getString(R.string.red));
			turn = RED_PLAYER;
			playerText.setTextColor(Color.RED);
		}

	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void createNewGame()
	{
		setContentView(R.layout.game);  

		cbSaveEqual = (CheckBox)findViewById(R.id.checkBoxSaveEqual);
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean save = mgr.getBoolean("save", false);	

		if(save)
		{
			cbSaveEqual.setChecked(true);
		}
		
		cbSaveEqual.setOnCheckedChangeListener(this);
		
		nbGame = ToolsBDD.getInstance(this).getNbPartie()+1;
		TextView tv1 = (TextView)findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game)+nbGame);

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);


		w = metrics.widthPixels;
		if(metrics.heightPixels<w)
			w = metrics.heightPixels;
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

		playerText = (TextView)findViewById(R.id.playerText);
		annulerList = new ArrayList<String>();
		tabIB = new ImageButton[3][3];   
		tabVal = new int[3][3]; 

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

				tabIB[i][j].setOnClickListener(this);
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		displayNextTurn();
		if(nbGame % 2 == 0)
		{
			displayNextTurn();
		}

		if(m != null)
		{
			m.getItem(0).setVisible(false);
			m.getItem(1).setVisible(false);
		}

		menu.invalidate();
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
				
				int h = sc.getHeight() - playerText.getHeight() - (((TextView)findViewById(R.id.welcomeGame)).getHeight());
				
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
	
	
	//Click on a button
	public void onClick(View view) 
	{
		
		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 3 ; j++)
			{
				if(view.getId() == tabIB[i][j].getId())
				{
					Drawable d;
					if(turn == BLUE_PLAYER)
					{
						d = getResources().getDrawable(R.drawable.croix);
						tabVal[i][j] = BLUE_PLAYER;
						displayNextTurn();
					}
					else
					{
						d = getResources().getDrawable(R.drawable.cercle);
						tabVal[i][j] = RED_PLAYER;
						displayNextTurn();
					}
					
					if(m != null)
					{
						m.getItem(0).setVisible(true);
						m.getItem(1).setVisible(true);
					}
					
					tabIB[i][j].setImageDrawable(d);
					tabIB[i][j].setEnabled(false);
					annulerList.add(i+","+j);
					menu.invalidate();
					this.checkWinner(i,j);
					
				}
			}
		}
	}

	private void checkWinner(int i, int j) 
	{
		if(tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2]!= NONE_PLAYER)
			congratsWinner(tabVal[0][0]);
		else if(tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2]!= NONE_PLAYER)
			congratsWinner(tabVal[1][0]);
		else if(tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2]!= NONE_PLAYER)
			congratsWinner(tabVal[2][0]);
		else if(tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0]!= NONE_PLAYER)
			congratsWinner(tabVal[0][0]);
		else if(tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1]!= NONE_PLAYER)
			congratsWinner(tabVal[0][1]);
		else if(tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2]!= NONE_PLAYER)
			congratsWinner(tabVal[0][2]);
		else if(tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2]!= NONE_PLAYER)
			congratsWinner(tabVal[0][0]);
		else if(tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2]!= NONE_PLAYER)
			congratsWinner(tabVal[2][0]);
		else {
			boolean equal = true;
			for(int x = 0 ; x < 3 ; x++)
			{
				for(int y = 0 ; y < 3 ; y++)
				{
					if(tabVal[x][y] == NONE_PLAYER)	
					{
						equal = false;
						break;
					}
				}			
			}

			if(equal)
			{
				congratsWinner(NONE_PLAYER);
			}
		}
	}

	private void congratsWinner(int winner)
	{
		playerText.setText(R.string.over);
		playerText.setTextColor(Color.WHITE);
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();  
		alertDialog.setIcon(R.drawable.icon);  

		String values ="";
		for(int i = 0 ; i < 3 ; i++)
		{
			for(int j = 0 ; j < 3 ; j++)
			{
				values += ","+tabVal[i][j];
			}
		}
		values = values.substring(1);

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean save = mgr.getBoolean("save", false);	

		if(winner == BLUE_PLAYER)
		{
			alertDialog.setTitle(R.string.win);  
			alertDialog.setMessage(getString(R.string.winb));
			ToolsBDD.getInstance(this).insertPartie(BLUE_PLAYER, values);
		}
		else if(winner==RED_PLAYER)
		{
			alertDialog.setTitle(R.string.win);  
			alertDialog.setMessage(getString(R.string.winr));
			ToolsBDD.getInstance(this).insertPartie(RED_PLAYER, values);
		}
		else if(winner == NONE_PLAYER)
		{
			alertDialog.setTitle(R.string.equal);
			alertDialog.setMessage(getString(R.string.equaltry)); 

			if(save)
				ToolsBDD.getInstance(this).insertPartie(NONE_PLAYER, values);
		}

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() 
		{  
			public void onClick(DialogInterface dialog, int which) 
			{  
				if(save)
					Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();

				nbGame++;
				createNewGame();
			}
		});
		alertDialog.show();  
	}

	private void annuler() 
	{
		if(annulerList.size()>0)
		{
			int x,y;
			x = Integer.parseInt(annulerList.get(annulerList.size()-1).split(",")[0]);
			y = Integer.parseInt(annulerList.get(annulerList.size()-1).split(",")[1]);
			annulerList.remove(annulerList.size()-1);

			tabVal[x][y] = NONE_PLAYER;
			tabIB[x][y].setImageDrawable(null);
			tabIB[x][y].setEnabled(true);
			if(turn == BLUE_PLAYER)
			{
				turn = RED_PLAYER;
				playerText.setText(getString(R.string.red));
				playerText.setTextColor(Color.RED);
			}
			else 
			{
				turn = BLUE_PLAYER;
				playerText.setText(getString(R.string.blue));
				playerText.setTextColor(Color.rgb(0, 148, 255));
			}
		}
	}



	/*BLOCK THE ROTATION OF THE SCREEN*/

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
  
	    Display display = getWindowManager().getDefaultDisplay();

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
	    
    	for(int i = 0 ; i < 3 ; i++)
    	{
        	for(int j = 0 ; j < 3 ; j++)
        	{
        		tabIB[i][j].setMinimumHeight(w/ratio);
        		tabIB[i][j].setMaxHeight(w/ratio);
        		tabIB[i][j].setMinimumWidth(w/ratio);
        		tabIB[i][j].setMaxWidth(w/ratio);
        	}
	    }	
		
		recalculateSize();

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView.getId()==R.id.checkBoxSaveEqual)
		{
			SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = mgr.edit();
		    editor.putBoolean("save", isChecked);
		    editor.commit();
		}
		
	}


}
