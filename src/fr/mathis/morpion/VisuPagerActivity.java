package fr.mathis.morpion;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.UnderlinePageIndicator;

import fr.mathis.morpion.tools.ToolsBDD;

public class VisuPagerActivity extends SherlockFragmentActivity implements OnPageChangeListener {
	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static int NUM_PAGES = 10;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.visupage);

		NUM_PAGES = ToolsBDD.getInstance(this).getNbPartie();

		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);

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