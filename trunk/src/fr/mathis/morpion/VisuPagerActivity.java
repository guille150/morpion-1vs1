package fr.mathis.morpion;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
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
import com.slidingmenu.lib.SlidingMenu;
import com.viewpagerindicator.UnderlinePageIndicator;

import fr.mathis.morpion.tools.ToolsBDD;

public class VisuPagerActivity extends SherlockFragmentActivity implements OnPageChangeListener {
	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static int NUM_PAGES = 10;
	private ArrayList<Integer> indexs;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	boolean isDark;
	private SlidingMenu menu;

	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);
		
		if(isDark)
		{
			//super.setTheme(R.style.AppThemeDark);
			setTheme(R.style.AppThemeDark);
		}
		initiActivity();
		
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

	private void initiActivity() {
		setContentView(R.layout.visupage);
		NUM_PAGES = ToolsBDD.getInstance(this).getNbPartie();
		indexs = new ArrayList<Integer>();
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setPageMargin((int)convertDpToPixel(9,this));
		mPager.setPageMarginDrawable(isDark ? R.drawable.lineblue : R.drawable.linegraypager);
		UnderlinePageIndicator titleIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
		titleIndicator.setViewPager(mPager);

		int premsIndex = Integer.parseInt(this.getIntent().getStringExtra("id"));
		int pos = 0;
		Cursor c = ToolsBDD.getInstance(getApplicationContext()).getAllParties();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			if (c.getInt(0) == premsIndex)
				pos = i;
			c.moveToNext();
		}

		mPager.setCurrentItem(pos);
		indexs.add(pos);
		getSupportActionBar().setTitle(getString(R.string.resume) + premsIndex);
		int n = ToolsBDD.getInstance(this).getWinner(premsIndex);
		if (n == MainActivity.BLUE_PLAYER) {
			getSupportActionBar().setSubtitle(R.string.win);
		} else if (n == MainActivity.RED_PLAYER) {
			getSupportActionBar().setSubtitle(R.string.loose);
		} else {
			getSupportActionBar().setSubtitle(R.string.equal);
		}

		titleIndicator.setOnPageChangeListener(this);
	}

	@Override
	public void onBackPressed() {
	  if(indexs.size()>1) {
		  mPager.setCurrentItem(indexs.get(indexs.size()-2));
		  indexs.remove(indexs.size()-1);
		  if(indexs.size()>1)
			  indexs.remove(indexs.size()-1);
	  } else {
	    super.onBackPressed(); 
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
		public Fragment getItem(int position) {

			int id = 0;
			Cursor c = ToolsBDD.getInstance(getApplicationContext()).getAllParties();
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				if (i == position)
					id = c.getInt(0);
				c.moveToNext();
			}

			return VisuFragment.newInstance(id);
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		indexs.add(arg0);
		int id = 0;
		Cursor c = ToolsBDD.getInstance(getApplicationContext()).getAllParties();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			if (i == arg0)
				id = c.getInt(0);
			c.moveToNext();
		}

		getSupportActionBar().setTitle(getString(R.string.resume) + id);
		int n = ToolsBDD.getInstance(this).getWinner(id);
		if (n == MainActivity.BLUE_PLAYER) {
			getSupportActionBar().setSubtitle(R.string.win);
		} else if (n == MainActivity.RED_PLAYER) {
			getSupportActionBar().setSubtitle(R.string.loose);
		} else {
			getSupportActionBar().setSubtitle(R.string.equal);
		}

	}

}