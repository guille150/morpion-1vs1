package fr.mathis.morpion;

import fr.mathis.morpion.R;
import fr.mathis.morpion.tools.ToolsBDD;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MorpionActivity extends Activity implements OnClickListener {
	
    private static final int MENU_PREF = 0;
	private static final int MENU_AIDE = 1;
	Button buttonNewGame;
    Button buttonQuitGame;
    Button buttonHistory;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Doesn't show the blue bar with the name of the app
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonQuitGame = (Button)findViewById(R.id.buttonQuitGame);
        buttonHistory = (Button)findViewById(R.id.history);
                
        buttonQuitGame.setOnClickListener(this);
        buttonNewGame.setOnClickListener(this);
        buttonHistory.setOnClickListener(this);
    }

    //Click on a button
	public void onClick(View v) 
	{
		if(v.getId() == R.id.buttonQuitGame)
		{
			/*try {
				ToolsBDD.backupDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			finish();
		}
		else if(v.getId() == R.id.buttonNewGame)
		{
			Bundle objetbunble = new Bundle();
			int nbPartie = ToolsBDD.getInstance(this).getNbPartie()+1;
			objetbunble.putString("nb", ""+nbPartie);
			Intent intent = new Intent(MorpionActivity.this, GameActivity.class);
			intent.putExtras(objetbunble);
			startActivity(intent);
		}
		else if(v.getId() == R.id.history)
		{
			if(0 != ToolsBDD.getInstance(this).getNbPartie())
			{
				Intent intent = new Intent(MorpionActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
			else {Toast.makeText(this, R.string.nohistory, Toast.LENGTH_SHORT).show();}
			
		}
	}
	
	/*MENU*/
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_PREF, 0, R.string.menupref).setIcon(android.R.drawable.ic_menu_preferences);
	    menu.add(0, MENU_AIDE, 0, R.string.menuhelp).setIcon(android.R.drawable.ic_menu_help);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_PREF:
			Intent intent = new Intent(MorpionActivity.this, PrefActivity.class);
			startActivity(intent);
	    	return true;
	    case MENU_AIDE:
			Intent intent2 = new Intent(MorpionActivity.this, HelpActivity.class);
			startActivity(intent2);
	    	return true;
	    }
	    return false;
	}
	
	
}