package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.StateHolder;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.views.GameView;
import fr.mathis.morpion.views.GameView.GameHandler;

@TargetApi(19)
@SuppressLint("HandlerLeak")
public class MainActivity extends BaseGameActivity implements OnClickListener, OnItemClickListener, OnChildClickListener, OnStateLoadedListener, RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener, OnInvitationReceivedListener {

	public static final String STATE_ACTIVE = "active";
	public static final String STATE_SECTION = "activeSection";
	public static final String STATE_CHILD = "activeChild";

	public static final int RED_PLAYER = 4;
	public static final int BLUE_PLAYER = 3;
	public static final int NONE_PLAYER = 5;

	public static final int REQUEST_BT = 6;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final int SAVE_PREF = 0;
	public static final int SAVE_HIST_1 = 1;
	public static final int SAVE_HIST_2 = 2;
	public static final int SAVE_HIST_3 = 3;

	public static final String SAVE_DELETED = "00";
	public static final String SAVE_TIE = "01";
	public static final String SAVE_BLUE_WIN = "10";
	public static final String SAVE_RED_WIN = "11";

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_PREF = 7;
	private static final int REQUEST_PATERN = 37;
	private static final int ACTIVITY_HISTORY = 36;
	public static final String ACTIVITY_HISTORY_RES_GROUP = "p1";
	public static final String ACTIVITY_HISTORY_RES_GROUP_CHILD = "p2";
	final static int RC_INVITATION_INBOX = 10001;
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_WAITING_ROOM = 10002;

	public static final String CMD_DETERMINE_TURN = "playerid";
	public static final String CMD_PLAY = "newturn";
	public static final String CMD_WANTS_RESTART = "wantsrestart";
	public static final String CMD_RESTART = "restart";

	private ActionBarDrawerToggle mDrawerToggle;
	ArrayList<NavigationSection> navSections;
	NavigationAdapter navAdapter;
	private int activeNavSection = 0;
	private int activeNavChild = 0;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	LinearLayout playerText;
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
	boolean finishedAI = false;
	boolean comeBackFromHistoryShouldAchievement = false;
	boolean oneGameHasBeenPlayedWeCanSave = false;
	MenuItem miPref;
	MenuItem miDeco;
	boolean shouldShowDeco = false;
	MenuItem miStopOnline;
	Activity activContext;
	GameView gv;
	protected BluetoothAdapter mBluetoothAdapter;
	private BluetoothChatService mChatService;
	private StringBuffer mOutStringBuffer;
	FrameLayout container;
	View congratsContainer;
	TextView retrycount;
	int winnerForUpdateField = -1;

	boolean firstStartShouldReloadConfig = true;
	boolean comeBackFromSettingsShouldSave = false;
	boolean userMightNotWantToLeave = false;
	boolean forceNoPatern = true;

	boolean isPlayingBluetooth = false;
	boolean avoidNextBluetoothMessage = false;

	boolean isPlayingOnline = false;
	String roomId = null;
	Room myroom = null;
	ProgressDialog progress;
	GameHandler onlineHandler;
	int[] pendingAction;
	boolean shouldOpenHistory = false;
	boolean isPreferedPage = false;

	public MainActivity() {
		super(BaseGameActivity.CLIENT_APPSTATE | BaseGameActivity.CLIENT_GAMES);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDark);
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		activeNavChild = 2;
		getSupportActionBar().setTitle(R.string.s31);
		activContext = this;
		setContentView(isDark ? R.layout.main_layout_dark : R.layout.main_layout);
		setSupportProgressBarIndeterminateVisibility(false);
		container = (FrameLayout) findViewById(R.id.container);

		initDrawer();
		init();

