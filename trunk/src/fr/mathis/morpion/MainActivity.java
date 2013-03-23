package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.StateHolder;
import fr.mathis.morpion.tools.ToolsBDD;

@SuppressLint("HandlerLeak")
public class MainActivity extends SherlockActivity implements OnNavigationListener, OnClickListener, OnCheckedChangeListener, OnOpenedListener, OnClosedListener {

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
	boolean amILatestPlayerMulti = false;
	boolean firstIsPlayed = false;
	CheckBox cbSaveEqual;
	CheckBox cbTheme;
	Spinner spBlue;
	Spinner spRed;
	boolean isDark;
	private ShowcaseView sv;
	MenuItem miPref;
	private ShowcaseView sv2;
	Activity a;
	protected ShowcaseView sv3;
	protected BluetoothAdapter mBluetoothAdapter;
	private BluetoothChatService mChatService;
	private StringBuffer mOutStringBuffer;
	public static final int REQUEST_BT = 6;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDark);

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.mainNavigationList, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);

		// configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(isDark ? R.layout.menudark : R.layout.menu);
		menu.setOnOpenedListener(this);
		menu.setOnClosedListener(this);
		// this method is called by the action bar
		// createNewGame();
		a = this;

		if (!StateHolder.GetMemorizedValue("showpref", this)) {
			if (miPref != null) {
				ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
				co.hideOnClickOutside = true;
				sv = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, miPref.getItemId(), this, R.string.sc3, R.string.sc5, co);
				sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {

					@Override
					public void onShowcaseViewShow(ShowcaseView showcaseView) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onShowcaseViewHide(ShowcaseView showcaseView) {
						sv = null;
					}
				});
			} else {
				new Async().execute();
			}
		}

	}

	class Async extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1500);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			sv = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, miPref.getItemId(), a, R.string.sc3, R.string.sc5, co);
			sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {

				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					sv = null;
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		m = menu;
		menu.add(R.string.restart).setIcon((isDark) ? R.drawable.ic_action_replaydark : R.drawable.ic_action_replay).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.getItem(0).setVisible(false);

		miPref = menu.add(R.string.menupref).setIcon((isDark) ? R.drawable.ic_action_prefdark : R.drawable.ic_action_pref);
		miPref.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getTitle().toString().compareTo(getString(R.string.restart)) == 0) {
			createNewGame();
		}
		if (item.getTitle().toString().compareTo(getString(R.string.ctrlz)) == 0) {
			annuler();
			if (annulerList.size() == 0) {
				m.getItem(0).setVisible(false);
			}
		}
		if (item.getTitle().toString().compareTo(getString(R.string.menupref)) == 0) {
			if (sv != null) {
				sv.hide();
				sv = null;
				StateHolder.MemorizeValue("showpref", true, this);
			}
			if (isMenuOpen) {
				menu.toggle(true);
			} else {
				menu.toggle(true);
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onBackPressed() {

		if (sv != null) {
			sv.hide();
			sv = null;
		} else if (sv2 != null) {
			sv2.hide();
			sv2 = null;
		} else {

			if (isMenuOpen) {
				menu.toggle(true);
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if (itemPosition == 0) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			createNewGame();
		}
		if (itemPosition == 2) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			if (0 != ToolsBDD.getInstance(this).getNbPartie()) {
				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
				startActivityForResult(intent, 0);

			} else {
				Toast.makeText(this, R.string.nohistory, Toast.LENGTH_SHORT).show();
				getSupportActionBar().setSelectedNavigationItem(0);
			}
		}
		if (itemPosition == 3) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			setContentView(R.layout.help);

			if (m != null) {
				m.getItem(0).setVisible(false);
			}
			menu.invalidate();
		} else if (itemPosition == 1) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			startBluetooth();
		}
		return false;
	}

	private void startBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.s1s, Toast.LENGTH_LONG).show();
			getSupportActionBar().setSelectedNavigationItem(0);
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			getSupportActionBar().setSelectedNavigationItem(0);
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_BT);
		} else {

			setContentView(R.layout.loadingbluetooth);

			// Initialize the BluetoothChatService to perform bluetooth
			// connections
			mChatService = new BluetoothChatService(this, mHandler);

			// Initialize the buffer for outgoing messages
			mOutStringBuffer = new StringBuffer("");

			startService();

			Button btnBTVisible = (Button) findViewById(R.id.buttonBTVisible);
			btnBTVisible.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
						startActivity(discoverableIntent);
					} else {
						Toast.makeText(getApplicationContext(), R.string.s2s, Toast.LENGTH_LONG).show();
					}
				}
			});

			Button btnBTSearch = (Button) findViewById(R.id.buttonBTSearch);
			btnBTSearch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
				}
			});

			final Button btnBTCut = (Button) findViewById(R.id.buttonBTCut);
			btnBTCut.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
						startService();
						btnBTCut.setText(R.string.s3);
					} else {
						stopService();
						btnBTCut.setText(R.string.s4);
					}

				}
			});

		}

	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.s5, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
		}
	}

	private final Handler mHandler = new Handler() {
		private String mConnectedDeviceName;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.i("d", "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:

					break;
				case BluetoothChatService.STATE_CONNECTING:

					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:

					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);

				if (writeMessage.startsWith("newturn/"))
					handleNewTurn(writeMessage, true);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				if (readMessage.startsWith("newturn/"))
					handleNewTurn(readMessage, false);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), getString(R.string.s7) + " " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

				createNewMuliGame();

				break;
			case MESSAGE_TOAST:
				if (msg.getData().getString(TOAST).compareTo(getString(R.string.s9)) == 0) {
					startBluetooth();
				}
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

	private void handleNewTurn(String writeMessage, boolean isMyTurn) {
		int i, j;

		i = Integer.parseInt(writeMessage.split("/")[1]);
		j = Integer.parseInt(writeMessage.split("/")[2]);
		amILatestPlayerMulti = isMyTurn;

		if (isMyTurn)
			turn = MainActivity.BLUE_PLAYER;
		else {
			turn = MainActivity.RED_PLAYER;
		}

		displayNextTurn();

		Drawable d;
		if (isMyTurn) {
			d = getResources().getDrawable(ColorHolder.getInstance(this).getDrawable(MainActivity.BLUE_PLAYER));
			tabVal[i][j] = BLUE_PLAYER;
		} else {
			d = getResources().getDrawable(ColorHolder.getInstance(this).getDrawable(MainActivity.RED_PLAYER));
			tabVal[i][j] = RED_PLAYER;
		}
		tabIB[i][j].setImageDrawable(d);
		tabIB[i][j].setEnabled(false);
		this.checkWinner(i, j, true);
	}

	@SuppressWarnings("deprecation")
	private void createNewMuliGame() {
		setContentView(R.layout.game);

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		w = metrics.widthPixels;
		if (metrics.heightPixels < w)
			w = metrics.heightPixels;
		int ratio = 5;

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
			if (display.getRotation() == Surface.ROTATION_0)
				ratio = 3;
		} else {
			if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				ratio = 3;
		}

		playerText = (TextView) findViewById(R.id.playerText);
		annulerList = new ArrayList<String>();
		tabIB = new ImageButton[3][3];
		tabVal = new int[3][3];

		tabIB[0][0] = (ImageButton) findViewById(R.id.imageButton1);
		tabIB[0][1] = (ImageButton) findViewById(R.id.imageButton2);
		tabIB[0][2] = (ImageButton) findViewById(R.id.imageButton3);
		tabIB[1][0] = (ImageButton) findViewById(R.id.imageButton4);
		tabIB[1][1] = (ImageButton) findViewById(R.id.imageButton5);
		tabIB[1][2] = (ImageButton) findViewById(R.id.imageButton6);
		tabIB[2][0] = (ImageButton) findViewById(R.id.imageButton7);
		tabIB[2][1] = (ImageButton) findViewById(R.id.imageButton8);
		tabIB[2][2] = (ImageButton) findViewById(R.id.imageButton9);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabIB[i][j].setMinimumWidth((w) / ratio);
				tabIB[i][j].setMinimumHeight((w) / ratio);
				tabIB[i][j].setMaxWidth((w) / ratio);
				tabIB[i][j].setMaxHeight((w) / ratio);

				tabIB[i][j].setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (!amILatestPlayerMulti)
							for (int i = 0; i < 3; i++) {
								for (int j = 0; j < 3; j++) {
									if (v.getId() == tabIB[i][j].getId()) {
										String messageToSend = "";
										messageToSend = "newturn/" + i + "/" + j;
										sendMessage(messageToSend);
									}
								}
							}
					}
				});
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		turn = RED_PLAYER;

		displayNextTurn();
		turn = BLUE_PLAYER;
		if (!isDark)
			playerText.setTextColor(Color.rgb(0, 0, 0));
		else {
			playerText.setTextColor(Color.rgb(255, 255, 255));
		}
		playerText.setText(R.string.s17);

		if (m != null) {
			m.getItem(0).setVisible(false);
		}

		menu.invalidate();

		if (firstIsPlayed) {
			if (amILatestPlayerMulti == true)
				turn = BLUE_PLAYER;
			else
				turn = RED_PLAYER;
			displayNextTurn();
		}

		firstIsPlayed = true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case 0:
			getSupportActionBar().setSelectedNavigationItem(0);
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_BT:
			if (resultCode == Activity.RESULT_OK) {
				getSupportActionBar().setSelectedNavigationItem(1);
			}

			break;
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	public void displayNextTurn() {
		if (turn == RED_PLAYER) {
			playerText.setText(" " + getString(R.string.blue));
			turn = BLUE_PLAYER;
			playerText.setTextColor(Color.parseColor(ColorHolder.getInstance(this).getColor(BLUE_PLAYER)));
		} else {
			playerText.setText(" " + getString(R.string.red));
			turn = RED_PLAYER;
			playerText.setTextColor(Color.parseColor(ColorHolder.getInstance(this).getColor(RED_PLAYER)));
		}

	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void createNewGame() {
		setContentView(R.layout.game);

		cbSaveEqual = (CheckBox) findViewById(R.id.checkBoxSaveEqual);
		cbTheme = (CheckBox) findViewById(R.id.checkBoxSelectTheme);
		spBlue = (Spinner) findViewById(R.id.spinner1);
		spRed = (Spinner) findViewById(R.id.spinner2);
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean save = mgr.getBoolean("save", true);
		final boolean isDark = mgr.getBoolean("isDark", false);
		if (save) {
			cbSaveEqual.setChecked(true);
		}

		if (isDark)
			cbTheme.setChecked(true);

		spBlue.setAdapter(new MySpinnerAdapter(this, ColorHolder.getAllColor()));
		spBlue.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColorHolder.getInstance(getApplicationContext()).save(BLUE_PLAYER, arg2);
				updateField();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				ColorHolder.getInstance(getApplicationContext()).save(BLUE_PLAYER, 0);
				updateField();
			}
		});
		spBlue.setSelection(ColorHolder.getInstance(getApplicationContext()).getColorIndex(BLUE_PLAYER));

		spRed.setAdapter(new MySpinnerAdapter(this, ColorHolder.getAllColor()));
		spRed.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColorHolder.getInstance(getApplicationContext()).save(RED_PLAYER, arg2);
				updateField();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				ColorHolder.getInstance(getApplicationContext()).save(RED_PLAYER, 9);
				updateField();
			}

		});
		spRed.setSelection(ColorHolder.getInstance(getApplicationContext()).getColorIndex(RED_PLAYER));

		cbSaveEqual.setOnCheckedChangeListener(this);
		cbTheme.setOnCheckedChangeListener(this);

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		w = metrics.widthPixels;
		if (metrics.heightPixels < w)
			w = metrics.heightPixels;
		int ratio = 5;

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
			if (display.getRotation() == Surface.ROTATION_0)
				ratio = 3;
		} else {
			if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				ratio = 3;
		}

		playerText = (TextView) findViewById(R.id.playerText);
		annulerList = new ArrayList<String>();
		tabIB = new ImageButton[3][3];
		tabVal = new int[3][3];

		tabIB[0][0] = (ImageButton) findViewById(R.id.imageButton1);
		tabIB[0][1] = (ImageButton) findViewById(R.id.imageButton2);
		tabIB[0][2] = (ImageButton) findViewById(R.id.imageButton3);
		tabIB[1][0] = (ImageButton) findViewById(R.id.imageButton4);
		tabIB[1][1] = (ImageButton) findViewById(R.id.imageButton5);
		tabIB[1][2] = (ImageButton) findViewById(R.id.imageButton6);
		tabIB[2][0] = (ImageButton) findViewById(R.id.imageButton7);
		tabIB[2][1] = (ImageButton) findViewById(R.id.imageButton8);
		tabIB[2][2] = (ImageButton) findViewById(R.id.imageButton9);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabIB[i][j].setMinimumWidth((w) / ratio);
				tabIB[i][j].setMinimumHeight((w) / ratio);
				tabIB[i][j].setMaxWidth((w) / ratio);
				tabIB[i][j].setMaxHeight((w) / ratio);

				tabIB[i][j].setOnClickListener(this);
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		if (nbGame % 2 != 0) {
			turn = RED_PLAYER;
		} else {
			turn = BLUE_PLAYER;
		}

		displayNextTurn();

		if (m != null) {
			m.getItem(0).setVisible(false);
		}

		menu.invalidate();
	}

	private void updateField() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tabVal[i][j] != NONE_PLAYER)
					tabIB[i][j].setImageDrawable(getResources().getDrawable(ColorHolder.getInstance(this).getDrawable(tabVal[i][j])));
			}
		}

		playerText.setTextColor(Color.parseColor(ColorHolder.getInstance(this).getColor(turn)));

	}

	public void recalculateSize() {
		final ScrollView sc = (ScrollView) findViewById(R.id.layoutswipe);
		ViewTreeObserver vto = sc.getViewTreeObserver();
		final Display display = getWindowManager().getDefaultDisplay();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				ViewTreeObserver obs = sc.getViewTreeObserver();

				w = sc.getWidth();

				int h = sc.getHeight() - playerText.getHeight() - (((TextView) findViewById(R.id.welcomeGame)).getHeight());

				if (h < w)
					w = h;
				int ratio = 3;

				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
					if (display.getRotation() == Surface.ROTATION_0)
						ratio = 3;
				} else {
					if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
						ratio = 3;
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						tabIB[i][j].setMinimumWidth((w) / ratio);
						tabIB[i][j].setMinimumHeight((w) / ratio);
						tabIB[i][j].setMaxWidth((w) / ratio);
						tabIB[i][j].setMaxHeight((w) / ratio);
						tabIB[i][j].invalidate();
					}
				}

				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

	public void onClick(View view) {

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (view.getId() == tabIB[i][j].getId()) {
					Drawable d = getResources().getDrawable(ColorHolder.getInstance(this).getDrawable(turn));
					if (turn == BLUE_PLAYER) {
						tabVal[i][j] = BLUE_PLAYER;
						displayNextTurn();
					} else {
						tabVal[i][j] = RED_PLAYER;
						displayNextTurn();
					}
					if (m != null) {
						m.getItem(0).setVisible(true);
					}
					tabIB[i][j].setImageDrawable(d);
					tabIB[i][j].setEnabled(false);
					annulerList.add(i + "," + j);
					menu.invalidate();
					this.checkWinner(i, j, false);

				}
			}
		}
	}

	private void checkWinner(int i, int j, boolean fromBT) {
		if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT);
		else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != NONE_PLAYER)
			congratsWinner(tabVal[1][0], fromBT);
		else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT);
		else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT);
		else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != NONE_PLAYER)
			congratsWinner(tabVal[0][1], fromBT);
		else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][2], fromBT);
		else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT);
		else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT);
		else {
			boolean equal = true;
			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					if (tabVal[x][y] == NONE_PLAYER) {
						equal = false;
						break;
					}
				}
			}

			if (equal) {
				congratsWinner(NONE_PLAYER, fromBT);
			}
		}
	}

	private void congratsWinner(int winner, final boolean fromBT) {
		nbGame++;
		if (fromBT)
			createNewMuliGame();

		if (!fromBT) {
			playerText.setText(R.string.over);
			playerText.setTextColor(Color.WHITE);
		}
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setCancelable(false);
		alertDialog.setIcon(R.drawable.ic_launcher);

		String values = "";
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				values += "," + tabVal[i][j];
			}
		}
		values = values.substring(1);

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean save = mgr.getBoolean("save", true);

		if (winner == BLUE_PLAYER) {
			alertDialog.setTitle(R.string.win);
			alertDialog.setMessage(getString(R.string.winb));
			ToolsBDD.getInstance(this).insertPartie(BLUE_PLAYER, values);
		} else if (winner == RED_PLAYER) {
			alertDialog.setTitle(R.string.win);
			alertDialog.setMessage(getString(R.string.winr));
			ToolsBDD.getInstance(this).insertPartie(RED_PLAYER, values);
		} else if (winner == NONE_PLAYER) {
			alertDialog.setTitle(R.string.equal);
			alertDialog.setMessage(getString(R.string.equaltry));

			if (save)
				ToolsBDD.getInstance(this).insertPartie(NONE_PLAYER, values);
		}

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (save)
					Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();

				if (fromBT)
					;
				else
					createNewGame();
			}
		});
		alertDialog.show();
	}

	private void annuler() {
		if (annulerList.size() > 0) {
			int x, y;
			x = Integer.parseInt(annulerList.get(annulerList.size() - 1).split(",")[0]);
			y = Integer.parseInt(annulerList.get(annulerList.size() - 1).split(",")[1]);
			annulerList.remove(annulerList.size() - 1);

			tabVal[x][y] = NONE_PLAYER;
			tabIB[x][y].setImageDrawable(null);
			tabIB[x][y].setEnabled(true);
			if (turn == BLUE_PLAYER) {
				turn = RED_PLAYER;
				playerText.setText(getString(R.string.red));
				playerText.setTextColor(Color.RED);
			} else {
				turn = BLUE_PLAYER;
				playerText.setText(getString(R.string.blue));
				playerText.setTextColor(Color.rgb(0, 148, 255));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean isLandscape = true;
		if (getSupportActionBar().getSelectedNavigationIndex() == 0) {
			Display display = getWindowManager().getDefaultDisplay();

			int ratio = 5;

			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
				if (display.getRotation() == Surface.ROTATION_0) {
					ratio = 3;
					isLandscape = false;
				}
			} else {
				if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
					ratio = 3;
					isLandscape = false;
				}
			}

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					tabIB[i][j].setMinimumHeight(w / ratio);
					tabIB[i][j].setMaxHeight(w / ratio);
					tabIB[i][j].setMinimumWidth(w / ratio);
					tabIB[i][j].setMaxWidth(w / ratio);
				}
			}

			recalculateSize();
		}
		if (sv != null)
			sv.hide();
		if (!StateHolder.GetMemorizedValue("showpref", this)) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			sv = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM, miPref.getItemId(), this, "", isLandscape ? "" : "", co);
			sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {

				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
				}

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					sv = null;
				}
			});
		}
		if (sv2 != null)
			sv2.hide();
		if (isMenuOpen && !StateHolder.GetMemorizedValue("changedtheme", this)) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			sv2 = ShowcaseView.insertShowcaseView(R.id.checkBoxSelectTheme, this, "", "", co);
			sv2.setOnShowcaseEventListener(new OnShowcaseEventListener() {

				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
				}

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					sv2 = null;
				}
			});
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.checkBoxSaveEqual) {
			SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = mgr.edit();
			editor.putBoolean("save", isChecked);
			editor.commit();
		}

		if (buttonView.getId() == R.id.checkBoxSelectTheme) {
			SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = mgr.edit();
			editor.putBoolean("isDark", isChecked);
			editor.commit();
			finish();
			startActivity(getIntent());
			StateHolder.MemorizeValue("changedtheme", true, getApplicationContext());
		}
	}

	@Override
	public void onOpened() {
		isMenuOpen = true;
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (!StateHolder.GetMemorizedValue("changedtheme", this)) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;
			sv2 = ShowcaseView.insertShowcaseView(R.id.checkBoxSelectTheme, this, R.string.sc3, R.string.sc4, co);
			sv2.setOnShowcaseEventListener(new OnShowcaseEventListener() {
				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
				}

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					sv2 = null;
				}
			});
		}
	}

	@Override
	public void onClosed() {
		isMenuOpen = false;
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		startService();
	}

	public void startService() {
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	public void stopService() {
		if (mChatService != null)
			mChatService.stop();
	}

	public class MySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
		private Activity activity;
		private List<String> list_bsl;

		public MySpinnerAdapter(Activity activity, List<String> list_bsl) {
			this.activity = activity;
			this.list_bsl = list_bsl;
		}

		public int getCount() {
			return list_bsl.size();
		}

		public Object getItem(int position) {
			return list_bsl.get(position);
		}

		public long getItemId(int position) {
			return list_bsl.hashCode();
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			View spinView;
			if (convertView == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				spinView = inflater.inflate(R.layout.item_spinner_color, null);
			} else {
				spinView = convertView;
			}
			LinearLayout layout = (LinearLayout) spinView;
			layout.setBackgroundColor(Color.parseColor((String) getItem(position)));
			return spinView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View spinView;
			if (convertView == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				spinView = inflater.inflate(R.layout.item_spinner_color, null);
			} else {
				spinView = convertView;
			}
			LinearLayout layout = (LinearLayout) spinView;
			layout.setBackgroundColor(Color.parseColor((String) getItem(position)));
			return spinView;
		}
	}
}
