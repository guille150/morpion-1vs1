package fr.mathis.morpion;

import fr.mathis.morpion.tools.ToolsBDD;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class VisuActivity extends Activity 
{

	ImageButton[][] tabIB;
	int w;
	int h;
	
    public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game);
		
		int id = Integer.parseInt(this.getIntent().getStringExtra("id"));
		
		TextView tv = (TextView)findViewById(R.id.infoTour);
		tv.setText("");
		
		TextView tv1 = (TextView)findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.resume)+id);
		
		String resultat = ToolsBDD.getInstance(this).getResultat(id);
		Display display = getWindowManager().getDefaultDisplay();
    	w = display.getWidth();
    	if(display.getHeight()<w)
    		w = display.getHeight();
	    int ratio = 4;
	    if(display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    	ratio = 3;	
		
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
        		
        		if(val[i][j] == GameActivity.BLUE_PLAYER)
        		{
					Drawable d = getResources().getDrawable(R.drawable.croix);
			    	tabIB[i][j].setImageDrawable(d);      			
        		}
        		if(val[i][j] == GameActivity.RED_PLAYER)
        		{
					Drawable d = getResources().getDrawable(R.drawable.cercle);
			    	tabIB[i][j].setImageDrawable(d);     
        		}
        	}
    	}
	}
    
    /*BLOCK THE ROTATION OF THE SCREEN*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
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

}
