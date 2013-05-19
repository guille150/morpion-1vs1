package fr.mathis.morpion;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.tools.ColorHolder;

public class PreferencesActivity extends SherlockActivity implements OnCheckedChangeListener {

	private CheckBox cbSaveEqual;
	private CheckBox cbTheme;
	private Spinner spBlue;
	private Spinner spRed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		boolean d = mgr.getBoolean("isDark", false);

		if (d)
			super.setTheme(R.style.AppThemeDark);
		super.onCreate(savedInstanceState);

		final boolean save = mgr.getBoolean("save", true);
		final boolean isDark = mgr.getBoolean("isDark", false);
		if (isDark)
			super.setTheme(R.style.AppThemeDark);
		setContentView(isDark ? R.layout.menudark : R.layout.menu);

		cbSaveEqual = (CheckBox) findViewById(R.id.checkBoxSaveEqual);
		cbTheme = (CheckBox) findViewById(R.id.checkBoxSelectTheme);
		spBlue = (Spinner) findViewById(R.id.spinner1);
		spRed = (Spinner) findViewById(R.id.spinner2);

		if (save) {
			cbSaveEqual.setChecked(true);
		}

		if (isDark)
			cbTheme.setChecked(true);

		spBlue.setAdapter(new MySpinnerAdapter(this, ColorHolder.getAllColor()));
		spBlue.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.BLUE_PLAYER, arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.BLUE_PLAYER, 0);
			}
		});
		spBlue.setSelection(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.BLUE_PLAYER));

		spRed.setAdapter(new MySpinnerAdapter(this, ColorHolder.getAllColor()));
		spRed.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.RED_PLAYER, arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				ColorHolder.getInstance(getApplicationContext()).save(MainActivity.RED_PLAYER, 9);
			}

		});
		spRed.setSelection(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.RED_PLAYER));

		cbSaveEqual.setOnCheckedChangeListener(this);
		cbTheme.setOnCheckedChangeListener(this);
		
		getSupportActionBar().setTitle(R.string.menupref);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
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
			setResult(RESULT_OK);
			finish();
		}
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
