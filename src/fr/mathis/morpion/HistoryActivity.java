package fr.mathis.morpion;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieSlice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.michaelpardo.android.widget.chartview.ChartView;
import com.michaelpardo.android.widget.chartview.LinearSeries;
import com.michaelpardo.android.widget.chartview.LinearSeries.LinearPoint;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import de.timroes.swipetodismiss.SwipeDismissList;
import de.timroes.swipetodismiss.SwipeDismissList.UndoMode;
import fr.mathis.morpion.fragments.RightFillerFragment;
import fr.mathis.morpion.fragments.VisuFragment;
import fr.mathis.morpion.interfaces.HoverHandler;
import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.tools.UndoBarController;
import fr.mathis.morpion.tools.UndoBarController.UndoListener;
import fr.mathis.morpion.views.GameView;

public class HistoryActivity extends SherlockFragmentActivity implements OnItemLongClickListener, OnItemClickListener, OnNavigationListener, UndoListener, HoverHandler, OnChildClickListener {

	public static final int RESULT_CODE_DETAIL = 0;
	static final int MENU_RESET = 0;
	static final int MENU_SHARE = 2;
	private static int currentId;
	ArrayList<HashMap<String, String>> listItem;
	private GridView lv;
	Button visu;
	Button effacer;
	Dialog dialog;
	String share;
	MyAdapter mSchedule;
	ActionMode mActionMode;
	ChartView chartView;
	View statContainer;
	private UndoBarController mUndoBarController;
	public boolean isDark;
	Timer timer;
	int displayWidth = 2000;
	ArrayList<Integer> items;
	PopupWindow popoup;
	FrameLayout rightContainer;
	SherlockFragment lastFragment;
	SwipeDismissList swipeList;
	private ActionBarDrawerToggle mDrawerToggle;
	ArrayList<NavigationSection> navSections;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	NavigationAdapter navAdapter;
	Menu m;
	boolean isSignedIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDark);
		super.onCreate(savedInstanceState);

		Context context = getSupportActionBar().getThemedContext();

		ArrayList<AbMenu> data = new ArrayList<AbMenu>();
		data.add(new AbMenu(isDark ? R.drawable.ic_action_spinner_listdark : R.drawable.ic_action_spinner_list, getString(R.string.m4), 1));
		data.add(new AbMenu(isDark ? R.drawable.ic_action_spinner_chartdark : R.drawable.ic_action_spinner_chart, getString(R.string.m6), 3));

		AbMenuAdapter adapter = new AbMenuAdapter(context, R.layout.ab_spinner_item, data);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(adapter, this);

		setContentView(isDark ? R.layout.listviewcustomdark : R.layout.listviewcustom);

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

		if (findViewById(R.id.container_right) != null)
			rightContainer = (FrameLayout) findViewById(R.id.container_right);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		displayWidth = metrics.widthPixels;

		if (rightContainer != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			lastFragment = RightFillerFragment.newInstance();
			ft.replace(R.id.container_right, lastFragment);
			ft.commit();
			if (statContainer != null)
				statContainer.setVisibility(View.GONE);
			rightContainer.setVisibility(View.VISIBLE);
		}

		isSignedIn = getIntent().getBooleanExtra("isSigned", false);

		if (findViewById(R.id.drawer_layout) != null)
			initDrawer();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	private void initDrawer() {
		if (findViewById(R.id.drawer_layout) != null) {
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		}
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawerlist);

		if (findViewById(R.id.sign_in_button) != null)
			findViewById(R.id.sign_in_button).setVisibility(View.GONE);

		navSections = new ArrayList<NavigationSection>();

		ArrayList<NavigationItem> n1 = new ArrayList<NavigationItem>();
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

		ArrayList<NavigationItem> n2 = new ArrayList<NavigationItem>();
		n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_savedark : R.drawable.ic_action_spinner_save, getString(R.string.m2), 0));
		// n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_partiehelpdark : R.drawable.ic_action_spinner_partiehelp, getString(R.string.m3), 0));
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
			n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_achivmentdark : R.drawable.ic_action_spinner_achivment, getString(R.string.s37), 0));
			n2.add(new NavigationItem(isDark ? R.drawable.ic_action_spinner_boarddarl : R.drawable.ic_action_spinner_board, getString(R.string.s38), 0));
		}

		NavigationSection s2 = new NavigationSection(getString(R.string.s40), n2);
		navSections.add(s2);

		navAdapter = new NavigationAdapter(this, navSections);
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
					supportInvalidateOptionsMenu();
					showClosedIcon();
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
				if (0 == childPosition && 1 == groupPosition)
					((android.widget.TextView) v.findViewById(R.id.nav_title)).setTypeface(null, Typeface.BOLD);
				if (!isSignedIn && ((groupPosition == 0 && childPosition == 3) || (groupPosition == 1 && childPosition == 1) || (groupPosition == 1 && childPosition == 2))) {
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

	private void showClosedIcon() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		new ShowMenuAsync().execute();
	}

	private void showOpenedIcon() {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (m != null) {
			new HideMenuAsync().execute();
		}

	}

	class ShowMenuAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (m != null)
				m.getItem(0).setVisible(true);
			super.onPostExecute(result);
		}
	}

	class HideMenuAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (m != null)
				m.getItem(0).setVisible(false);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			super.onPostExecute(result);
		}
	}

	private void generateActionBarIcon() {

		GameView vGame = (GameView) findViewById(R.id.gameViewForIcon);
		int[][] values = new int[][] { new int[] { MainActivity.RED_PLAYER, MainActivity.RED_PLAYER, MainActivity.BLUE_PLAYER }, new int[] { MainActivity.BLUE_PLAYER, MainActivity.BLUE_PLAYER, MainActivity.RED_PLAYER }, new int[] { MainActivity.RED_PLAYER, MainActivity.BLUE_PLAYER, MainActivity.RED_PLAYER } };
		vGame.setValues(values, MainActivity.BLUE_PLAYER);
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

	public void createList() {
		initViews();

		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			c.close();
			share = getString(R.string.sharetry);
		} else {
			c.close();
			reloadItems();
		}
		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);

		if (listItem.size() > 100)
			lv.setFastScrollEnabled(true);

		setSwypeListener();
	}

	public void reloadItems() {
		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();

		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			share = getString(R.string.sharetry);
			c.close();
		} else {

			share = getString(R.string.app_name) + " - https://play.google.com/store/apps/details?id=fr.mathis.morpion - ";
			int win = 0;
			int lost = 0;
			int equal = 0;
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				int n = c.getInt(1);
				if (n == MainActivity.BLUE_PLAYER) {
					map = new HashMap<String, String>();
					map.put("titre", "" + getString(R.string.win));
					win++;
				} else if (n == MainActivity.RED_PLAYER) {
					map = new HashMap<String, String>();
					map.put("titre", "" + getString(R.string.loose));
					lost++;
				} else {
					map = new HashMap<String, String>();
					map.put("titre", "" + getString(R.string.equal));
					equal++;
				}
				map.put("num", "N°" + c.getInt(0));
				map.put("description", getString(R.string.s34).replace(":win", win + "").replace(":loose", lost + "").replace(":tie", equal + ""));
				map.put("winner", n + "");
				map.put("disposition", c.getString(2));
				listItem.add(map);
				c.moveToNext();
			}
			c.close();
			share += (win + lost + equal) + " " + getString(R.string.share1);
			share += " " + win + " " + getString(R.string.share2);
			share += " " + lost + " " + getString(R.string.share3);
		}

		mSchedule = new MyAdapter(this.getBaseContext(), listItem, isDark ? R.layout.itemlistviewcustomdark : R.layout.itemlistviewcustom, new String[] { "titre", "description", "num" }, new int[] { R.id.titre, R.id.description, R.id.num });
		lv.setAdapter(mSchedule);
	}

	private void setSwypeListener() {

		if (swipeList == null) {
			SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
				public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
					mSchedule.remove(listItem.get(position));
					mSchedule.notifyDataSetChanged();
					return null;
				}
			};

			swipeList = new SwipeDismissList(lv, callback, UndoMode.SINGLE_UNDO);
		} else
			swipeList.setSwipeDisabled(false);
	}

	private void removeSwypeListener() {
		swipeList.setSwipeDisabled(true);
		// lv.setOnTouchListener(null);
		// lv.setOnScrollListener(null);
	}

	@Override
	protected void onStop() {
		if (swipeList != null)
			swipeList.discardUndo();
		super.onStop();
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}

		if (item.getTitle().toString().compareTo(getString(R.string.reset)) == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
			return true;
		} else {
			int itemId = item.getItemId();
			switch (itemId) {
			case android.R.id.home:
				finish();
				break;
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void resetHistory() {
		ToolsBDD.getInstance(this).resetTable();
		Toast.makeText(this, R.string.resethistory, Toast.LENGTH_LONG).show();
		finish();
	}

	@SuppressLint("NewApi")
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		setSwypeListener();
		@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
		String s = map.get("num");
		String winner = map.get("winner");
		currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);

		if (rightContainer != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			lastFragment = VisuFragment.newInstance(currentId);
			ft.replace(R.id.container_right, lastFragment);
			ft.commit();
			rightContainer.setVisibility(View.VISIBLE);
			if (statContainer != null)
				statContainer.setVisibility(View.GONE);
		} else {

			Intent intent = new Intent(HistoryActivity.this, VisuPagerActivity.class);
			intent.putExtra("id", HistoryActivity.currentId);
			intent.putExtra("isSigned", isSignedIn);
			
			Bundle b = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

				View view = arg1.findViewById(R.id.gameView1);

				Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
				if (winner.compareTo("" + MainActivity.BLUE_PLAYER) == 0) {
					bitmap.eraseColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));
					b = ActivityOptions.makeThumbnailScaleUpAnimation(view, bitmap, 0, 0).toBundle();
				} else if (winner.compareTo("" + MainActivity.RED_PLAYER) == 0) {
					bitmap.eraseColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
					b = ActivityOptions.makeThumbnailScaleUpAnimation(view, bitmap, 0, 0).toBundle();
				} else {
					bitmap.eraseColor(isDark ? Color.DKGRAY : Color.LTGRAY);
					b = ActivityOptions.makeThumbnailScaleUpAnimation(view, bitmap, 0, 0).toBundle();
				}
				startActivityForResult(intent, RESULT_CODE_DETAIL, b);
				overridePendingTransition(0, 0);
			} else {
				startActivityForResult(intent, RESULT_CODE_DETAIL);
				overridePendingTransition(0, 0);
			}
		}
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				resetHistory();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	/* MENU */

	public boolean onCreateOptionsMenu(Menu menu) {
		m = menu;
		menu.add(0, MENU_RESET, 0, R.string.reset).setIcon(isDark ? R.drawable.ic_action_trashdark : R.drawable.ic_action_trash).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (!pos.contains(arg2)) {
			if (pos.size() == 0) {
				mActionMode = startActionMode(mActionModeCallback);
			}
			pos.add(arg2);
		} else {
			pos = removeInt(pos, arg2);
		}
		if (pos.size() == 0) {
			mActionMode.finish();
			setSwypeListener();
		}
		if (mActionMode != null) {
			mActionMode.setTitle(pos.size() + "");
			removeSwypeListener();
		}
		mSchedule.notifyDataSetChanged();
		return true;
	}

	private ArrayList<Integer> removeInt(ArrayList<Integer> pos2, int arg2) {
		for (int i = 0; i < pos2.size(); i++) {
			if (pos2.get(i) == arg2) {
				pos2.remove(i);
				break;
			}
		}
		return pos2;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (rightContainer == null) {
			int num = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
			lv.setNumColumns(num);
		}
	}

	ArrayList<Integer> pos = new ArrayList<Integer>();

	private class MyAdapter extends SimpleAdapter {

		public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		public void remove(Object item) {

			@SuppressWarnings("unchecked")
			HashMap<String, String> map = (HashMap<String, String>) item;
			String s = map.get("num");
			int id = Integer.parseInt(s.split("N°")[1].split(" ")[0]);
			ToolsBDD.getInstance(getApplicationContext()).removePartie(id);

			saveId = id;
			saveWinner = Integer.parseInt(map.get("winner"));
			saveDisposition = map.get("disposition");
			saveFromCards = false;
			mUndoBarController.showUndoBar(false, getString(R.string.undobar_sample_message), null);

			listItem.remove(item);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			String resultat = listItem.get(position).get("disposition");
			int[][] val = new int[3][3];
			int tooker = 0;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					try {
						val[i][j] = Integer.parseInt(resultat.split(",")[tooker]);
					} catch (Exception e) {
						val[i][j] = MainActivity.NONE_PLAYER;
					}
					tooker++;
				}
			}

			GameView gv = (GameView) v.findViewById(R.id.gameView1);
			gv.setMode(GameView.MODE_NOT_INTERACTIVE);
			gv.setAlignement(GameView.STYLE_CENTER_HORIZONTAL);
			gv.setDark(isDark);
			gv.setStrikeWidth(1);
			gv.setDark(isDark);
			gv.setShowWinner(true);
			gv.setValues(val, MainActivity.BLUE_PLAYER);

			gv.setHoverHandler(HistoryActivity.this);

			View selectorCheck = v.findViewById(R.id.cab_selector);

			if (pos != null) {
				if (pos.contains(position)) {
					// v.setBackgroundColor(isDark ? Color.rgb(19, 133, 173) : Color.LTGRAY);
					selectorCheck.setVisibility(View.VISIBLE);
					gv.setVisibility(View.GONE);
				} else {
					// v.setBackgroundColor(Color.TRANSPARENT);
					selectorCheck.setVisibility(View.GONE);
					gv.setVisibility(View.VISIBLE);
				}
				if (pos.size() == 0) {
					selectorCheck.setVisibility(View.GONE);
					gv.setVisibility(View.VISIBLE);
				}
			}

			int winner = Integer.parseInt(listItem.get(position).get("winner"));

			if (winner == MainActivity.BLUE_PLAYER) {
				selectorCheck.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.BLUE_PLAYER)));
			} else if (winner == MainActivity.RED_PLAYER) {
				selectorCheck.setBackgroundColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(MainActivity.RED_PLAYER)));
			} else {
				selectorCheck.setBackgroundColor(isDark ? Color.rgb(19, 133, 173) : Color.LTGRAY);
			}

			final int indexForselector = position;
			selectorCheck.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemLongClick(null, null, indexForselector, 0);
				}
			});

			gv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemLongClick(null, null, indexForselector, 0);
				}
			});

			return v;
		}
	}

	public class AbMenu {
		public int icon;
		public String title;
		public int id;

		public AbMenu() {
			super();
		}

		public AbMenu(int icon, String title, int id) {
			super();
			this.icon = icon;
			this.title = title;
			this.id = id;
		}
	}

	public class AbMenuAdapter extends BaseAdapter {

		Context context;
		int layoutResourceId;
		ArrayList<AbMenu> data;
		LayoutInflater inflater;

		public AbMenuAdapter(Context a, int textViewResourceId, ArrayList<AbMenu> data) {
			// super(a, textViewResourceId, data);
			this.data = data;
			inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = a;
			this.layoutResourceId = textViewResourceId;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(layoutResourceId, null);
			}
			final AbMenu item = data.get(position);
			if (item != null) {
				((android.widget.TextView) v.findViewById(R.id.textViewSpinner)).setText(item.title);
				((ImageView) v.findViewById(R.id.imageViewSpinner)).setImageResource(item.icon);
			}
			return v;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(layoutResourceId, null);
			}
			final AbMenu item = data.get(position);
			if (item != null) {
				((android.widget.TextView) v.findViewById(R.id.textViewSpinner)).setText(item.title);
				((android.widget.TextView) v.findViewById(R.id.textViewSpinner)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				((ImageView) v.findViewById(R.id.imageViewSpinner)).setImageResource(item.icon);
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

	public void animateToRight(final int pos, final boolean reload) {

		int firstPosition = lv.getFirstVisiblePosition();
		int wantedChild = pos - firstPosition;

		if (wantedChild < 0 || wantedChild >= lv.getChildCount()) {

		} else {
			final View wanted = lv.getChildAt(wantedChild);
			animate(wanted).translationX(displayWidth).alpha(0).setDuration(400).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {

					animate(wanted).translationX(0).alpha(1).setDuration(50).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {

						}
					});
				}
			});
		}

		if (reload) {
			new AsyncReload().execute();
		}
	}

	class AsyncReload extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			reloadItems();
			mSchedule.notifyDataSetChanged();
			if (mActionMode != null) {
				mActionMode.finish();
			}
			super.onPostExecute(result);
		}

	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(0, 50, 0, R.string.delete).setIcon(isDark ? R.drawable.ic_action_trashdark : R.drawable.ic_action_trash).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case 50:
				boolean relaod = true;
				for (int num : pos) {
					HashMap<String, String> map = (HashMap<String, String>) listItem.get(num);
					String s = map.get("num");
					currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);
					ToolsBDD.getInstance(getApplicationContext()).removePartie(HistoryActivity.currentId);
					animateToRight(num, relaod);
					relaod = false;
				}

				return true;
			default:

				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			pos = new ArrayList<Integer>();
			mSchedule.notifyDataSetChanged();
			setSwypeListener();
		}
	};

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if (itemPosition == 0) {
			createList();
		}
		if (itemPosition == 1) {
			if (ToolsBDD.getInstance(this).getNbPartie() >= 2) {
				createChart();
			} else {
				Toast.makeText(this, R.string.charteneeds, Toast.LENGTH_SHORT).show();
				getSupportActionBar().setSelectedNavigationItem(0);
			}
		}
		if (itemPosition == 2) {
			finish();
		}

		return false;
	}

	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	private void initViews() {
		if (rightContainer != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			lastFragment = RightFillerFragment.newInstance();
			ft.replace(R.id.container_right, lastFragment);
			ft.commit();
			if (statContainer != null)
				statContainer.setVisibility(View.GONE);
			rightContainer.setVisibility(View.VISIBLE);
		}

		lv = (GridView) findViewById(R.id.listviewperso);
		lv.setVisibility(View.VISIBLE);

		statContainer = findViewById(R.id.statContainer);
		statContainer.setVisibility(View.GONE);

		mUndoBarController = new UndoBarController(findViewById(R.id.undobar), this);

	}

	private void createChart() {
		initViews();

		if (rightContainer == null) {
			lv.setVisibility(View.GONE);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(lastFragment);
			ft.commit();

			rightContainer.setVisibility(View.GONE);
		}

		statContainer.setVisibility(View.VISIBLE);
		chartView = (ChartView) findViewById(R.id.chart_view);
		BarGraph bargraph = (BarGraph) findViewById(R.id.bargraph);

		chartView.setGridLineColor(isDark ? Color.rgb(19, 133, 173) : Color.BLACK);
		chartView.setGridLinesVertical(0);
		chartView.setGridLinesHorizontal(0);
		LinearSeries seriesBlue = new LinearSeries();
		seriesBlue.setLineColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.BLUE_PLAYER)));
		seriesBlue.setLineWidth(convertDpToPixel(1.5f, getApplicationContext()));

		LinearSeries seriesRed = new LinearSeries();
		seriesRed.setLineColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.RED_PLAYER)));
		seriesRed.setLineWidth(convertDpToPixel(1.5f, getApplicationContext()));

		LinearSeries seriesGreen = new LinearSeries();
		seriesGreen.setLineColor(isDark ? Color.WHITE : Color.BLACK);
		seriesGreen.setLineWidth(convertDpToPixel(1.5f, getApplicationContext()));

		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		c.moveToFirst();

		int bluecount = 0;
		int greencount = 0;
		int redcount = 0;

		for (int i = 0; i < c.getCount(); i++) {
			int n = c.getInt(1);
			if (n == MainActivity.BLUE_PLAYER) {
				bluecount++;
			} else if (n == MainActivity.RED_PLAYER) {
				redcount++;
			} else {
				greencount++;
			}

			seriesBlue.addPoint(new LinearPoint(i, bluecount));
			seriesRed.addPoint(new LinearPoint(i, redcount));
			seriesGreen.addPoint(new LinearPoint(i, greencount));
			c.moveToNext();
		}
		c.close();
		chartView.clearSeries();
		chartView.addSeries(seriesBlue);
		chartView.addSeries(seriesRed);
		chartView.addSeries(seriesGreen);

		// bar
		ArrayList<Bar> points = new ArrayList<Bar>();
		Bar d = new Bar();
		d.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.BLUE_PLAYER)));
		d.setValue(bluecount);
		Bar d2 = new Bar();
		d2.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.RED_PLAYER)));
		d2.setValue(redcount);
		Bar d3 = new Bar();
		d3.setColor(isDark ? Color.LTGRAY : Color.DKGRAY);
		d3.setValue(greencount);

		points.add(d);
		points.add(d3);
		points.add(d2);

		bargraph.setBars(points);

		// pie
		PieGraph pieBlue = (PieGraph) findViewById(R.id.pieBlue);
		pieBlue.setSlices(new ArrayList<PieSlice>());
		PieSlice slice = new PieSlice();
		slice.setColor(isDark ? Color.BLACK : Color.WHITE);
		slice.setValue(redcount + greencount);
		pieBlue.addSlice(slice);
		slice = new PieSlice();
		slice.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.BLUE_PLAYER)));
		slice.setValue(bluecount);
		pieBlue.addSlice(slice);

		PieGraph pieRed = (PieGraph) findViewById(R.id.pieRed);
		pieRed.setSlices(new ArrayList<PieSlice>());
		slice = new PieSlice();
		slice.setColor(isDark ? Color.BLACK : Color.WHITE);
		slice.setValue(bluecount + greencount);
		pieRed.addSlice(slice);
		slice = new PieSlice();
		slice.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.RED_PLAYER)));
		slice.setValue(redcount);
		pieRed.addSlice(slice);

		PieGraph pieTie = (PieGraph) findViewById(R.id.pieTie);
		pieTie.setSlices(new ArrayList<PieSlice>());
		slice = new PieSlice();
		slice.setColor(isDark ? Color.BLACK : Color.WHITE);
		slice.setValue(bluecount + redcount);
		pieTie.addSlice(slice);
		slice = new PieSlice();
		slice.setColor(isDark ? Color.LTGRAY : Color.DKGRAY);
		slice.setValue(greencount);
		pieTie.addSlice(slice);

		PieGraph pieAll = (PieGraph) findViewById(R.id.pieAll);
		pieAll.setSlices(new ArrayList<PieSlice>());
		slice = new PieSlice();
		slice.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.BLUE_PLAYER)));
		slice.setValue(bluecount);
		pieAll.addSlice(slice);
		slice = new PieSlice();
		slice.setColor(isDark ? Color.LTGRAY : Color.DKGRAY);
		slice.setValue(greencount);
		pieAll.addSlice(slice);
		slice = new PieSlice();
		slice.setColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.RED_PLAYER)));
		slice.setValue(redcount);

		pieAll.addSlice(slice);
	}

	int saveId = -1;
	int saveWinner = -1;
	String saveDisposition = "";
	boolean saveFromCards = false;

	@Override
	public void onUndo(Parcelable token) {

		ToolsBDD.getInstance(this).insertPartie(saveId, saveWinner, saveDisposition);
		if (saveFromCards) {
			items.clear();

			Cursor c = ToolsBDD.getInstance(this).getAllParties();
			if (c == null || c.getCount() == 0) {
				share = getString(R.string.sharetry);
				c.close();
			} else {
				share = getString(R.string.app_name) + " - https://play.google.com/store/apps/details?id=fr.mathis.morpion - ";
				int win = 0;
				int lost = 0;
				int equal = 0;
				c.moveToFirst();
				for (int i = 0; i < c.getCount(); i++) {
					int n = c.getInt(1);
					if (n == MainActivity.BLUE_PLAYER) {
						win++;
					} else if (n == MainActivity.RED_PLAYER) {
						lost++;
					} else {
						equal++;
					}
					items.add(i);
					c.moveToNext();
				}
				c.close();
				share += (win + lost + equal) + " " + getString(R.string.share1);
				share += " " + win + " " + getString(R.string.share2);
				share += " " + lost + " " + getString(R.string.share);
			}
		} else {
			HashMap<String, String> map = new HashMap<String, String>();

			int n = saveWinner;
			if (n == MainActivity.BLUE_PLAYER) {
				map = new HashMap<String, String>();
				map.put("titre", "" + getString(R.string.win));
				map.put("description", getString(R.string.s35));
				map.put("num", "N°" + saveId);
			} else if (n == MainActivity.RED_PLAYER) {
				map = new HashMap<String, String>();
				map.put("titre", "" + getString(R.string.loose));
				map.put("description", getString(R.string.s35));
				map.put("num", "N°" + saveId);
			} else {
				map = new HashMap<String, String>();
				map.put("titre", "" + getString(R.string.equal));
				map.put("description", getString(R.string.s35));
				map.put("num", "N°" + saveId);
			}

			map.put("winner", n + "");
			map.put("disposition", saveDisposition);

			listItem.add(findCorrectPlace(map), map);
			mSchedule.notifyDataSetChanged();
		}
	}

	private int findCorrectPlace(HashMap<String, String> map) {

		int res = 0;
		int toinsertid = Integer.parseInt(map.get("num").split("N°")[1].split(" ")[0]);
		for (res = 0; res < listItem.size(); res++) {
			String titleCurrent = listItem.get(res).get("num");
			int currentid = Integer.parseInt(titleCurrent.split("N°")[1].split(" ")[0]);
			if (toinsertid <= currentid) {
				break;
			}
		}
		return res;
	}

	@Override
	public void give(MotionEvent ev, GameView gv) {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (ev.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
				if (popoup != null && popoup.isShowing())
					popoup.dismiss();
				gv.setHoveredMode(true);
				View popupView = getLayoutInflater().inflate(isDark ? R.layout.popup_gameviewdark : R.layout.popup_gameview, null);
				GameView gvPopup = (GameView) popupView.findViewById(R.id.popupGame);
				gvPopup.setMode(GameView.MODE_NOT_INTERACTIVE);
				gvPopup.setAlignement(GameView.STYLE_CENTER_HORIZONTAL);
				gvPopup.setDark(isDark);
				gvPopup.setStrikeWidth(1);
				gvPopup.setDark(isDark);
				gvPopup.setShowWinner(true);
				gvPopup.setValues(gv.getValues(), MainActivity.BLUE_PLAYER);

				popoup = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				popoup.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				// set window at position
				int[] xy = new int[2];
				gv.getLocationOnScreen(xy);
				popoup.showAtLocation(this.getWindow().getDecorView(), Gravity.RIGHT | Gravity.TOP, xy[0] + gv.getWidth(), xy[1] + gv.getHeight() / 2 - ((int) convertDpToPixel(95, getApplicationContext())));
				popoup.update();
			}
			if (ev.getAction() == MotionEvent.ACTION_HOVER_MOVE) {

			}
			if (ev.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
				if (popoup != null && popoup.isShowing())
					popoup.dismiss();
				gv.setHoveredMode(false);
			}
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4) {

		if (arg2 == 1 && arg3 == 0) {
			if (mDrawerLayout != null)
				mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			Intent output = new Intent();
			output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP, arg2);
			output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP_CHILD, arg3);
			setResult(RESULT_OK, output);
			finish();
			overridePendingTransition(0, 0);
		}

		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RESULT_CODE_DETAIL:
			if (resultCode == Activity.RESULT_OK) {
				Intent output = new Intent();
				output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP, data.getIntExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP, -1));
				output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP_CHILD, data.getIntExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP_CHILD, -1));
				setResult(RESULT_OK, output);
				finish();
				overridePendingTransition(0, 0);
			}

			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent output = new Intent();
		output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP, 100);
		output.putExtra(MainActivity.ACTIVITY_HISTORY_RES_GROUP_CHILD, 100);
		setResult(RESULT_OK, output);
		finish();
	}

}
