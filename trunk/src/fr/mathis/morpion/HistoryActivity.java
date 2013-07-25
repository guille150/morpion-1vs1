package fr.mathis.morpion;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.michaelpardo.android.widget.chartview.ChartView;
import com.michaelpardo.android.widget.chartview.LinearSeries;
import com.michaelpardo.android.widget.chartview.LinearSeries.LinearPoint;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.StateHolder;
import fr.mathis.morpion.tools.SwipeDismissListViewTouchListener;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.tools.UndoBarController;
import fr.mathis.morpion.tools.UndoBarController.UndoListener;

public class HistoryActivity extends SherlockActivity implements OnItemLongClickListener, OnItemClickListener, OnNavigationListener, UndoListener, OnDismissCallback, HoverHandler {

	static final int MENU_RESET = 0;
	static final int MENU_SHARE = 2;
	private static int currentId;
	ArrayList<HashMap<String, String>> listItem;
	private ListView lv;
	Button visu;
	Button effacer;
	Dialog dialog;
	String share;
	MyAdapter mSchedule;
	ActionMode mActionMode;
	ChartView chartView;
	private UndoBarController mUndoBarController;
	public boolean isDark;
	Timer timer;
	ShowcaseView sv;
	int displayWidth = 2000;
	ListView listViewcards;
	GoogleCardsAdapter mGoogleCardsAdapter;
	ArrayList<Integer> items;
	PopupWindow popoup;

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
		data.add(new AbMenu(isDark ? R.drawable.ic_action_spinner_carddark : R.drawable.ic_action_spinner_cards, getString(R.string.m5), 2));
		data.add(new AbMenu(isDark ? R.drawable.ic_action_spinner_chartdark : R.drawable.ic_action_spinner_chart, getString(R.string.m6), 3));

