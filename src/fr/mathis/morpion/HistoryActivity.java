package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.tools.ToolsBDD;



public class HistoryActivity extends SherlockActivity implements OnItemLongClickListener, OnItemClickListener {

	static final int MENU_RESET = 0;
	static final int MENU_SHARE = 2;
	private static int currentId;
	ArrayList<HashMap<String, String>> listItem;
	private ListView lv;
	Button visu;
	Button effacer;
	Dialog dialog;
	String share;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
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
			share = getString(R.string.app_name)+" - https://play.google.com/store/apps/details?id=fr.mathis.morpion - ";
			int win = 0;
			int lost = 0;
			int equal = 0;
			c.moveToFirst();
			for(int i = 0; i < c.getCount();i++)
			{
				int n = c.getInt(1);
				if(n == MainActivity.BLUE_PLAYER)
				{
			        map = new HashMap<String, String>();
			        map.put("titre", "N°"+c.getInt(0)+" - "+getString(R.string.win));
			        map.put("description", getString(R.string.winb));
			        map.put("img", String.valueOf(R.drawable.croix));
			        win++;
				}
				else if(n == MainActivity.RED_PLAYER){
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
		
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

    	if(item.getTitle().toString().compareTo(getString(R.string.share))==0)
    	{
    		share();
    	}
    	else if(item.getTitle().toString().compareTo(getString(R.string.reset))==0)
    	{
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
    	}
    	else {
		    int itemId = item.getItemId();
		    switch (itemId) {
		    case android.R.id.home:
	
		        finish();
		        break;

		    }
    	}

	    return true;
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
		
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", ""+HistoryActivity.currentId);
		Intent intent = new Intent(HistoryActivity.this, VisuActivity.class);
		intent.putExtras(objetbunble);
		startActivity(intent);
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
		menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(R.drawable.social_share2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);;
	    menu.add(0, MENU_RESET, 0, R.string.reset).setIcon(R.drawable.content_discard2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);;
	    return true;
	}
	
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
		String s = map.get("titre");
		currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);
		final Context c = getApplicationContext();
	    final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.deletegame);
	    dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	        	ToolsBDD.getInstance(c).removePartie(HistoryActivity.currentId);
				dialog.dismiss();
				finish();
				if(listItem.size() > 1)
				{
					Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
					startActivity(intent);
				}
				else {
					resetHistory();
					Toast.makeText(c, getString(R.string.resethistory), Toast.LENGTH_LONG).show();
				}
	        }
	    });
	    dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();
	        }
	    });
	    AlertDialog alert = dialog.create();		
		alert.show();
		
		
		return false;
	}
	
    /*BLOCK THE ROTATION OF THE SCREEN*/
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
	}
}
