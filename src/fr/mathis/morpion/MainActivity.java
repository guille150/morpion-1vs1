package fr.mathis.morpion;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.GameView.GameHandler;
import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.StateHolder;
import fr.mathis.morpion.tools.ToolsBDD;

@SuppressLint("HandlerLeak")
public class MainActivity extends SherlockActivity implements OnClickListener, OnItemClickListener {

	public static final int RED_PLAYER = 4;
	public static final int BLUE_PLAYER = 3;
	public static final int NONE_PLAYER = 5;

	public static final int REQUEST_BT = 6;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_PREF = 7;

	private ActionBarDrawerToggle mDrawerToggle;
	ArrayList<NavigationItem> navItems;
	NavigationAdapter navAdapter;
	private int activeNavItem = 0;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	TextView playerText;
	ImageButton[][] tabIB;
	int[][] tabVal;
	int turn = BLUE_PLAYER;
	int nbGame;
	int w;
	Menu m;
	boolean isMenuOpen = false;
	boolean amILatestPlayerMulti = false;
	boolean firstIsPlayed = false;
	boolean computerStarted = false;
	boolean isDark;
	boolean shouldRestartBeVisible = false;
	boolean initdone = false;
	MenuItem miPref;
	Activity a;
	GameView gv;
	protected BluetoothAdapter mBluetoothAdapter;
	private BluetoothChatService mChatService;
	private StringBuffer mOutStringBuffer;
	LinearLayout container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDark);
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		a = this;
		setContentView(isDark ? R.layout.main_layout_dark : R.layout.main_layout);
		container = (LinearLayout) findViewById(R.id.container);
		initDrawer();
		createNewGame();
		if (!StateHolder.GetMemorizedValue("showwelcomedrawer", this) || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			new Async().execute();
		}
	}

	class Async extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mDrawerLayout.openDrawer(GravityCompat.START);
			StateHolder.MemorizeValue("showwelcomedrawer", true, getApplicationContext());
			super.onPostExecute(result);
		}
	}

	private void initDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		navItems = new ArrayList<MainActivity.NavigationItem>();

		navItems.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiemultidark : R.drawable.ic_action_spinner_partiemulti, getString(R.string.m1), 0));
		navItems.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiemultidark : R.drawable.ic_action_spinner_partiemulti, getString(R.string.m8), 0));
		navItems.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiedark : R.drawable.ic_action_spinner_partie, getString(R.string.s31), 0));
		navItems.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_savedark : R.drawable.ic_action_spinner_save, getString(R.string.m2), 0));
		navItems.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiehelpdark : R.drawable.ic_action_spinner_partiehelp, getString(R.string.m3), 0));

		navAdapter = new NavigationAdapter(a, navItems);
		mDrawerList.setAdapter(navAdapter);

		mDrawerList.setOnItemClickListener(this);
		showClosedIcon();

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
				showClosedIcon();
			}

			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();
				shouldRestartBeVisible = m.getItem(0).isVisible();
				showOpenedIcon();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		boolean forseFirst = false;
		if (pos == 0) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			createNewGame();

		}
		if (pos == 3) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			if (0 != ToolsBDD.getInstance(this).getNbPartie()) {
				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
				startActivityForResult(intent, 0);
			} else {
				Toast.makeText(this, R.string.nohistory, Toast.LENGTH_SHORT).show();
				onItemClick(null, null, 0, 0);
				forseFirst = true;
			}
		}
		if (pos == 4) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			View child = getLayoutInflater().inflate(isDark ? R.layout.helpdark : R.layout.help, null);
			container.removeAllViews();
			container.addView(child);

			if (m != null) {
				m.getItem(0).setVisible(false);
			}
		} else if (pos == 1) {
			getSupportActionBar().setIcon(R.drawable.two_player);
			startBluetooth();
		} else if (pos == 2) {
			getSupportActionBar().setIcon(R.drawable.ic_launcher);
			createNewGameAI();
		}
		if (pos != 1 && !forseFirst) {
			if (pos != 3) {
				activeNavItem = pos;
				navAdapter.notifyDataSetChanged();
			}
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
	}

	public class NavigationItem {
		public int icon;
		public String title;
		public int nbNews;

		public NavigationItem(int icon, String title, int nbNews) {
			super();
			this.icon = icon;
			this.title = title;
			this.nbNews = nbNews;
		}
	}

	public class NavigationAdapter extends BaseAdapter {

		Context context;
		ArrayList<NavigationItem> data;
		LayoutInflater inflater;

		public NavigationAdapter(Context a, ArrayList<NavigationItem> data) {
			this.data = data;
			inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = a;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = inflater.inflate(R.layout.navigation_item, null);
			final NavigationItem item = data.get(position);
			if (item != null) {
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setText(item.title);
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(item.icon), null, null, null);
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setCompoundDrawablePadding((int) convertDpToPixel(16.0f, getApplicationContext()));
				if (activeNavItem == position)
					v.setBackgroundColor(isDark ? Color.parseColor("#33B5E5") : Color.parseColor("#D3D3D3"));
			}
			return v;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	private void showClosedIcon() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
		if (m != null)
			m.getItem(0).setVisible(shouldRestartBeVisible);
	}

	private void showOpenedIcon() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (m != null)
			m.getItem(0).setVisible(false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
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

		if (mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}

		if (item.getTitle().toString().compareTo(getString(R.string.restart)) == 0) {
			if (activeNavItem == 0)
				createNewGame();
			else if (activeNavItem == 1) {

			} else {
				createNewGameAI();
			}
		}

		if (item.getTitle().toString().compareTo(getString(R.string.menupref)) == 0) {
			Intent prefIntent = new Intent(this, PreferencesActivity.class);
			startActivityForResult(prefIntent, REQUEST_PREF);
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createNewGameAI() {
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);
		container.removeAllViews();
		container.addView(child);

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();

		playerText = (TextView) findViewById(R.id.playerText);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		gv.setDelegate(new GameHandler() {
			@Override
			public void handleTurn(int i, int j) {
				if (turn == BLUE_PLAYER) {
					tabVal[i][j] = BLUE_PLAYER;
					gv.setValues(tabVal, BLUE_PLAYER);
					gv.invalidate();
					makeTheAIPlay(i, j);
				} else {
					tabVal[i][j] = RED_PLAYER;
					gv.setValues(tabVal, BLUE_PLAYER);
					gv.invalidate();
					makeTheAIPlay(i, j);
				}
				if (m != null) {
					m.getItem(0).setVisible(true);
				}
				checkWinner(i, j, false, true);

			}
		});

		if (nbGame % 2 != 0) {
			computerStarted = true;
			makeTheAIPlay(-1, -1);
			turn = BLUE_PLAYER;

			displayNextTurn();
			displayNextTurn();
		} else {
			computerStarted = false;
			turn = BLUE_PLAYER;

			displayNextTurn();
			displayNextTurn();
		}

		gv.setValues(tabVal, turn);
		if (m != null) {
			m.getItem(0).setVisible(false);
		}

	}

	private void makeTheAIPlay(int lastI, int lastJ) {
		int i, j;
		i = -1;
		j = -1;

		Point p = doINeedToPlaceItThere();
		if (p.x != -1 && p.y != -1) {
			i = p.x;
			j = p.y;
		}

		if (computerStarted) {

			if (numberOfTurn() == 0) {
				i = 0;
				j = 0;
			}

			if (numberOfTurn() == 1) {
				if (lastI == 1 && lastJ == 1) {
					i = 2;
					j = 2;
				} else if (tabVal[0][2] == BLUE_PLAYER) {
					i = 2;
					j = 0;
				} else if (tabVal[2][0] == BLUE_PLAYER) {
					i = 0;
					j = 2;
				} else if (tabVal[2][2] == BLUE_PLAYER) {
					i = 2;
					j = 0;
				} else if (tabVal[0][1] == BLUE_PLAYER) {
					i = 2;
					j = 0;
				} else if (tabVal[1][0] == BLUE_PLAYER) {
					i = 0;
					j = 2;
				} else if (tabVal[1][2] == BLUE_PLAYER) {
					i = 0;
					j = 2;
				} else if (tabVal[2][1] == BLUE_PLAYER) {
					i = 2;
					j = 0;
				}
			}

			if (numberOfTurn() == 2 && i == -1 && j == -1) {

				if (tabVal[0][1] == BLUE_PLAYER && tabVal[1][0] == BLUE_PLAYER && tabVal[1][1] == NONE_PLAYER) {
					i = 1;
					j = 1;
				} else if (tabVal[0][2] == NONE_PLAYER) {
					i = 0;
					j = 2;
				} else if (tabVal[2][2] == NONE_PLAYER) {
					i = 2;
					j = 2;
				} else if (tabVal[2][0] == NONE_PLAYER) {
					i = 2;
					j = 0;
				}
			}
		} else {

			if (numberOfTurn() == 0 && tabVal[1][1] == NONE_PLAYER) {
				i = 1;
				j = 1;
			}

			if (numberOfTurn() == 1 && i == -1 && j == -1) {
				if (tabVal[1][1] == RED_PLAYER) {

					if (tabVal[0][1] == BLUE_PLAYER && tabVal[1][0] == BLUE_PLAYER) {
						i = 0;
						j = 0;
					}

					else if (tabVal[2][1] == BLUE_PLAYER && tabVal[1][2] == BLUE_PLAYER) {
						i = 2;
						j = 2;
					}

					else if (tabVal[1][0] == BLUE_PLAYER && tabVal[2][1] == BLUE_PLAYER) {
						i = 2;
						j = 0;
					}

					else if (tabVal[0][1] == BLUE_PLAYER && tabVal[1][2] == BLUE_PLAYER) {
						i = 0;
						j = 2;
					}

					else if ((tabVal[0][1] == BLUE_PLAYER && tabVal[2][0] == BLUE_PLAYER) || (tabVal[0][1] == BLUE_PLAYER && tabVal[2][2] == BLUE_PLAYER)) {
						i = 1;
						j = 2;
					}

					else if (tabVal[2][1] == NONE_PLAYER) {
						i = 2;
						j = 1;
					}

					else if (tabVal[1][2] == NONE_PLAYER) {
						i = 1;
						j = 2;
					}
				}
			}

		}

		if ((i == -1 && j == -1) || (tabVal[i][j] != NONE_PLAYER)) {
			p = findRandomPlace();
			i = p.x;
			j = p.y;
		}

		if (tabVal[i][j] == NONE_PLAYER) {
			tabVal[i][j] = RED_PLAYER;
			gv.setValues(tabVal, BLUE_PLAYER);
			gv.invalidate();
		}
	}

	private Point findRandomPlace() {
		Point p = new Point();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tabVal[i][j] == NONE_PLAYER) {
					p = new Point(i, j);
					break;
				}
			}

		}
		return p;
	}

	private int numberOfTurn() {
		int res = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tabVal[i][j] == RED_PLAYER)
					res++;
			}

		}
		return res;
	}

	private Point doINeedToPlaceItThere() {
		Point p = new Point();
		p.x = -1;
		p.y = -1;

		boolean isToPreventVictory = true;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tabVal[i][j] == NONE_PLAYER && isToPreventVictory) {

					tabVal[i][j] = RED_PLAYER;

					if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					} else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER) {
						p.x = i;
						p.y = j;
						isToPreventVictory = false;
					}

					if (p.x == -1 && p.y == -1 && isToPreventVictory) {
						tabVal[i][j] = BLUE_PLAYER;

						if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						} else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER) {
							p.x = i;
							p.y = j;
						}
					}
					tabVal[i][j] = NONE_PLAYER;
				}
			}
		}

		return p;
	}

	private void startBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.s1s, Toast.LENGTH_LONG).show();
			onItemClick(null, null, 0, 0);
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			onItemClick(null, null, 0, 0);
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_BT);
		} else {
			View child = getLayoutInflater().inflate(isDark ? R.layout.loadingbluetoothdark : R.layout.loadingbluetooth, null);

			container.removeAllViews();
			container.addView(child);
			activeNavItem = 1;
			navAdapter.notifyDataSetChanged();
			mDrawerLayout.closeDrawer(GravityCompat.START);
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

		if (isMyTurn) {
			tabVal[i][j] = BLUE_PLAYER;
		} else {
			tabVal[i][j] = RED_PLAYER;
		}

		if (gv != null)
			gv.setValues(tabVal, turn);
		this.checkWinner(i, j, true, false);
	}

	private void createNewMuliGame() {
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);

		container.removeAllViews();
		container.addView(child);

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();

		playerText = (TextView) findViewById(R.id.playerText);
		tabIB = new ImageButton[3][3];
		tabVal = new int[3][3];

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
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

		if (firstIsPlayed) {
			if (amILatestPlayerMulti == true)
				turn = BLUE_PLAYER;
			else
				turn = RED_PLAYER;
			displayNextTurn();
		}

		firstIsPlayed = true;

		gv.setDelegate(new GameHandler() {

			@Override
			public void handleTurn(int i, int j) {
				if (!amILatestPlayerMulti) {
					String messageToSend = "";
					messageToSend = "newturn/" + i + "/" + j;
					sendMessage(messageToSend);
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_INSECURE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_BT:
			if (resultCode == Activity.RESULT_OK) {
				onItemClick(null, null, 1, 0);
			}

			break;
		case REQUEST_PREF:
			if (resultCode == RESULT_OK) {
				finish();
				startActivity(getIntent());
			} else {
				updateField();
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
	public void createNewGame() {
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);

		container.removeAllViews();
		container.addView(child);
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean isDark = mgr.getBoolean("isDark", false);

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();
		gv.invalidate();

		playerText = (TextView) findViewById(R.id.playerText);
		tabIB = new ImageButton[3][3];
		tabVal = new int[3][3];

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		gv.setDelegate(new GameHandler() {
			@Override
			public void handleTurn(int i, int j) {
				if (turn == BLUE_PLAYER) {
					tabVal[i][j] = BLUE_PLAYER;
					displayNextTurn();
					gv.setValues(tabVal, turn);
				} else {
					tabVal[i][j] = RED_PLAYER;
					displayNextTurn();
					gv.setValues(tabVal, turn);
				}
				if (m != null && activeNavItem != 1) {
					m.getItem(0).setVisible(true);
				}
				checkWinner(i, j, false, false);

			}
		});

		if (nbGame % 2 != 0) {
			turn = RED_PLAYER;
		} else {
			turn = BLUE_PLAYER;
		}

		displayNextTurn();
		gv.setValues(tabVal, turn);
		if (m != null) {
			m.getItem(0).setVisible(false);
		}

	}

	private void updateField() {
		if (gv != null) {
			gv.loadcolors();
			gv.invalidate();
		}

		playerText.setTextColor(Color.parseColor(ColorHolder.getInstance(this).getColor(turn)));
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
					if (m != null && activeNavItem != 1) {
						m.getItem(0).setVisible(true);
					}
					tabIB[i][j].setImageDrawable(d);
					tabIB[i][j].setEnabled(false);
					this.checkWinner(i, j, false, false);

				}
			}
		}
	}

	private void checkWinner(int i, int j, boolean fromBT, boolean fromMulti) {
		if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti);
		else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != NONE_PLAYER)
			congratsWinner(tabVal[1][0], fromBT, fromMulti);
		else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT, fromMulti);
		else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti);
		else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != NONE_PLAYER)
			congratsWinner(tabVal[0][1], fromBT, fromMulti);
		else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][2], fromBT, fromMulti);
		else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti);
		else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT, fromMulti);
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
				congratsWinner(NONE_PLAYER, fromBT, fromMulti);
			}
		}
	}

	private void congratsWinner(int winner, final boolean fromBT, final boolean fromMulti) {
		nbGame++;
		if (!fromBT) {
			playerText.setText(R.string.over);
			if (isDark)
				playerText.setTextColor(Color.WHITE);
			else
				playerText.setTextColor(Color.DKGRAY);
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

		if (fromBT)
			createNewMuliGame();

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
				else if (fromMulti)
					createNewGameAI();
				else
					createNewGame();
			}
		});
		alertDialog.show();
	}

	@SuppressLint("NewApi")
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// mDrawerToggle.onConfigurationChanged(newConfig);
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

	private android.view.MenuItem getMenuItem(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {
				return false;
			}

			@Override
			public boolean expandActionView() {
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				return null;
			}

			@Override
			public View getActionView() {
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				return 0;
			}

			@Override
			public int getGroupId() {
				return 0;
			}

			@Override
			public Drawable getIcon() {
				return null;
			}

			@Override
			public Intent getIntent() {
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				return null;
			}

			@Override
			public char getNumericShortcut() {
				return 0;
			}

			@Override
			public int getOrder() {
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				return null;
			}

			@Override
			public CharSequence getTitle() {
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				return false;
			}

			@Override
			public boolean isCheckable() {
				return false;
			}

			@Override
			public boolean isChecked() {
				return false;
			}

			@Override
			public boolean isVisible() {
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(ActionProvider actionProvider) {
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar, char alphaChar) {
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				return null;
			}
		};
	}

}
