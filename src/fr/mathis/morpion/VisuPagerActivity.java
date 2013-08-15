package fr.mathis.morpion;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.ToolsBDD;

public class VisuPagerActivity extends SherlockFragmentActivity implements OnPageChangeListener {

	private static int NUM_PAGES = 10;
	private ArrayList<Integer> indexs;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	boolean isDark;
	PagerSlidingTabStrip tabs;
	private String defaultColor = "#666666";
	ArrayList<HashMap<String, String>> listItem;

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

		initiActivity();
	}

	private void initiActivity() {

		setContentView(isDark ? R.layout.visupagedark : R.layout.visupage);
		NUM_PAGES = ToolsBDD.getInstance(this).getNbPartie();
		indexs = new ArrayList<Integer>();

		int premsIndex = this.getIntent().getIntExtra("id", 0);
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
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setPageMargin((int) convertDpToPixel(9, this));
		mPager.setPageMarginDrawable(isDark ? R.drawable.lineblue : R.drawable.linegraypager);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setViewPager(mPager);
		tabs.setOnPageChangeListener(this);

		mPager.setCurrentItem(pos);
		indexs.add(pos);
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
	}

}