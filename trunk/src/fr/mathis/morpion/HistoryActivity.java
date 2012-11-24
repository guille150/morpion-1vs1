package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;

import fr.mathis.morpion.tools.ToolsBDD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HistoryActivity extends Activity implements OnItemClickListener, OnClickListener, OnItemLongClickListener{

	private static final int MENU_RESET = 0;
	private static final int MENU_QUIT_GAME = 1;
	private static final int MENU_SHARE = 2;
	private static int currentId;
	ArrayList<HashMap<String, String>> listItem;
	private ListView lv;
	Button visu;
	Button effacer;
	Dialog dialog;
	String share;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.listviewcustom);
		
		lv = (ListView)findViewById(R.id.listviewperso);
		
        listItem = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        
        Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0)
		{
			share = getString(R.string.sharetry);
		}
		else 
		{
			share = "Morpion 1vs1 - ";
			int win = 0;
			int lost = 0;
			int equal = 0;
			c.moveToFirst();
			for(int i = 0; i < c.getCount();i++)
			{
				int n = c.getInt(1);
				if(n == GameActivity.BLUE_PLAYER)
				{
			        map = new HashMap<String, String>();
			        map.put("titre", "N°"+c.getInt(0)+" - "+getString(R.string.win));
			        map.put("description", getString(R.string.winb));
			        map.put("img", String.valueOf(R.drawable.croix));
			        win++;
				}
				else if(n == GameActivity.RED_PLAYER){
			        map = new HashMap<String, String>();
			        map.put("titre", "N°"+c.getInt(0)+" - "+getString(R.string.loose));
			        map.put("description", getString(R.string.winr));
			        map.put("img", String.valueOf(R.drawable.cercle));
			        lost++;
				}
				else {
			        map = new HashMap<String, String>();
			        map.put("titre", "N°"+c.getInt(0)+" - "+getString(R.string.equal));
			        map.put("description", getString(R.string.equaltry));
			        map.put("img", String.valueOf(R.drawable.icon));
			        equal++;
				}
		        listItem.add(map);
				c.moveToNext();
			}
			share += (win+lost+equal)+" "+getString(R.string.share1);
			share += " "+win+" "+getString(R.string.share2);
			share += " "+lost+ " " +getString(R.string.share3);
		}
		
        SimpleAdapter mSchedule = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.itemlistviewcustom, new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});
        lv.setAdapter(mSchedule);
        lv.setOnItemLongClickListener(this);
        lv.setOnItemClickListener(this);
	}
		

	private void share() 
	{
		final Intent MessIntent = new Intent(Intent.ACTION_SEND);
    	MessIntent.setType("text/plain");
    	MessIntent.putExtra(Intent.EXTRA_TEXT, share);
    	HistoryActivity.this.startActivity(Intent.createChooser(MessIntent, getString(R.string.sharewith)));
	}

	private void resetHistory() 
	{
		ToolsBDD.getInstance(this).resetTable();
		Toast.makeText(this, R.string.resethistory, Toast.LENGTH_LONG).show();
		finish();
	}

	//Click on an item from the listview
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
		String s = map.get("titre");
		currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.menuhistory);
        dialog.setTitle(R.string.whattodo);
        dialog.show();
		effacer =(Button)dialog.findViewById(R.id.effacerPartie);
		visu = (Button)dialog.findViewById(R.id.voirPartie);
		effacer.setOnClickListener(this);
		visu.setOnClickListener(this);
	}

	//Click on the dialog box from the click event on an item
	public void onClick(View v) {
		if(v.getId() == R.id.voirPartie)
		{
			Bundle objetbunble = new Bundle();
			objetbunble.putString("id", ""+HistoryActivity.currentId);
			Intent intent = new Intent(HistoryActivity.this, VisuActivity.class);
			intent.putExtras(objetbunble);
			dialog.dismiss();	
			startActivity(intent);
		}
		if(v.getId() == R.id.effacerPartie)
		{
			ToolsBDD.getInstance(this).removePartie(HistoryActivity.currentId);
			dialog.dismiss();
			finish();
			if(listItem.size() > 1)
			{
				Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
			else {
				resetHistory();
				Toast.makeText(this, R.string.resethistory, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	        switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	        	resetHistory();
	            break;
	        case DialogInterface.BUTTON_NEGATIVE:
	            break;
	        }
	    }
	};
	
	/*MENU*/
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(android.R.drawable.ic_menu_share);
	    menu.add(0, MENU_RESET, 0, R.string.reset).setIcon(R.drawable.reset);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_RESET:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
	        return true;
	    case MENU_QUIT_GAME:
	    	finish();
	    	return true;
	    case MENU_SHARE:
	    	share();
	    	return true;
	    }
	    return false;
	}


	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
		String s = map.get("titre");
		currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);
		
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", ""+HistoryActivity.currentId);
		Intent intent = new Intent(HistoryActivity.this, VisuActivity.class);
		intent.putExtras(objetbunble);
		startActivity(intent);
		return false;
	}
	
    /*BLOCK THE ROTATION OF THE SCREEN*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
	}

}
