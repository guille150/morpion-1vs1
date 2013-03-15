package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.Card.OnCardSwiped;
import com.fima.cardsui.views.CardUI;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;
import com.michaelpardo.android.widget.TextView;
import com.michaelpardo.android.widget.chartview.ChartView;
import com.michaelpardo.android.widget.chartview.LabelAdapter;
import com.michaelpardo.android.widget.chartview.LinearSeries;
import com.michaelpardo.android.widget.chartview.LinearSeries.LinearPoint;
import com.slidingmenu.lib.SlidingMenu;

import fr.mathis.morpion.tools.StateHolder;
import fr.mathis.morpion.tools.SwipeDismissListViewTouchListener;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.tools.UndoBarController;
import fr.mathis.morpion.tools.UndoBarController.UndoListener;

public class HistoryActivity extends SherlockActivity implements OnItemLongClickListener, OnItemClickListener, OnNavigationListener, OnCardSwiped, UndoListener {

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
	CardUI cards;
	ChartView chartView;
	private UndoBarController mUndoBarController;
	boolean isDark;
	private SlidingMenu menu;
	Timer timer;
	ShowcaseView sv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDark);

		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.secondNavigationList, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportActionBar().setTitle("");

		setContentView(R.layout.listviewcustom);

		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(isDark ? R.layout.menudark : R.layout.menu);
	}

	public void createList() {

		lv = (ListView) findViewById(R.id.listviewperso);
		lv.setVisibility(View.VISIBLE);

		cards = (CardUI) findViewById(R.id.cardsview);
		if (!isDark)
			cards.setBackgroundResource(R.color.abs__background_holo_light);
		cards.setVisibility(View.GONE);

		chartView = (ChartView) findViewById(R.id.chart_view);
		chartView.setVisibility(View.GONE);

		mUndoBarController = new UndoBarController(findViewById(R.id.undobar), this);

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
					map.put("titre", "N�" + c.getInt(0) + " - " + getString(R.string.win));
					map.put("description", getString(R.string.winb));
					map.put("img", String.valueOf(R.drawable.croix));
					win++;
				} else if (n == MainActivity.RED_PLAYER) {
					map = new HashMap<String, String>();
					map.put("titre", "N�" + c.getInt(0) + " - " + getString(R.string.loose));
					map.put("description", getString(R.string.winr));
					map.put("img", String.valueOf(R.drawable.cercle));
					lost++;
				} else {
					map = new HashMap<String, String>();
					map.put("titre", "N�" + c.getInt(0) + " - " + getString(R.string.equal));
					map.put("description", getString(R.string.equaltry));
					map.put("img", String.valueOf(R.drawable.icon));
					equal++;
				}
				map.put("winner", n + "");
				map.put("disposition", c.getString(2));
				listItem.add(map);
				c.moveToNext();
			}
			share += (win + lost + equal) + " " + getString(R.string.share1);
			share += " " + win + " " + getString(R.string.share2);
			share += " " + lost + " " + getString(R.string.share3);
		}

		mSchedule = new MyAdapter(this.getBaseContext(), listItem, R.layout.itemlistviewcustom, new String[] { "img", "titre", "description" }, new int[] { R.id.img, R.id.titre, R.id.description });
		lv.setAdapter(mSchedule);
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

	class Async extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(4000);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
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

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (pos.size() == 0) {
			setSwypeListener();
			@SuppressWarnings("unchecked")
			HashMap<String, String> map = (HashMap<String, String>) lv.getItemAtPosition(arg2);
			String s = map.get("titre");
			currentId = Integer.parseInt(s.split("N�")[1].split(" ")[0]);

			Intent intent = new Intent(HistoryActivity.this, VisuPagerActivity.class);
			Bundle objetbunble = new Bundle();
			objetbunble.putString("id", "" + HistoryActivity.currentId);
			intent.putExtras(objetbunble);
			startActivity(intent);
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
			String s = map.get("titre");
			int id = Integer.parseInt(s.split("N�")[1].split(" ")[0]);
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
				for (int num : pos) {
					HashMap<String, String> map = (HashMap<String, String>) listItem.get(num);
					String s = map.get("titre");
					currentId = Integer.parseInt(s.split("N�")[1].split(" ")[0]);
					ToolsBDD.getInstance(getApplicationContext()).removePartie(HistoryActivity.currentId);
				}

				Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
				startActivity(intent);
				finish();

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
		cards.setVisibility(View.GONE);

		chartView.setVisibility(View.VISIBLE);
		chartView.setGridLineColor(isDark ? Color.rgb(19, 133, 173) : Color.BLACK);
		LinearSeries seriesBlue = new LinearSeries();
		seriesBlue.setLineColor(Color.rgb(0, 148, 255));
		seriesBlue.setLineWidth(4);

		LinearSeries seriesRed = new LinearSeries();
		seriesRed.setLineColor(Color.RED);
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

	public static class ValueLabelAdapter extends LabelAdapter {
		public enum LabelOrientation {
			HORIZONTAL, VERTICAL
		}

		private Context mContext;
		private LabelOrientation mOrientation;

		public ValueLabelAdapter(Context context, LabelOrientation orientation) {
			mContext = context;
			mOrientation = orientation;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView labelTextView;
			if (convertView == null) {
				convertView = new TextView(mContext);
			}

			labelTextView = (TextView) convertView;

			int gravity = Gravity.CENTER;
			if (mOrientation == LabelOrientation.VERTICAL) {
				if (position == 0) {
					gravity = Gravity.BOTTOM | Gravity.RIGHT;
				} else if (position == getCount() - 1) {
					gravity = Gravity.TOP | Gravity.RIGHT;
				} else {
					gravity = Gravity.CENTER | Gravity.RIGHT;
				}
			} else if (mOrientation == LabelOrientation.HORIZONTAL) {
				if (position == 0) {
					gravity = Gravity.CENTER | Gravity.LEFT;
				} else if (position == getCount() - 1) {
					gravity = Gravity.CENTER | Gravity.RIGHT;
				}
			}

			labelTextView.setGravity(gravity);
			labelTextView.setPadding(8, 0, 8, 0);
			labelTextView.setText(String.format("%.1f", getItem(position)));

			return convertView;
		}
	}

	public void createCards() {
		cards.setSwipeable(true);
		cards.setVisibility(View.VISIBLE);

		cards.clearCards();

		lv.setVisibility(View.GONE);
		chartView.setVisibility(View.GONE);
		Cursor c = ToolsBDD.getInstance(this).getAllParties();

		if (c == null || c.getCount() == 0) {

		} else {
			c.moveToFirst();

			for (int i = 0; i < c.getCount(); i++) {
				String v = "N�" + c.getInt(0) + " - ";
				int n = c.getInt(1);
				if (n == MainActivity.BLUE_PLAYER) {
					v += getString(R.string.win);
				} else if (n == MainActivity.RED_PLAYER) {
					v += getString(R.string.loose);
				} else {
					v += getString(R.string.equal);
				}

				CardGame cg = new CardGame(c.getInt(1), c.getString(2), v, c.getInt(0), getWindowManager().getDefaultDisplay(), getApplicationContext());
				cg.setOnCardSwipedListener(this);
				final int idC = c.getInt(0);
				cg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(HistoryActivity.this, VisuPagerActivity.class);
						Bundle objetbunble = new Bundle();
						objetbunble.putString("id", "" + idC);
						intent.putExtras(objetbunble);
						startActivity(intent);
					}
				});
				cards.addCard(cg);

				c.moveToNext();
			}
		}

		cards.refresh();
	}

	@Override
	public void onCardSwiped(Card card, View layout) {
		if (card instanceof CardGame) {
			int id = ((CardGame) card).get_id();
			ToolsBDD.getInstance(getApplicationContext()).removePartie(id);
			saveId = id;
			saveWinner = ((CardGame) card).get_winner();
			saveDisposition = ((CardGame) card).get_disposition();
			saveFromCards = true;
			mUndoBarController.showUndoBar(false, getString(R.string.undobar_sample_message), null);
		}
	}

	int saveId = -1;
	int saveWinner = -1;
	String saveDisposition = "";
	boolean saveFromCards = false;

	@Override
	public void onUndo(Parcelable token) {

		ToolsBDD.getInstance(this).insertPartie(saveId, saveWinner, saveDisposition);
		if (saveFromCards)
			createCards();
		else {
			// createList();

			HashMap<String, String> map = new HashMap<String, String>();

			int n = saveWinner;
			if (n == MainActivity.BLUE_PLAYER) {
				map = new HashMap<String, String>();
				map.put("titre", "N�" + saveId + " - " + getString(R.string.win));
				map.put("description", getString(R.string.winb));
				map.put("img", String.valueOf(R.drawable.croix));
			} else if (n == MainActivity.RED_PLAYER) {
				map = new HashMap<String, String>();
				map.put("titre", "N�" + saveId + " - " + getString(R.string.loose));
				map.put("description", getString(R.string.winr));
				map.put("img", String.valueOf(R.drawable.cercle));
			} else {
				map = new HashMap<String, String>();
				map.put("titre", "N�" + saveId + " - " + getString(R.string.equal));
				map.put("description", getString(R.string.equaltry));
				map.put("img", String.valueOf(R.drawable.icon));
			}

			map.put("winner", n + "");
			map.put("disposition", saveDisposition);

			listItem.add(findCorrectPlace(map), map);
			mSchedule.notifyDataSetChanged();
		}
	}

	private int findCorrectPlace(HashMap<String, String> map) {

		int res = 0;
		int toinsertid = Integer.parseInt(map.get("titre").split("N�")[1].split(" ")[0]);
		for (res = 0; res < listItem.size(); res++) {
			String titleCurrent = listItem.get(res).get("titre");
			int currentid = Integer.parseInt(titleCurrent.split("N�")[1].split(" ")[0]);
			if (toinsertid <= currentid) {
				break;
			}
		}
		return res;
	}
}
