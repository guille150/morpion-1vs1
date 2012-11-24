package fr.mathis.morpion;

import java.util.ArrayList;

import fr.mathis.morpion.R;
import fr.mathis.morpion.tools.ToolsBDD;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity implements OnClickListener {

	private static final int MENU_NEW_GAME = 0;
	private static final int MENU_QUIT_GAME = 1;
	private static final int MENU_ANNULER = 2;
	
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
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
    	super.onCreate(savedInstanceState);    
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.game);  
    	
    	nbGame = Integer.parseInt(this.getIntent().getStringExtra("nb"));
    	TextView tv1 = (TextView)findViewById(R.id.welcomeGame);
    	tv1.setText(getString(R.string.game)+nbGame);

    	Display display = getWindowManager().getDefaultDisplay();
    	w = display.getWidth();
    	if(display.getHeight()<w)
    		w = display.getHeight();
	    int ratio = 4;
	    if(display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    	ratio = 3;	
    	
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
        			tabIB[i][j].setImageDrawable(d);
        			tabIB[i][j].setEnabled(false);
        			annulerList.add(i+","+j);
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
		final boolean directGame = mgr.getBoolean("newgame", true);	
		
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

	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() 
	    {  
	      public void onClick(DialogInterface dialog, int which) 
	      {  
	    	  if(save)
	    		  Toast.makeText(GameActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
	    	  
	    	  if(directGame)
	    	  {
	    		  nbGame++;
	    		  newGame();
	    	  }
	    	  
	    	  finish();



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
				playerText.setText(" "+getString(R.string.red));
				playerText.setTextColor(Color.RED);
			}
			else 
			{
				turn = BLUE_PLAYER;
				playerText.setText(" "+getString(R.string.blue));
				playerText.setTextColor(Color.rgb(0, 148, 255));
			}
		}
	}
	
	private void newGame() 
	{
		Bundle objetbunble = new Bundle();
		objetbunble.putString("nb", nbGame+"");
		Intent intent = new Intent(GameActivity.this, GameActivity.class);
		intent.putExtras(objetbunble);
		startActivity(intent);
		finish();
	}
	
    /*BLOCK THE ROTATION OF THE SCREEN*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    
	    Display display = getWindowManager().getDefaultDisplay();
	    
	    
	    int orientation = display.getOrientation();
	    int ratio = 4;
	    if(orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    	ratio = 3;	    
	    
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
	    
	}
	
	/*MENU*/
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_NEW_GAME, 0, R.string.restart).setIcon(android.R.drawable.ic_delete);
	    menu.add(0, MENU_ANNULER, 0, R.string.ctrlz).setIcon(android.R.drawable.ic_menu_edit).setEnabled(false);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_NEW_GAME:
	        newGame();
	        return true;
	    case MENU_QUIT_GAME:
	    	finish();
	    	return true;
	    case MENU_ANNULER:
	    	annuler();
	    	return true;
	    }
	    return false;
	}
	
	//Call when the menu is invoke
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(annulerList.size()==0)
			menu.getItem(1).setEnabled(false);
		else
			menu.getItem(1).setEnabled(true);
		return true;
	}
	

	
}