		AbMenuAdapter adapter = new AbMenuAdapter(context, R.layout.ab_spinner_item, data);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(adapter, this);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setContentView(isDark ? R.layout.listviewcustomdark : R.layout.listviewcustom);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		displayWidth = metrics.widthPixels;
	}

	public void createList() {

		lv = (ListView) findViewById(R.id.listviewperso);
		lv.setVisibility(View.VISIBLE);

		listViewcards = (ListView) findViewById(R.id.listviewcards);
		listViewcards.setVisibility(View.GONE);

		chartView = (ChartView) findViewById(R.id.chart_view);
		chartView.setVisibility(View.GONE);

		mUndoBarController = new UndoBarController(findViewById(R.id.undobar), this);

		listItem = new ArrayList<HashMap<String, String>>();
		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			share = getString(R.string.sharetry);
		} else {
			reloadItems();
		}
		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setSwypeListener();

		TypedValue tv = new TypedValue();
		this.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true);
		int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

		if (!StateHolder.GetMemorizedValue("swype", this)) {
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = true;

			sv = ShowcaseView.insertShowcaseView(actionBarHeight * 2, actionBarHeight * 2, this, R.string.sc1, R.string.sc2, co);
			sv.animateGesture(0, 0, +600, 0);
			new Async().execute();
			sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
				}

				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					StateHolder.MemorizeValue("swype", true, getApplicationContext());
				}
			});

		}
	}

	public void reloadItems() {
		listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map = new HashMap<String, String>();

		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			share = getString(R.string.sharetry);
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
			share += (win + lost + equal) + " " + getString(R.string.share1);
			share += " " + win + " " + getString(R.string.share2);
			share += " " + lost + " " + getString(R.string.share3);
		}

		mSchedule = new MyAdapter(this.getBaseContext(), listItem, R.layout.itemlistviewcustom, new String[] { "titre", "description", "num" }, new int[] { R.id.titre, R.id.description, R.id.num });
		lv.setAdapter(mSchedule);
	}

	class Async extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(4000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (sv != null)
				sv.animateGesture(0, 0, +600, 0);
			super.onPostExecute(result);
		}
	}

	private void setSwypeListener() {

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(lv, new SwipeDismissListViewTouchListener.OnDismissCallback() {
			@Override
			public void onDismiss(ListView listView, int[] reverseSortedPositions) {
				for (int position : reverseSortedPositions) {
					mSchedule.remove(mSchedule.getItem(position));
				}
				mSchedule.notifyDataSetChanged();
				if (sv != null) {
					sv.hide();
					sv = null;
				}
			}
		});
		lv.setOnTouchListener(touchListener);
		lv.setOnScrollListener(touchListener.makeScrollListener());
	}

	private void removeSwypeListener() {
		lv.setOnTouchListener(null);
		lv.setOnScrollListener(null);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getTitle().toString().compareTo(getString(R.string.share)) == 0) {
			share();
		} else if (item.getTitle().toString().compareTo(getString(R.string.reset)) == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
		} else {
			int itemId = item.getItemId();
			switch (itemId) {
			case android.R.id.home:
				finish();
				break;
			}
		}

		return true;
	}

	private void share() {
		final Intent MessIntent = new Intent(Intent.ACTION_SEND);
		MessIntent.setType("text/plain");
		MessIntent.putExtra(Intent.EXTRA_TEXT, share);
		HistoryActivity.this.startActivity(Intent.createChooser(MessIntent, getString(R.string.sharewith)));
	}

	private void resetHistory() {
		ToolsBDD.getInstance(this).resetTable();
		Toast.makeText(this, R.string.resethistory, Toast.LENGTH_LONG).show();
		finish();
	}

	@SuppressLint("NewApi")
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (pos.size() == 0) {
			setSwypeListener();
			@SuppressWarnings("unchecked")
			HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
			String s = map.get("num");
			String winner = map.get("winner");
			currentId = Integer.parseInt(s.split("N°")[1].split(" ")[0]);

			Intent intent = new Intent(HistoryActivity.this, VisuPagerActivity.class);
			intent.putExtra("id", HistoryActivity.currentId);

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
				startActivity(intent, b);
			} else {
				startActivity(intent);
			}

		} else {
			if (!pos.contains(arg2)) {
				pos.add(arg2);
			} else {
				pos = removeInt(pos, arg2);
			}

			if (pos.size() == 0) {
				mActionMode.finish();
				setSwypeListener();
			} else {
				removeSwypeListener();
				if (mActionMode != null) {
					if (pos.size() == 1)
						mActionMode.setTitle(pos.size() + " " + getString(R.string.s2));
					else
						mActionMode.setTitle(pos.size() + " " + getString(R.string.s1));
				}
			}
			mSchedule.notifyDataSetChanged();
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
		menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(isDark ? R.drawable.ic_action_sharedark : R.drawable.ic_action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
			if (pos.size() == 1)
				mActionMode.setTitle(pos.size() + " " + getString(R.string.s2));
			else
				mActionMode.setTitle(pos.size() + " " + getString(R.string.s1));
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

	/* BLOCK THE ROTATION OF THE SCREEN */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
					}
					catch (Exception e) {
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

			CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox1);
			final int posid = position;
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int i = posid;
					if (isChecked) {
						if (pos.contains(i)) {

						} else {
							pos.add(i);
							mSchedule.notifyDataSetChanged();
						}
					} else {
						pos = removeInt(pos, i);
						mSchedule.notifyDataSetChanged();
					}

					if (mActionMode != null) {
						if (pos.size() == 1)
							mActionMode.setTitle(pos.size() + " " + getString(R.string.s2));
						else
							mActionMode.setTitle(pos.size() + " " + getString(R.string.s1));
						removeSwypeListener();
						if (pos.size() == 0) {
							setSwypeListener();
							mActionMode.finish();
						}
					}

				}
			});

			if (pos != null) {
				if (pos.contains(position)) {
					v.setBackgroundColor(isDark ? Color.rgb(19, 133, 173) : Color.LTGRAY);
					cb.setChecked(true);
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
					cb.setChecked(false);
				}
				if (pos.size() != 0) {
					cb.setVisibility(View.VISIBLE);
				} else {
					cb.setVisibility(View.GONE);
				}
			}
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

		int firstPosition = lv.getFirstVisiblePosition() - lv.getHeaderViewsCount();
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
			}
			catch (InterruptedException e) {
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
			createCards();
		}
		if (itemPosition == 2) {
			if (ToolsBDD.getInstance(this).getNbPartie() >= 2) {
				createChart();
			} else {
				Toast.makeText(this, R.string.charteneeds, Toast.LENGTH_SHORT).show();
				getSupportActionBar().setSelectedNavigationItem(0);
			}
		}
		if (itemPosition == 3) {
			finish();
		}

		return false;
	}

	private void createChart() {
		lv.setVisibility(View.GONE);
		listViewcards.setVisibility(View.GONE);
		chartView.setVisibility(View.VISIBLE);
		chartView.setGridLineColor(isDark ? Color.rgb(19, 133, 173) : Color.BLACK);
		LinearSeries seriesBlue = new LinearSeries();
		seriesBlue.setLineColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.BLUE_PLAYER)));
		seriesBlue.setLineWidth(4);

		LinearSeries seriesRed = new LinearSeries();
		seriesRed.setLineColor(Color.parseColor(ColorHolder.getInstance(this).getColor(MainActivity.RED_PLAYER)));
		seriesRed.setLineWidth(4);

		LinearSeries seriesGreen = new LinearSeries();
		seriesGreen.setLineColor(isDark ? Color.WHITE : Color.BLACK);
		seriesGreen.setLineWidth(4);

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

		chartView.addSeries(seriesBlue);
		chartView.addSeries(seriesRed);
		chartView.addSeries(seriesGreen);
	}

	public void createCards() {
		listViewcards.setVisibility(View.VISIBLE);
		chartView.setVisibility(View.GONE);
		lv.setVisibility(View.GONE);

		mGoogleCardsAdapter = new GoogleCardsAdapter(this, isDark);
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mGoogleCardsAdapter, this));
		swingBottomInAnimationAdapter.setListView(listViewcards);

		listViewcards.setAdapter(swingBottomInAnimationAdapter);
		listViewcards.setOnItemClickListener(this);
		items = new ArrayList<Integer>();
		Cursor c = ToolsBDD.getInstance(this).getAllParties();
		if (c == null || c.getCount() == 0) {
			share = getString(R.string.sharetry);
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
			share += (win + lost + equal) + " " + getString(R.string.share1);
			share += " " + win + " " + getString(R.string.share2);
			share += " " + lost + " " + getString(R.string.share);
		}
		mGoogleCardsAdapter.addAll(items);

	}

	private static class GoogleCardsAdapter extends ArrayAdapter<Integer> {

		private Context mContext;
		private boolean isDark;

		public GoogleCardsAdapter(Context context, boolean isDark) {
			mContext = context;
			this.isDark = isDark;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(mContext).inflate(isDark ? R.layout.activity_googlecards_carddark : R.layout.activity_googlecards_card, parent, false);
			}

			ImageView cercle = (ImageView)view.findViewById(R.id.imageViewcerclewinner);
			ImageView croix = (ImageView)view.findViewById(R.id.imageViewcroixwinner);
			
			cercle.setImageResource(ColorHolder.getInstance(mContext).getDrawable(MainActivity.RED_PLAYER));
			croix.setImageResource(ColorHolder.getInstance(mContext).getDrawable(MainActivity.BLUE_PLAYER));

			String textViewTitle = "";
			String result = "";

			Cursor c = ToolsBDD.getInstance(mContext).getAllParties();
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				if (i == position) {
					int n = c.getInt(1);
					if (n == MainActivity.BLUE_PLAYER) {
						cercle.setVisibility(View.INVISIBLE);
						croix.setVisibility(View.VISIBLE);
						textViewTitle = mContext.getString(R.string.win);
					} else if (n == MainActivity.RED_PLAYER) {
						cercle.setVisibility(View.VISIBLE);
						croix.setVisibility(View.INVISIBLE);
						textViewTitle = mContext.getString(R.string.loose);
					} else {
						textViewTitle = mContext.getString(R.string.equal);
						cercle.setVisibility(View.INVISIBLE);
						croix.setVisibility(View.INVISIBLE);
					}
					textViewTitle = c.getInt(0) + " - " + textViewTitle;
					result = c.getString(2);
				}
				c.moveToNext();
			}

			int[][] val = new int[3][3];
			int tooker = 0;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					try {
						val[i][j] = Integer.parseInt(result.split(",")[tooker]);
					}
					catch (Exception e) {
						val[i][j] = MainActivity.NONE_PLAYER;
					}
					tooker++;
				}
			}

			GameView gv = (GameView) view.findViewById(R.id.gameView1);
			gv.setMode(GameView.MODE_NOT_INTERACTIVE);
			gv.setDark(isDark);
			gv.setAlignement(GameView.STYLE_CENTER_BOTH);
			gv.setValues(val, MainActivity.BLUE_PLAYER);
			gv.setShowWinner(true);

			return view;
		}
	}

	int saveId = -1;
	int saveWinner = -1;
	String saveDisposition = "";
	boolean saveFromCards = false;

	@Override
	public void onUndo(Parcelable token) {

		ToolsBDD.getInstance(this).insertPartie(saveId, saveWinner, saveDisposition);
		if (saveFromCards) {
			mGoogleCardsAdapter.clear();
			items.clear();

			Cursor c = ToolsBDD.getInstance(this).getAllParties();
			if (c == null || c.getCount() == 0) {
				share = getString(R.string.sharetry);
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
				share += (win + lost + equal) + " " + getString(R.string.share1);
				share += " " + win + " " + getString(R.string.share2);
				share += " " + lost + " " + getString(R.string.share);
			}
			mGoogleCardsAdapter.addAll(items);
			mGoogleCardsAdapter.notifyDataSetChanged();
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
	public void onDismiss(ListView listView, int[] reverseSortedPositions) {
		int id = 0;

		for (int position : reverseSortedPositions) {
			mGoogleCardsAdapter.remove(mGoogleCardsAdapter.getItem(position));
		}
		mGoogleCardsAdapter.notifyDataSetChanged();

		Cursor c = ToolsBDD.getInstance(getApplicationContext()).getAllParties();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			if (i == reverseSortedPositions[0]) {
				id = c.getInt(0);
				saveWinner = c.getInt(1);
				saveDisposition = c.getString(2);
			}
			c.moveToNext();
		}

		ToolsBDD.getInstance(getApplicationContext()).removePartie(id);
		saveId = id;
		saveFromCards = true;

		mUndoBarController.showUndoBar(false, getString(R.string.undobar_sample_message), null);
	}

	@Override
	public void give(MotionEvent ev, GameView gv) {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (ev.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
				if (popoup != null && popoup.isShowing())
					popoup.dismiss();
				gv.setHoveredMode(true);
				View popupView = getLayoutInflater().inflate(isDark ?R.layout.popup_gameviewdark : R.layout.popup_gameview, null);
				GameView gvPopup = (GameView)popupView.findViewById(R.id.popupGame);
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
				popoup.showAtLocation(this.getWindow().getDecorView(), Gravity.LEFT | Gravity.TOP, xy[0] + (int) MainActivity.convertDpToPixel(0, getApplicationContext()) + gv.getWidth(), xy[1] - gv.getHeight() / 2 - getSupportActionBar().getHeight());
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
	
	
}