		if (StateHolder.GetMemorizedValue(STATE_ACTIVE, getApplicationContext())) {

			int iSection = StateHolder.GetMemorizedValueInt(STATE_SECTION, getApplicationContext());
			int iChild = StateHolder.GetMemorizedValueInt(STATE_CHILD, getApplicationContext());

			if (iSection == 0 && iChild == 3) {
				pendingAction = new int[] { iSection, iChild };
			} else if (iSection == 1 && iChild == 0) {
				onChildClick(null, null, 0, 0, 0);
				isPreferedPage = true;
				onChildClick(null, null, iSection, iChild, 0);
			} else {
				if (iSection == 0 && iChild == 1) {
					iChild = 2;
				}
				onChildClick(null, null, iSection, iChild, 0);
			}
		} else {
			createNewGameAI();
		}
		if (!StateHolder.GetMemorizedValue("showwelcomedrawer", this) || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			new Async().execute();
		}
	}

	public void init() {
		playerText = (LinearLayout) findViewById(R.id.playerText);
		tabVal = new int[3][3];
	}

	private void generateActionBarIcon() {

		GameView vGame = (GameView) findViewById(R.id.gameViewForIcon);
		int[][] values = new int[][] { new int[] { RED_PLAYER, RED_PLAYER, BLUE_PLAYER }, new int[] { BLUE_PLAYER, BLUE_PLAYER, RED_PLAYER }, new int[] { RED_PLAYER, BLUE_PLAYER, RED_PLAYER } };
		vGame.setValues(values, BLUE_PLAYER);
		vGame.loadcolors();
		vGame.setStrikeWidth(1);

		View v = findViewById(R.id.IconContainer);
		if (v.getWidth() != 0 && v.getHeight() != 0) {
			Bitmap returnedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(returnedBitmap);
			Drawable bgDrawable = v.getBackground();
			if (bgDrawable != null)
				bgDrawable.draw(canvas);
			v.draw(canvas);

			Drawable d = new BitmapDrawable(getResources(), returnedBitmap);
			getSupportActionBar().setIcon(d);
		}
	}

	class Async extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mDrawerLayout != null)
				mDrawerLayout.openDrawer(GravityCompat.START);
			StateHolder.MemorizeValue("showwelcomedrawer", true, getApplicationContext());
			super.onPostExecute(result);
		}
	}

	private void initDrawer() {
		if (findViewById(R.id.drawer_layout) != null) {
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		}
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawerlist);

		findViewById(R.id.sign_in_button).setOnClickListener(this);

		navSections = new ArrayList<MainActivity.NavigationSection>();

		ArrayList<NavigationItem> n1 = new ArrayList<MainActivity.NavigationItem>();
		n1.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiemultidark : R.drawable.ic_action_spinner_partiemulti, getString(R.string.m1), 0));
		n1.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiemultidark : R.drawable.ic_action_spinner_partiemulti, getString(R.string.m8), 0));
		n1.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiedark : R.drawable.ic_action_spinner_partie, getString(R.string.s31), 0));
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
			n1.add(new NavigationItem(isDark ? R.drawable.ic_action_onlinedark : R.drawable.ic_action_online, getString(R.string.s44), 0));
		} else {
			findViewById(R.id.sign_in_button).setVisibility(View.GONE);

			findViewById(R.id.layoutPlayService).setVisibility(View.VISIBLE);
			findViewById(R.id.playgo).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final String appName = "com.google.android.gms";
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
					}
					finish();
				}
			});
		}
		NavigationSection s1 = new NavigationSection(getString(R.string.s39), n1);
		navSections.add(s1);

		ArrayList<NavigationItem> n2 = new ArrayList<MainActivity.NavigationItem>();
		n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_savedark : R.drawable.ic_action_spinner_save, getString(R.string.m2), 0));
		// n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiehelpdark : R.drawable.ic_action_spinner_partiehelp, getString(R.string.m3), 0));
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
			n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_achivmentdark : R.drawable.ic_action_spinner_achivment, getString(R.string.s37), 0));
			n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_boarddarl : R.drawable.ic_action_spinner_board, getString(R.string.s38), 0));
		}

		NavigationSection s2 = new NavigationSection(getString(R.string.s40), n2);
		navSections.add(s2);

		navAdapter = new NavigationAdapter(activContext, navSections);
		mDrawerList.setAdapter(navAdapter);
		mDrawerList.setDividerHeight(0);
		mDrawerList.setOnItemClickListener(this);
		mDrawerList.setOnChildClickListener(this);

		for (int i = 0; i < navAdapter.getGroupCount(); i++)
			mDrawerList.expandGroup(i);

		mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		if (mDrawerLayout != null) {
			showClosedIcon();
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
				public void onDrawerClosed(View view) {
					if (shouldOpenHistory) {
						Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
						intent.putExtra("isSigned", isSignedIn());
						startActivityForResult(intent, ACTIVITY_HISTORY);
						overridePendingTransition(0, 0);
						shouldOpenHistory = false;
					} else {
						supportInvalidateOptionsMenu();
						showClosedIcon();
					}
				}

				public void onDrawerOpened(View drawerView) {
					supportInvalidateOptionsMenu();
					showOpenedIcon();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		onChildClick(null, null, 0, pos, 0);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		boolean shouldCloseDrawer = true;
		if (activeNavChild != childPosition || activeNavSection != groupPosition) {
			shouldRestartBeVisible = false;
		}
		if (isPlayingOnline) {
			Toast.makeText(getApplicationContext(), R.string.s48, Toast.LENGTH_SHORT).show();
		} else if (isPlayingBluetooth) {
			Toast.makeText(getApplicationContext(), R.string.s48, Toast.LENGTH_SHORT).show();
		} else {
			boolean forseFirst = false;

			if (groupPosition == 0 && childPosition == 3) {
				if (isSignedIn()) {
					createOnlineScreen();
				} else {
					Toast.makeText(getApplicationContext(), R.string.s57, Toast.LENGTH_SHORT).show();
					shouldCloseDrawer = false;
				}
			}
			if (groupPosition == 0 && childPosition == 0) {
				createNewGame();
			}
			if (groupPosition == 1 && childPosition == 0) {
				if (mDrawerLayout == null || isPreferedPage) {
					Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
					intent.putExtra("isSigned", isSignedIn());
					startActivityForResult(intent, ACTIVITY_HISTORY);
					overridePendingTransition(0, 0);
				} else {
					shouldOpenHistory = true;
				}

				isPreferedPage = false;
			}
			if (groupPosition == 0 && childPosition == 1) {
				startBluetooth();
			} else if (groupPosition == 0 && childPosition == 2) {
				createNewGameAI();
			}

			if (groupPosition == 1 && childPosition == 1) {
				if (isSignedIn())
					startActivityForResult(getGamesClient().getAchievementsIntent(), 0);
				else
					Toast.makeText(getApplicationContext(), R.string.s57, Toast.LENGTH_SHORT).show();
				shouldCloseDrawer = false;
			}
			if (groupPosition == 1 && childPosition == 2) {
				if (isSignedIn())
					startActivityForResult(getGamesClient().getLeaderboardIntent(getString(R.string.leaderboard_most_active)), 0);
				else
					Toast.makeText(getApplicationContext(), R.string.s57, Toast.LENGTH_SHORT).show();
				shouldCloseDrawer = false;
			}

			if (!(groupPosition == 0 && childPosition == 1) && !forseFirst) {
				if (!(groupPosition == 1 && childPosition == 0) && !(groupPosition == 2 && childPosition == 1) && !(groupPosition == 2 && childPosition == 0) && shouldCloseDrawer) {
					activeNavChild = childPosition;
					activeNavSection = groupPosition;
					navAdapter.notifyDataSetChanged();
					getSupportActionBar().setDisplayShowTitleEnabled(true);
					getSupportActionBar().setTitle(navSections.get(activeNavSection).items.get(activeNavChild).title);
				}
				if (shouldCloseDrawer && mDrawerLayout != null)
					mDrawerLayout.closeDrawer(GravityCompat.START);
				userMightNotWantToLeave = false;
			}

		}
		return true;
	}

	public class NavigationSection {
		public String title;
		public ArrayList<NavigationItem> items;

		public NavigationSection(String title, ArrayList<NavigationItem> items) {
			super();
			this.title = title;
			this.items = items;
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

	public class NavigationAdapter extends BaseExpandableListAdapter {

		Context context;
		ArrayList<NavigationSection> data;
		LayoutInflater inflater;

		public NavigationAdapter(Context a, ArrayList<NavigationSection> data) {
			this.data = data;
			inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = a;
		}

		@Override
		public Object getChild(int arg0, int arg1) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View v = inflater.inflate(isDark ? R.layout.navigation_itemdark : R.layout.navigation_item, null);
			final NavigationItem item = data.get(groupPosition).items.get(childPosition);
			if (item != null) {
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setText(item.title);
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(item.icon), null, null, null);
				((android.widget.TextView) v.findViewById(R.id.nav_title)).setCompoundDrawablePadding((int) convertDpToPixel(8.0f, getApplicationContext()));
				v.findViewById(R.id.separator_little).setVisibility(childPosition == 0 ? View.GONE : View.VISIBLE);
				v.findViewById(R.id.separator_big).setVisibility(childPosition == 0 ? View.VISIBLE : View.GONE);
				if (activeNavChild == childPosition && activeNavSection == groupPosition)
					((android.widget.TextView) v.findViewById(R.id.nav_title)).setTypeface(null, Typeface.BOLD);
				if (!isSignedIn() && ((groupPosition == 0 && childPosition == 3) || (groupPosition == 1 && childPosition == 1) || (groupPosition == 1 && childPosition == 2))) {
					((android.widget.TextView) v.findViewById(R.id.nav_title)).setTextColor(isDark ? Color.parseColor("#40FFFFFF") : Color.parseColor("#40000000"));
				}
			}

			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return data.get(groupPosition).items.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return data.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return data.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View v = inflater.inflate(isDark ? R.layout.navigation_sectiondark : R.layout.navigation_section, null);
			((TextView) v.findViewById(R.id.textView1)).setText(data.get(groupPosition).title);

			v.findViewById(R.id.imageView1).setVisibility(isExpanded ? View.GONE : View.VISIBLE);

			return v;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
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
		if (!isPlayingOnline)
			new ShowMenuAsync().execute();

	}

	class ShowMenuAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (m != null)
				m.getItem(0).setVisible(shouldRestartBeVisible);
			getSupportActionBar().setDisplayShowTitleEnabled(true);
			super.onPostExecute(result);
		}
	}

	private void showOpenedIcon() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (!isPlayingOnline) {
			shouldRestartBeVisible = m.getItem(0).isVisible();
			if (m != null) {
				m.getItem(0).setVisible(false);
			}
		}
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		m = menu;
		menu.add(R.string.restart).setIcon((isDark) ? R.drawable.ic_action_replaydark : R.drawable.ic_action_replay).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.getItem(0).setVisible(false);

		miPref = menu.add(R.string.menupref).setIcon((isDark) ? R.drawable.ic_action_prefdark : R.drawable.ic_action_pref);
		miPref.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		miStopOnline = menu.add(R.string.s52).setIcon((isDark) ? R.drawable.ic_action_av_stopdark : R.drawable.ic_action_av_stop);
		miStopOnline.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		miStopOnline.setVisible(false);

		miDeco = menu.add(R.string.s36).setVisible(shouldShowDeco);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}

		if (item.getTitle().toString().compareTo(getString(R.string.restart)) == 0) {
			if (activeNavSection == 0 && activeNavChild == 0)
				createNewGame();
			else if (activeNavSection == 0 && activeNavChild == 1) {

			} else {
				createNewGameAI();
			}
		}

		if (item.getTitle().toString().compareTo(getString(R.string.s52)) == 0) {
			if (isPlayingOnline)
				leftGameOnline(-1);
			if (isPlayingBluetooth) {
				stopService();
				startService();
			}
			return true;
		}

		if (isPlayingOnline) {
			Toast.makeText(getApplicationContext(), R.string.s48, Toast.LENGTH_SHORT).show();
		} else if (isPlayingBluetooth) {
			Toast.makeText(getApplicationContext(), R.string.s48, Toast.LENGTH_SHORT).show();
		} else {
			if (item.getTitle().toString().compareTo(getString(R.string.menupref)) == 0) {
				Intent prefIntent = new Intent(this, PreferencesActivity.class);
				startActivityForResult(prefIntent, REQUEST_PREF);
			}

			if (item.getTitle().toString().compareTo(getString(R.string.s36)) == 0) {
				signOutProcess();
				if (activeNavChild == 3 && activeNavSection == 0) {
					createNewGameAI();
					activeNavChild = 2;
					navAdapter.notifyDataSetChanged();

				}
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createNewGameAI() {
		userMightNotWantToLeave = false;
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);
		container.removeAllViews();
		container.addView(child);

		generateActionBarIcon();
		final View iconL = findViewById(R.id.gameViewForIcon);
		ViewTreeObserver vto = iconL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				generateActionBarIcon();
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
					iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();
		finishedAI = false;
		playerText = (LinearLayout) findViewById(R.id.playerText);
		playerText.setVisibility(View.INVISIBLE);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		gv.setDelegate(new GameHandler() {
			@Override
			public void handleTurn(int i, int j) {
				userMightNotWantToLeave = true;
				userMightNotWantToLeave = true;
				if (turn == BLUE_PLAYER) {
					tabVal[i][j] = BLUE_PLAYER;
					gv.setValues(tabVal, BLUE_PLAYER);
					gv.invalidate();
				} else {
					tabVal[i][j] = RED_PLAYER;
					gv.setValues(tabVal, BLUE_PLAYER);
					gv.invalidate();
				}
				if (m != null) {
					m.getItem(0).setVisible(true);
				}
				checkWinner(i, j, false, true, false);
				if (!finishedAI)
					makeTheAIPlay(i, j);
				generateActionBarIcon();
			}
		});

		if (nbGame % 2 != 0) {
			computerStarted = true;
			makeTheAIPlay(-1, -1);
			turn = BLUE_PLAYER;
		} else {
			computerStarted = false;
			turn = BLUE_PLAYER;
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

				if (tabVal[0][1] == BLUE_PLAYER && tabVal[1][2] == BLUE_PLAYER) {
					i = 1;
					j = 1;
				} else if (tabVal[0][1] == BLUE_PLAYER && tabVal[1][0] == BLUE_PLAYER && tabVal[1][1] == NONE_PLAYER) {
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
			checkWinner(i, j, false, true, false);
		}
	}

	private void createOnlineScreen() {
		View child = getLayoutInflater().inflate(isDark ? R.layout.onlinedark : R.layout.online, null);

		container.removeAllViews();
		container.addView(child);

		generateActionBarIcon();
		final View iconL = findViewById(R.id.gameViewForIcon);
		ViewTreeObserver vto = iconL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				generateActionBarIcon();
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
					iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		getSupportActionBar().setTitle(R.string.s44);
		View quick = child.findViewById(R.id.onlineQuickMatch);
		quick.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startQuickGame();
			}
		});

		View showInvit = child.findViewById(R.id.onlineshowInvit);
		showInvit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				progress = ProgressDialog.show(activContext, null, getString(R.string.s50), true);
				Intent intent = getGamesClient().getInvitationInboxIntent();
				startActivityForResult(intent, RC_INVITATION_INBOX);
				if (progress != null && progress.isShowing())
					progress.dismiss();
			}
		});

		View invite = child.findViewById(R.id.onlineAskInvit);
		invite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				progress = ProgressDialog.show(activContext, null, getString(R.string.s50), true);
				Intent intent = getGamesClient().getSelectPlayersIntent(1, 1);
				startActivityForResult(intent, RC_SELECT_PLAYERS);
				if (progress != null && progress.isShowing())
					progress.dismiss();
			}
		});

		View b1 = findViewById(R.id.imageViewPrems);
		View b2 = findViewById(R.id.imageView2);
		View b3 = findViewById(R.id.imageView3);
		b1.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));
		b2.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
		b3.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));

	}

	private void startQuickGame() {
		if (getGamesClient().isConnected()) {
			Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

			RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
			roomConfigBuilder.setAutoMatchCriteria(am);
			RoomConfig roomConfig = roomConfigBuilder.build();

			getGamesClient().createRoom(roomConfig);

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			progress = ProgressDialog.show(this, null, getString(R.string.s50), true);
		} else {

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
		miStopOnline.setVisible(false);
		firstIsPlayed = false;
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

			generateActionBarIcon();
			final View iconL = findViewById(R.id.gameViewForIcon);
			ViewTreeObserver vto = iconL.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@SuppressLint("NewApi")
				@Override
				public void onGlobalLayout() {
					generateActionBarIcon();
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
						iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					else
						iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			});

			activeNavSection = 0;
			activeNavChild = 1;
			getSupportActionBar().setTitle(navSections.get(activeNavSection).items.get(activeNavChild).title);
			navAdapter.notifyDataSetChanged();
			if (mDrawerLayout != null)
				mDrawerLayout.closeDrawer(GravityCompat.START);
			// Initialize the BluetoothChatService to perform bluetooth
			// connections
			mChatService = new BluetoothChatService(this, mHandler);
			mOutStringBuffer = new StringBuffer("");

			startService();

			Button btnBTVisible = (Button) findViewById(R.id.buttonBTVisible);
			btnBTVisible.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
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
			btnBTSearch.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
			btnBTSearch.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
				}
			});

			final Button btnBTCut = (Button) findViewById(R.id.buttonBTCut);
			btnBTCut.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
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

			final Button btnOnline = (Button) findViewById(R.id.buttonOnline);
			btnOnline.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));

			if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
				btnOnline.setVisibility(View.GONE);
				findViewById(R.id.tohidewhenno).setVisibility(View.GONE);
			}

			btnOnline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isSignedIn()) {
						createOnlineScreen();
						activeNavChild = 3;
						activeNavSection = 0;
						navAdapter.notifyDataSetChanged();
						miStopOnline.setVisible(false);
						isPlayingOnline = false;
					} else {
						Toast.makeText(getApplicationContext(), R.string.s57, Toast.LENGTH_SHORT).show();
					}
				}
			});

			btnOnline.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER), "FF")));
						break;
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER), "BB")));
						break;
					default:
						break;
					}
					return false;
				}
			});

			btnBTSearch.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER), "FF")));
						break;
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER), "BB")));
						break;
					default:
						break;
					}
					return false;
				}
			});

			btnBTVisible.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER), "FF")));
						break;
					case MotionEvent.ACTION_DOWN:
						v.setBackgroundColor(Color.parseColor(ColorHolder.addAlphaFormColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER), "BB")));
						break;
					default:
						break;
					}
					return false;
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
				if (!avoidNextBluetoothMessage)
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
		this.checkWinner(i, j, true, false, false);
	}

	private void createNewMuliGame() {
		isPlayingBluetooth = true;
		miStopOnline.setVisible(true);
		userMightNotWantToLeave = false;
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);

		container.removeAllViews();
		container.addView(child);

		generateActionBarIcon();
		final View iconL = findViewById(R.id.gameViewForIcon);
		ViewTreeObserver vto = iconL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				generateActionBarIcon();
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
					iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();

		playerText = (LinearLayout) findViewById(R.id.playerText);
		tabVal = new int[3][3];

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		turn = RED_PLAYER;

		displayNextTurn();
		turn = BLUE_PLAYER;
		playerText.setVisibility(View.INVISIBLE);

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
				userMightNotWantToLeave = true;
				userMightNotWantToLeave = true;
				if (!amILatestPlayerMulti) {
					String messageToSend = "";
					messageToSend = "newturn/" + i + "/" + j;
					sendMessage(messageToSend);
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_INSECURE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_PATERN:
			if (resultCode == Activity.RESULT_OK) {
				finish();
			}
			break;
		case REQUEST_BT:
			if (resultCode == Activity.RESULT_OK) {
				onItemClick(null, null, 1, 0);
			}

			break;
		case REQUEST_PREF:
			updateField();

			comeBackFromSettingsShouldSave = true;
			break;
		case ACTIVITY_HISTORY:
			comeBackFromSettingsShouldSave = true;
			comeBackFromHistoryShouldAchievement = true;

			if (resultCode == Activity.RESULT_OK) {
				int p1, p2;
				p1 = data.getIntExtra(ACTIVITY_HISTORY_RES_GROUP, -1);
				p2 = data.getIntExtra(ACTIVITY_HISTORY_RES_GROUP_CHILD, -1);

				if (p1 == 100 && p2 == 100) {
					finish();
				} else if ((p1 == 0 && p2 == 3) || (p1 == 1 && p2 == 1) || (p1 == 1 && p2 == 2)) {
					pendingAction = new int[] { p1, p2 };
				} else {
					onChildClick(null, null, p1, p2, 0);
				}
			}

			nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
			TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
			if (tv1 != null)
				tv1.setText(getString(R.string.game) + nbGame);

			break;

		case RC_INVITATION_INBOX:
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			Bundle extras = data.getExtras();
			Invitation invitation = extras.getParcelable(GamesClient.EXTRA_INVITATION);

			RoomConfig roomConfig = makeBasicRoomConfigBuilder().setInvitationIdToAccept(invitation.getInvitationId()).build();
			getGamesClient().joinRoom(roomConfig);

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			break;
		case RC_SELECT_PLAYERS:
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			if (isSignedIn())
				getGamesClient().unlockAchievement(getString(R.string.achivement_friendly));
			createGameAvoidDuplicate(data);

			break;
		case RC_WAITING_ROOM:
			if (resultCode == Activity.RESULT_OK) {
				isPlayingOnline = true;

				createOnlineGame(true);

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// Waiting room was dismissed with the back button. The meaning
				// of this
				// action is up to the game. You may choose to leave the room
				// and cancel the
				// match, or do something else like minimize the waiting room
				// and
				// continue to connect in the background.

				// in this example, we take the simple approach and just leave
				// the room:
				getGamesClient().leaveRoom(this, roomId);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				if (progress != null && progress.isShowing())
					progress.dismiss();
			} else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
				// player wants to leave the room.
				getGamesClient().leaveRoom(this, roomId);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				if (progress != null && progress.isShowing())
					progress.dismiss();
			}

			break;
		}
	}

	@SuppressLint("NewApi")
	private void createOnlineGame(boolean firstone) {


		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
		miStopOnline.setVisible(true);
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);

		container.removeAllViews();
		container.addView(child);

		generateActionBarIcon();
		final View iconL = findViewById(R.id.gameViewForIcon);
		ViewTreeObserver vto = iconL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				generateActionBarIcon();
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
					iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		nbGame = ToolsBDD.getInstance(this).getNbPartieNumber() + 1;
		TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
		tv1.setText(getString(R.string.game) + nbGame);

		gv = (GameView) findViewById(R.id.gameView1);
		gv.setValues(null, BLUE_PLAYER);
		gv.setMode(GameView.MODE_INTERACTIVE);
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_TOP_VERTICAL_CENTER_HORIZONTAL);
		gv.invalidate();

		playerText = (LinearLayout) findViewById(R.id.playerText);
		tabVal = new int[3][3];

		if (onlineHandler == null)
			onlineHandler = new MultiHandler();

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}
		if (firstone) {
			gv.setValues(tabVal, turn);
			new TurnFinder().execute();
		} else {
			displayNextTurn();
			displayNextTurn();
			gv.setDelegate(onlineHandler);
		}

	}

	class TurnFinder extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setSupportProgressBarIndeterminateVisibility(false);
			// getGamesClient().sendUnreliableRealTimeMessageToAll((CMD_DETERMINE_TURN
			// + "/" + getGamesClient().getCurrentPlayerId()).getBytes(),
			// roomId);

			sendUntilSucces((CMD_DETERMINE_TURN + "/" + getGamesClient().getCurrentPlayerId()).getBytes());
		}
	}

	private void sendUntilSucces(byte[] data) {
		byte[] f = data;
		for (Participant p : myroom.getParticipants()) {
			if (!p.getParticipantId().equals(myroom.getParticipantId(getGamesClient().getCurrentPlayerId()))) {
				getGamesClient().sendReliableRealTimeMessage(new MyRealTimeMessageListener(f), f, myroom.getRoomId(), p.getParticipantId());
			}
		}
	}

	class MyRealTimeMessageListener implements RealTimeReliableMessageSentListener {
		final byte[] f;

		public MyRealTimeMessageListener(byte[] data) {
			f = data;
		}

		@Override
		public void onRealTimeMessageSent(int arg0, int arg1, String arg2) {

			if (arg0 == GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED) {
				for (Participant p : myroom.getParticipants()) {
					if (!p.getParticipantId().equals(myroom.getParticipantId(getGamesClient().getCurrentPlayerId()))) {
						getGamesClient().sendReliableRealTimeMessage(new MyRealTimeMessageListener(f), f, myroom.getRoomId(), p.getParticipantId());
					}
				}
			}
		}

	}

	class MultiHandler implements GameHandler {

		@Override
		public void handleTurn(int i, int j) {
			userMightNotWantToLeave = true;
			miStopOnline.setVisible(true);
			if (turn == BLUE_PLAYER) {
				tabVal[i][j] = BLUE_PLAYER;
				displayNextTurn();
				gv.setValues(tabVal, turn);
				// getGamesClient().sendUnreliableRealTimeMessageToAll((CMD_PLAY
				// + "/" + (i + "") + (j + "")).getBytes(), roomId);

				sendUntilSucces((CMD_PLAY + "/" + (i + "") + (j + "")).getBytes());

				setSupportProgressBarIndeterminateVisibility(true);
			} else if (turn == RED_PLAYER) {

			}

			checkWinner(i, j, false, false, true);
		}

	}

	@Override
	public void onRealTimeMessageReceived(RealTimeMessage rtm) {
		byte[] b = rtm.getMessageData();
		String s = new String(b);
		if (s.startsWith(CMD_DETERMINE_TURN)) {
			String otherplayerId = s.split("/")[1];
			if (getGamesClient().getCurrentPlayerId().compareTo(otherplayerId) < 0)
				turn = BLUE_PLAYER;
			else {
				turn = RED_PLAYER;
				setSupportProgressBarIndeterminateVisibility(true);
			}
			displayNextTurn();
			displayNextTurn();

			gv.setDelegate(onlineHandler);
		} else if (s.startsWith(CMD_PLAY)) {
			String options = s.split("/")[1];
			int i = Integer.parseInt(options.substring(0, 1));
			int j = Integer.parseInt(options.substring(1, 2));
			tabVal[i][j] = RED_PLAYER;
			turn = RED_PLAYER;
			displayNextTurn();
			gv.setValues(tabVal, turn);
			checkWinner(i, j, false, false, true);
			setSupportProgressBarIndeterminateVisibility(false);
			miStopOnline.setVisible(true);
		}

	}

	public void createGameAvoidDuplicate(Intent data) {
		// get the invitee list

		final ArrayList<String> invitees = data.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);

		// get automatch criteria
		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = data.getIntExtra(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

		if (minAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
		} else {
			autoMatchCriteria = null;
		}

		// create the room and specify a variant if appropriate
		RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
		roomConfigBuilder.addPlayersToInvite(invitees);
		if (autoMatchCriteria != null) {
			roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		}
		RoomConfig roomConfig = roomConfigBuilder.build();
		getGamesClient().createRoom(roomConfig);

		// prevent screen from sleeping during handshake
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

		playerText.setVisibility(View.VISIBLE);
		if (turn == RED_PLAYER) {
			turn = BLUE_PLAYER;
			playerText.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(this).getColor(BLUE_PLAYER)));
		} else {
			turn = RED_PLAYER;
			playerText.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(this).getColor(RED_PLAYER)));
		}
		playerText.invalidate();
	}

	@SuppressLint("NewApi")
	public void createNewGame() {
		userMightNotWantToLeave = false;
		View child = getLayoutInflater().inflate(isDark ? R.layout.game_aidark : R.layout.game_ai, null);
		container.removeAllViews();
		container.addView(child);
		init();
		generateActionBarIcon();
		final View iconL = findViewById(R.id.gameViewForIcon);
		ViewTreeObserver vto = iconL.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				generateActionBarIcon();
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
					iconL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				else
					iconL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

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

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabVal[i][j] = NONE_PLAYER;
			}
		}

		gv.setDelegate(new GameHandler() {
			@Override
			public void handleTurn(int i, int j) {
				userMightNotWantToLeave = true;
				if (turn == BLUE_PLAYER) {
					tabVal[i][j] = BLUE_PLAYER;
					displayNextTurn();
					gv.setValues(tabVal, turn);
				} else {
					tabVal[i][j] = RED_PLAYER;
					displayNextTurn();
					gv.setValues(tabVal, turn);
				}
				if (m != null && !(activeNavSection == 0 && activeNavChild == 1)) {
					m.getItem(0).setVisible(true);
				}
				checkWinner(i, j, false, false, false);

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

		if (findViewById(R.id.gameViewForIcon) != null) {
			generateActionBarIcon();
		}

		View b1 = findViewById(R.id.imageViewPrems);
		View b2 = findViewById(R.id.imageView2);
		View b3 = findViewById(R.id.imageView3);
		if (b1 != null && b2 != null && b3 != null) {
			b1.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));
			b2.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
			b3.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
		}

		congratsContainer = findViewById(R.id.congratsContainer);
		if (congratsContainer != null) {
			if (winnerForUpdateField == BLUE_PLAYER) {
				congratsContainer.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(BLUE_PLAYER)));
			} else if (winnerForUpdateField == RED_PLAYER) {
				congratsContainer.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(RED_PLAYER)));
			} else if (winnerForUpdateField == NONE_PLAYER) {
				congratsContainer.setBackgroundColor(isDark ? Color.DKGRAY : Color.LTGRAY);
			}
		}

		if (playerText != null)
			playerText.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(this).getColor(turn)));

		Button btnBTVisible = (Button) findViewById(R.id.buttonBTVisible);
		if (btnBTVisible != null)
			btnBTVisible.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
		Button btnBTSearch = (Button) findViewById(R.id.buttonBTSearch);
		if (btnBTSearch != null)
			btnBTSearch.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
		final Button btnBTCut = (Button) findViewById(R.id.buttonBTCut);
		if (btnBTCut != null)
			btnBTCut.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
		final Button btnOnline = (Button) findViewById(R.id.buttonOnline);
		if (btnOnline != null)
			btnOnline.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));
	}

	public void signOutProcess() {
		signOut();

		navAdapter.notifyDataSetChanged();

		// show sign-in button, hide the sign-out button
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		if (miDeco != null)
			miDeco.setVisible(false);
	}

	public void onClick(View view) {

		if (view.getId() == R.id.sign_in_button) {
			// start the asynchronous sign in flow
			beginUserInitiatedSignIn();
		} 
	}

	private void checkWinner(int i, int j, boolean fromBT, boolean fromMulti, boolean fromOnline) {
		if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti, fromOnline);
		else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != NONE_PLAYER)
			congratsWinner(tabVal[1][0], fromBT, fromMulti, fromOnline);
		else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT, fromMulti, fromOnline);
		else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti, fromOnline);
		else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != NONE_PLAYER)
			congratsWinner(tabVal[0][1], fromBT, fromMulti, fromOnline);
		else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][2], fromBT, fromMulti, fromOnline);
		else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != NONE_PLAYER)
			congratsWinner(tabVal[0][0], fromBT, fromMulti, fromOnline);
		else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != NONE_PLAYER)
			congratsWinner(tabVal[2][0], fromBT, fromMulti, fromOnline);
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
				congratsWinner(NONE_PLAYER, fromBT, fromMulti, fromOnline);
			}
		}
	}

	private void congratsWinner(int winner, final boolean fromBT, final boolean fromMulti, final boolean fromOnline) {
		winnerForUpdateField = winner;
		oneGameHasBeenPlayedWeCanSave = true;
		finishedAI = true;
		userMightNotWantToLeave = false;

		if (isSignedIn()) {
			if (tabVal[0][0] == RED_PLAYER && tabVal[0][1] == RED_PLAYER && tabVal[0][2] == BLUE_PLAYER && tabVal[1][0] == BLUE_PLAYER && tabVal[1][1] == BLUE_PLAYER && tabVal[1][2] == RED_PLAYER && tabVal[2][0] == RED_PLAYER && tabVal[2][1] == BLUE_PLAYER && tabVal[2][2] == RED_PLAYER) {
				getGamesClient().unlockAchievement(getString(R.string.achievement_fan));
			}
			getGamesClient().unlockAchievement(getString(R.string.achivement_multiplayer));
		}
		if (isSignedIn()) {
			getGamesClient().incrementAchievement(getString(R.string.achievement_bored), 1);
			getGamesClient().incrementAchievement(getString(R.string.achievement_veteran), 1);
		}
		if (isSignedIn() && fromOnline) {
			getGamesClient().incrementAchievement(getString(R.string.achievement_welltrained), 1);
		}
		nbGame++;
		if (!fromBT) {
			playerText.setVisibility(View.INVISIBLE);
		}

		gv.setMode(GameView.MODE_NOT_INTERACTIVE);
		gv.setShowWinner(true);

		congratsContainer = findViewById(R.id.congratsContainer);
		congratsContainer.setVisibility(View.VISIBLE);

		AlphaAnimation alpha = new AlphaAnimation(0.0F, 0.0F);
		alpha.setDuration(0);
		alpha.setFillAfter(true);
		congratsContainer.startAnimation(alpha);

		alpha = new AlphaAnimation(0.0F, 1.0F);
		alpha.setDuration(600);
		alpha.setFillAfter(true);
		congratsContainer.startAnimation(alpha);

		TextView tvCongrats = (TextView) findViewById(R.id.resultText2);

		if (fromOnline) {
			View retry = findViewById(R.id.congratsRetry);
			retry.setVisibility(View.GONE);
			retrycount = (TextView) findViewById(R.id.congratsRetryCount);
			retrycount.setVisibility(View.VISIBLE);
			new OnlineWaiterCount().execute();
		} else {
			View retry = findViewById(R.id.congratsRetry);
			retry.setVisibility(View.VISIBLE);
			View retrycount = findViewById(R.id.congratsRetryCount);
			retrycount.setVisibility(View.GONE);

			retry.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (fromBT) {
						if (isSignedIn()) {
							getGamesClient().incrementAchievement(getString(R.string.achievement_welltrained), 1);
						}
					} else if (fromMulti) {
						if (isSignedIn()) {
							getGamesClient().incrementAchievement(getString(R.string.achievement_sadomasochistic), 1);
						}
						createNewGameAI();
					} else {
						createNewGame();
						if (isSignedIn())
							getGamesClient().incrementAchievement(getString(R.string.achievement_welltrained), 1);
					}
				}
			});
		}

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
			if (fromBT)
				Toast.makeText(getApplicationContext(), R.string.winb, Toast.LENGTH_LONG).show();
			tvCongrats.setText(R.string.winb);
			congratsContainer.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(BLUE_PLAYER)));
			ToolsBDD.getInstance(this).insertPartie(BLUE_PLAYER, values);
			if (isSignedIn()) {
				getGamesClient().unlockAchievement(getString(R.string.achievement_firstvictort));
			}
		} else if (winner == RED_PLAYER) {
			if (fromBT)
				Toast.makeText(getApplicationContext(), R.string.winr, Toast.LENGTH_LONG).show();
			tvCongrats.setText(R.string.winr);
			congratsContainer.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(RED_PLAYER)));
			ToolsBDD.getInstance(this).insertPartie(RED_PLAYER, values);
		} else if (winner == NONE_PLAYER) {
			if (fromBT)
				Toast.makeText(getApplicationContext(), R.string.equaltry, Toast.LENGTH_LONG).show();
			tvCongrats.setText(R.string.equaltry);
			congratsContainer.setBackgroundColor(isDark ? Color.DKGRAY : Color.LTGRAY);

			if (save) {
				ToolsBDD.getInstance(this).insertPartie(NONE_PLAYER, values);
			}
		}

		if (isSignedIn()) {
			getGamesClient().submitScore(getString(R.string.leaderboard_most_active), ToolsBDD.getInstance(getApplicationContext()).getNbPartieNumber());
		}
	}

	class OnlineWaiterCount extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 5; i > 0; i--) {
				try {
					publishProgress((i) + "");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			retrycount.setText(values[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (isPlayingOnline)
				createOnlineGame(false);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopService();
		isPlayingBluetooth = false;
	}

	@Override
	protected void onPause() {
		if (oneGameHasBeenPlayedWeCanSave)
			saveState();
		super.onPause();

		StateHolder.MemorizeValue(STATE_SECTION, activeNavSection, getApplicationContext());
		StateHolder.MemorizeValue(STATE_CHILD, activeNavChild, getApplicationContext());
		StateHolder.MemorizeValue(STATE_ACTIVE, true, getApplicationContext());
	}

	public void saveState() {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		byte[] my_app_state = new byte[128];

		if (mgr.getBoolean("save", true))
			my_app_state[0] = 1;
		else
			my_app_state[0] = 0;

		if (mgr.getBoolean("isDark", false))
			my_app_state[1] = 1;
		else
			my_app_state[1] = 0;

		my_app_state[2] = (byte) mgr.getInt("colorblue", 0);
		my_app_state[3] = (byte) mgr.getInt("colorred", 0);

		String bValue = Integer.toString(ToolsBDD.getInstance(getApplicationContext()).getNbPartieNumber(), 2);
		if (bValue.length() > 8) {
			my_app_state[4] = (byte) (int) Integer.valueOf(bValue.substring(bValue.length() - 8), 2);
			my_app_state[5] = (byte) (int) Integer.valueOf(bValue.substring(0, bValue.length() - 8), 2);
		} else {
			my_app_state[4] = (byte) (int) Integer.valueOf(bValue, 2);
		}

		if (getGamesClient().isConnected()) {
			getAppStateClient().updateState(SAVE_PREF, my_app_state);
		}

		byte[] hist1 = new byte[128];
		byte[] hist2 = new byte[128];
		byte[] hist3 = new byte[128];

		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			c.close();
		} else {
			c.moveToFirst();
			String futurByte = "";
			int currentByteTab = 0;
			int currentTabIndex = 0;
			int currentGameNum = 0;
			for (int i = 0; i < c.getCount(); i++) {
				int newGameNum = c.getInt(0);
				if (newGameNum - currentGameNum > 1) {
					int missingGames = newGameNum - currentGameNum;
					missingGames--;
					for (int m = 0; m < missingGames; m++) {
						futurByte += SAVE_DELETED;
						if (futurByte.length() == 8) {
							byte b = (byte) (int) Integer.valueOf(futurByte, 2);
							if (currentByteTab == 0)
								hist1[currentTabIndex] = b;
							if (currentByteTab == 1)
								hist2[currentTabIndex] = b;
							if (currentByteTab == 2)
								hist3[currentTabIndex] = b;
							currentTabIndex++;
							if (currentTabIndex == 128) {
								currentTabIndex = 0;
								currentByteTab++;
							}
							futurByte = "";
						}
					}
				}
				currentGameNum = newGameNum;

				int n = c.getInt(1);
				if (n == MainActivity.BLUE_PLAYER) {
					futurByte += SAVE_BLUE_WIN;
				} else if (n == MainActivity.RED_PLAYER) {
					futurByte += SAVE_RED_WIN;
				} else {
					futurByte += SAVE_TIE;
				}

				if (futurByte.length() == 8) {
					byte b = (byte) (int) Integer.valueOf(futurByte, 2);
					if (currentByteTab == 0)
						hist1[currentTabIndex] = b;
					if (currentByteTab == 1)
						hist2[currentTabIndex] = b;
					if (currentByteTab == 2)
						hist3[currentTabIndex] = b;
					currentTabIndex++;
					if (currentTabIndex == 128) {
						currentTabIndex = 0;
						currentByteTab++;
					}
					futurByte = "";
				}

				c.moveToNext();
			}
			c.close();

			while (futurByte.length() < 8) {
				futurByte += "0";
			}

			if (futurByte.length() == 8) {
				byte b = (byte) (int) Integer.valueOf(futurByte, 2);
				if (currentByteTab == 0)
					hist1[currentTabIndex] = b;
				if (currentByteTab == 1)
					hist2[currentTabIndex] = b;
				if (currentByteTab == 2)
					hist3[currentTabIndex] = b;
				currentTabIndex++;
				if (currentTabIndex == 128) {
					currentTabIndex = 0;
					currentByteTab++;
				}
				futurByte = "";
			}

		}

		if (getGamesClient().isConnected())
			getAppStateClient().updateState(SAVE_HIST_1, hist1);
		if (getGamesClient().isConnected())
			getAppStateClient().updateState(SAVE_HIST_2, hist2);
		if (getGamesClient().isConnected())
			getAppStateClient().updateState(SAVE_HIST_3, hist3);

	}

	@SuppressWarnings("unchecked")
	private void restoreState(ArrayList<byte[]> dataSaved2) {
		if (totalGameToRestore != -1) {
			new RestoreTask().execute(dataSaved2);
		}
	}

	class RestoreTask extends AsyncTask<List<byte[]>, Void, Void> {
		@Override
		protected Void doInBackground(List<byte[]>... params) {
			int totalTreater = 0;

			byte[] data = new byte[128 * 3];
			int n = 0;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < params[0].get(i).length; j++) {
					data[n] = params[0].get(i)[j];
					n++;
				}
			}

			ToolsBDD.getInstance(getApplicationContext()).getBDD().beginTransaction();
			for (int i = 0; i < data.length && totalTreater < totalGameToRestore; i++) {
				String d = getWellFormedBytesAsString("" + Integer.toBinaryString((data[i] + 256) % 256));
				for (int di = 0; di < 4 && totalTreater < totalGameToRestore;) {

					String code = d.substring(di * 2, (di * 2) + 2);
					if (ToolsBDD.getInstance(getApplicationContext()).getResultat((totalTreater + 1)).compareTo("vide") == 0) {

						if (code.compareTo(SAVE_BLUE_WIN) == 0) {
							ToolsBDD.getInstance(getApplicationContext()).insertPartie((totalTreater + 1), BLUE_PLAYER, BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER + "," + BLUE_PLAYER);
						}
						if (code.compareTo(SAVE_RED_WIN) == 0) {
							ToolsBDD.getInstance(getApplicationContext()).insertPartie((totalTreater + 1), RED_PLAYER, RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER);
						}
						if (code.compareTo(SAVE_TIE) == 0) {
							ToolsBDD.getInstance(getApplicationContext()).insertPartie((totalTreater + 1), NONE_PLAYER, BLUE_PLAYER + "," + BLUE_PLAYER + "," + NONE_PLAYER + "," + BLUE_PLAYER + "," + NONE_PLAYER + "," + RED_PLAYER + "," + NONE_PLAYER + "," + RED_PLAYER + "," + RED_PLAYER);
						}
					}
					if (code.compareTo(SAVE_DELETED) == 0) {
						ToolsBDD.getInstance(getApplicationContext()).removePartie((totalTreater + 1));
					}

					totalTreater++;
					di++;
				}

			}
			ToolsBDD.getInstance(activContext).getBDD().setTransactionSuccessful();
			ToolsBDD.getInstance(getApplicationContext()).getBDD().endTransaction();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			nbGame = ToolsBDD.getInstance(activContext).getNbPartieNumber() + 1;
			TextView tv1 = (TextView) findViewById(R.id.welcomeGame);
			if (tv1 != null)
				tv1.setText(getString(R.string.game) + nbGame);
		}

	}

	public String getWellFormedBytesAsString(String value) {
		String res = value;
		if (res.length() > 8) {
			res = res.substring(res.length() - 8);
		} else if (res.length() < 8) {
			while (res.length() < 8) {
				res = "0" + res;
			}
		}
		return res;
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
		if (mChatService != null) {
			mChatService.stop();
			avoidNextBluetoothMessage = true;
		}

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

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		}
		shouldShowDeco = false;
		if (miDeco != null)
			miDeco.setVisible(false);
		navAdapter.notifyDataSetChanged();

	}

	private RoomConfig.Builder makeBasicRoomConfigBuilder() {
		return RoomConfig.builder(this).setMessageReceivedListener(this).setRoomStatusUpdateListener(this);
	}

	@Override
	public void onSignInSucceeded() {
		shouldShowDeco = true;
		// show sign-out button, hide the sign-in button
		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		if (miDeco != null)
			miDeco.setVisible(true);
		// (your code here: update UI, enable functionality that depends on sign
		// in, etc)
		if (comeBackFromSettingsShouldSave) {
			saveState();
			comeBackFromSettingsShouldSave = false;
		} else if (firstStartShouldReloadConfig) {
			if (getGamesClient().isConnected())
				getAppStateClient().loadState(this, SAVE_PREF);
			if (getGamesClient().isConnected())
				getAppStateClient().loadState(this, SAVE_HIST_1);
			if (getGamesClient().isConnected())
				getAppStateClient().loadState(this, SAVE_HIST_2);
			if (getGamesClient().isConnected())
				getAppStateClient().loadState(this, SAVE_HIST_3);
			firstStartShouldReloadConfig = false;
		}
		if (comeBackFromHistoryShouldAchievement) {
			if (isSignedIn())
				getGamesClient().unlockAchievement(getString(R.string.achievement_history));
		}
		comeBackFromHistoryShouldAchievement = false;

		if (getGamesClient().isConnected())
			getGamesClient().registerInvitationListener(this);

		if (getGamesClient().isConnected())
			if (getInvitationId() != null) {
				RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
				roomConfigBuilder.setInvitationIdToAccept(getInvitationId());
				getGamesClient().joinRoom(roomConfigBuilder.build());

				// prevent screen from sleeping during handshake
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

				// go to game screen
			}
		navAdapter.notifyDataSetChanged();

		if (pendingAction != null) {
			onChildClick(null, null, pendingAction[0], pendingAction[1], 0);
			pendingAction = null;
		}
		if (HistoryActivity.navAdapter != null) {
			HistoryActivity.isSignedIn = true;
			HistoryActivity.navAdapter.notifyDataSetChanged();
		}

		if (VisuPagerActivity.navAdapter != null) {
			VisuPagerActivity.isSignedIn = true;
			VisuPagerActivity.navAdapter.notifyDataSetChanged();
		}
	}

	ArrayList<byte[]> dataSaved;
	short totalGameToRestore = -1;
	private String mIncomingInvitationId;

	@Override
	public void onStateLoaded(int statusCode, int stateKey, byte[] data) {
		if (stateKey == 0) {
			if (statusCode == AppStateClient.STATUS_OK) {
				SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);

				SharedPreferences.Editor editor = mgr.edit();
				editor.putBoolean("save", data[0] == 1);
				editor.commit();

				if (isDark != (data[1] == 1)) {
					Toast.makeText(getApplicationContext(), R.string.s43, Toast.LENGTH_SHORT).show();
				}

				editor.putBoolean("isDark", data[1] == 1);
				editor.commit();

				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.RED_PLAYER, data[3]);
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.BLUE_PLAYER, data[2]);

				totalGameToRestore = (short) (((data[5] & 0xFF) << 8) | (data[4] & 0xFF));

				updateField();
			} else if (statusCode == AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA) {
				SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);

				SharedPreferences.Editor editor = mgr.edit();
				editor.putBoolean("save", data[0] == 1);
				editor.commit();

				if (isDark != (data[1] == 1)) {
					Toast.makeText(getApplicationContext(), R.string.s43, Toast.LENGTH_SHORT).show();
				}
				editor.putBoolean("isDark", data[1] == 1);
				editor.commit();

				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.RED_PLAYER, data[3]);
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.BLUE_PLAYER, data[2]);

				totalGameToRestore = (short) (((data[5] & 0xFF) << 8) | (data[4] & 0xFF));

				updateField();
			} else {

			}
		}
		if (stateKey == 1) {
			dataSaved = new ArrayList<byte[]>();
			dataSaved.add(data);
		}
		if (stateKey == 2) {
			if (dataSaved != null)
				dataSaved.add(data);
		}
		if (stateKey == 3) {
			if (dataSaved != null) {
				dataSaved.add(data);
				if (dataSaved.size() == 3) {
					restoreState(dataSaved);
				}
			}
		}
	}

	@Override
	public void onStateConflict(int stateKey, String ver, byte[] localData, byte[] serverData) {
		getAppStateClient().resolveState(this, stateKey, ver, serverData);
	}

	@Override
	public void onLeftRoom(int arg0, String arg1) {

	}

	@Override
	public void onRoomCreated(int statusCode, Room room) {
		if (statusCode != GamesClient.STATUS_OK) {
			// let screen go to sleep
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.
			if (progress != null && progress.isShowing())
				progress.dismiss();
		} else {
			roomId = room.getRoomId();
			myroom = room;
			Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
			startActivityForResult(i, RC_WAITING_ROOM);
		}

	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		if (statusCode != GamesClient.STATUS_OK) {
			// let screen go to sleep
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.
		} else {
			roomId = room.getRoomId();
			myroom = room;
			Intent i = getGamesClient().getRealTimeWaitingRoomIntent(room, Integer.MAX_VALUE);
			startActivityForResult(i, RC_WAITING_ROOM);
		}
	}

	@Override
	public void onRoomConnected(int statusCode, Room room) {
		if (statusCode != GamesClient.STATUS_OK) {
			// let screen go to sleep
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			// show error message, return to main screen.

		} else {
			myroom = room;
			roomId = room.getRoomId();
		}
	}

	boolean mPlaying = false;

	final static int MIN_PLAYERS = 2;

	boolean shouldStartGame(Room room) {
		int connectedPlayers = 0;
		for (Participant p : room.getParticipants()) {
			if (p.isConnectedToRoom())
				++connectedPlayers;
		}
		return connectedPlayers >= MIN_PLAYERS;
	}

	boolean shouldCancelGame(Room room) {

		return shouldStartGame(room);
	}

	@Override
	public void onPeersConnected(Room room, List<String> peers) {
		if (mPlaying) {

		} else if (shouldStartGame(room)) {

		}
		myroom = room;
		roomId = room.getRoomId();
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> peers) {
		myroom = room;
		roomId = room.getRoomId();
		if (shouldCancelGame(room)) {
			// cancel the game
			getGamesClient().leaveRoom(this, room.getRoomId());
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			if (progress != null && progress.isShowing())
				progress.dismiss();
			leftGameOnline(-1);
		}
	}

	@Override
	public void onPeerLeft(Room room, List<String> peers) {
		// peer left -- see if game should be cancelled
		if (!mPlaying && shouldCancelGame(room)) {
			leftGameOnline(R.string.s51);
		}
	}

	public void leftGameOnline(int s) {
		getGamesClient().leaveRoom(this, roomId);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (s > 0)
			Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
		createOnlineScreen();
		activeNavChild = 0;
		activeNavSection = 3;
		navAdapter.notifyDataSetChanged();
		miStopOnline.setVisible(false);
		isPlayingOnline = false;
		if (progress != null && progress.isShowing())
			progress.dismiss();
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onPeerDeclined(Room room, List<String> peers) {
		// peer declined invitation -- see if game should be cancelled
		if (!mPlaying && shouldCancelGame(room)) {
			getGamesClient().leaveRoom(this, room.getRoomId());
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			leftGameOnline(-1);
		}
	}

	@Override
	public void onConnectedToRoom(Room arg0) {
		myroom = arg0;
		roomId = arg0.getRoomId();
	}

	@Override
	public void onDisconnectedFromRoom(Room room) {

		getGamesClient().leaveRoom(this, room.getRoomId());

		// clear the flag that keeps the screen on
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	}

	@Override
	public void onPeerInvitedToRoom(Room arg0, List<String> arg1) {

	}

	@Override
	public void onPeerJoined(Room arg0, List<String> arg1) {

	}

	@Override
	public void onRoomAutoMatching(Room arg0) {

	}

	@Override
	public void onRoomConnecting(Room arg0) {

	}

	@Override
	public void onInvitationReceived(Invitation invitation) {
		mIncomingInvitationId = invitation.getInvitationId();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.s44);
		builder.setMessage(R.string.s45);
		builder.setPositiveButton(R.string.s46, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
				roomConfigBuilder.setInvitationIdToAccept(mIncomingInvitationId);
				getGamesClient().joinRoom(roomConfigBuilder.build());

				// prevent screen from sleeping during handshake
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

				dialog.dismiss();
				createOnlineGame(true);

			}

		});

		builder.setNegativeButton(R.string.s47, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onP2PConnected(String arg0) {

	}

	@Override
	public void onP2PDisconnected(String arg0) {

	}

}
