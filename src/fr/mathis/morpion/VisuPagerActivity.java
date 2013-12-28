package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import fr.mathis.morpion.fragments.VisuFragment;
import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.views.GameView;

public class VisuPagerActivity extends SherlockFragmentActivity implements OnPageChangeListener, OnChildClickListener, OnItemClickListener {

	private int NUM_PAGES = 10;
	private ArrayList<Integer> indexs;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	boolean isDark;
	PagerSlidingTabStrip tabs;
	private String defaultColor = "#666666";
	ArrayList<HashMap<String, String>> listItem;
	private ActionBarDrawerToggle mDrawerToggle;
	ArrayList<NavigationSection> navSections;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	NavigationAdapter navAdapter;
	Menu m;
	boolean isSignedIn = false;
	int premsIndex = -1;

	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark) {
			setTheme(R.style.AppThemeDark);
		}
		super.onCreate(savedInstanceState);

		premsIndex = this.getIntent().getIntExtra("id", 0);

		initiActivity();
	}

	private void initiActivity() {
		boolean wasNew = true;
		if (mPager != null && mPagerAdapter != null) {
//			mPager.removeAllViews();
//			NUM_PAGES = 0;
//			mPagerAdapter.notifyDataSetChanged();
//			NUM_PAGES = 10;
			wasNew = false;
		}
		else {
			setContentView(isDark ? R.layout.visupagedark : R.layout.visupage);
			mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		}

		isSignedIn = getIntent().getBooleanExtra("isSigned", false);
		getSupportActionBar().setTitle("");

		

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

		NUM_PAGES = ToolsBDD.getInstance(this).getNbPartie();
		indexs = new ArrayList<Integer>();

		int pos = 0;

		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();

		Cursor c = ToolsBDD.getInstance(this).getAllParties();

		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			map = new HashMap<String, String>();
			if (c.getInt(0) == premsIndex)
				pos = i;

			int n = c.getInt(1);
			map.put("num", "" + c.getInt(0));
			map.put("winner", n + "");
			listItem.add(map);
			c.moveToNext();
		}
		c.close();

		ArrayList<HashMap<String, String>> realItems = new ArrayList<HashMap<String, String>>();
		int realpos = 0;
		for (int i = pos - 10; i < pos + 11; i++) {
			if (i >= 0 && i < listItem.size()) {
				realItems.add(listItem.get(i));
				if (i < pos) {
					realpos++;
				}
			}

		}
		listItem = realItems;
		pos = realpos;
		NUM_PAGES = listItem.size();

		mPager = (ViewPager) findViewById(R.id.pager);
		

		mPager.setAdapter(mPagerAdapter);

		mPager.setPageMargin((int) convertDpToPixel(9, this));
		mPager.setPageMarginDrawable(isDark ? R.drawable.lineblue : R.drawable.linegraypager);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

		tabs.setViewPager(mPager);
		tabs.setOnPageChangeListener(this);

		mPager.setCurrentItem(0);

		indexs.add(pos);

		if (findViewById(R.id.drawer_layout) != null)
			initDrawer();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		// mPager.setCurrentItem(pos, false);
		new Async().execute(pos);
		if(!wasNew)
			mPagerAdapter.notifyDataSetChanged();
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
			showClosedIcon();
			mDrawerToggle.setDrawerIndicatorEnabled(false);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		new Async().execute(mPager.getCurrentItem());
	}

	class Async extends AsyncTask<Integer, Void, Integer> {
		@Override
		protected Integer doInBackground(Integer... params) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(Integer result) {
			mPager.setCurrentItem(result, false);
			super.onPostExecute(result);
		}

	}

	private void changeColor(int newColor) {
		tabs.setIndicatorColor(newColor);
	}

	@Override
	public void onBackPressed() {
		if (indexs.size() > 1) {
			int i1 = indexs.get(0);
			int i2 = indexs.get(1);
			if (indexs.size() == 2 && i1 == i2) {
				finish();
				return;
			}
		} else
			finish();

		if (indexs.size() > 1) {
			mPager.setCurrentItem(indexs.get(indexs.size() - 2));
			indexs.remove(indexs.size() - 1);
			if (indexs.size() > 1)
				indexs.remove(indexs.size() - 1);
		} else {
			finish();
		}
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(getMenuItem(item))) {
			return true;
		}

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {

			if (listItem != null) {
				HashMap<String, String> map = listItem.get(position);
				String s = map.get("num") + " - ";
				int n = Integer.parseInt(map.get("winner"));
				if (n == MainActivity.BLUE_PLAYER) {
					s += getString(R.string.win);
				} else if (n == MainActivity.RED_PLAYER) {
					s += getString(R.string.loose);
				} else {
					s += getString(R.string.equal);
				}

				return s;
			}
			return "error";
		}

		@Override
		public Fragment getItem(int position) {
			HashMap<String, String> map = listItem.get(position);
			return VisuFragment.newInstance(Integer.parseInt(map.get("num")));
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		indexs.add(arg0);
		HashMap<String, String> map = listItem.get(arg0);

		int n = Integer.parseInt(map.get("winner"));

		if (n != MainActivity.NONE_PLAYER)
			changeColor(Color.parseColor(ColorHolder.getInstance(getApplicationContext()).getColor(n)));
		else {
			changeColor(Color.parseColor(defaultColor + ""));
		}

		if ((arg0 == listItem.size() - 1 && ToolsBDD.getInstance(getApplicationContext()).getNextId(Integer.parseInt(map.get("num"))) != -1) || (arg0 == 0 && ToolsBDD.getInstance(getApplicationContext()).getPreviousId(Integer.parseInt(map.get("num"))) != -1)) {
			premsIndex = Integer.parseInt(map.get("num"));
			initiActivity();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

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

}