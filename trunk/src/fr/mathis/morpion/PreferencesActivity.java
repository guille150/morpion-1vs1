package fr.mathis.morpion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.Tools;

public class PreferencesActivity extends SherlockActivity implements OnCheckedChangeListener {

	public static int CODE_COLORSELECTOR_BLUE = 0;
	public static int CODE_COLORSELECTOR_RED = 1;

	private CheckBox cbSaveEqual;
	private CheckBox cbTheme;
	private LinearLayout colorBlue;
	private LinearLayout colorRed;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
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
		colorBlue = (LinearLayout) findViewById(R.id.colorBlue);
		colorRed = (LinearLayout) findViewById(R.id.colorRed);

		ShapeDrawable circleBlue = new ShapeDrawable(new OvalShape());
		circleBlue.getPaint().setColor(Color.parseColor(ColorHolder.getAllColor().get(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.BLUE_PLAYER))));
		circleBlue.setBounds(0, 0, Tools.convertDpToPixel(48), Tools.convertDpToPixel(48));
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			colorBlue.setBackgroundDrawable(circleBlue);
		} else {
			colorBlue.setBackground(circleBlue);
		}

		ShapeDrawable circleRed = new ShapeDrawable(new OvalShape());
		circleRed.getPaint().setColor(Color.parseColor(ColorHolder.getAllColor().get(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.RED_PLAYER))));
		circleRed.setBounds(0, 0, Tools.convertDpToPixel(48), Tools.convertDpToPixel(48));

		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			colorRed.setBackgroundDrawable(circleRed);
		} else {
			colorRed.setBackground(circleRed);
		}

		if (save) {
			cbSaveEqual.setChecked(true);
		}

		if (isDark) {
			cbTheme.setChecked(true);
		}

		cbSaveEqual.setOnCheckedChangeListener(this);
		cbTheme.setOnCheckedChangeListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.checkBoxSelectThemeContainer).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cbTheme.setChecked(!cbTheme.isChecked());
			}
		});

		findViewById(R.id.checkBoxSaveEqualContainer).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cbSaveEqual.setChecked(!cbSaveEqual.isChecked());
			}
		});

		findViewById(R.id.spinner1Container).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(PreferencesActivity.this, ColorSelectorActivity.class);
				Bundle b = new Bundle();
				b.putInt("player", MainActivity.BLUE_PLAYER);
				i.putExtras(b);
				startActivityForResult(i, CODE_COLORSELECTOR_BLUE);
			}
		});

		findViewById(R.id.spinner2Container).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(PreferencesActivity.this, ColorSelectorActivity.class);
				Bundle b = new Bundle();
				b.putInt("player", MainActivity.RED_PLAYER);
				i.putExtras(b);
				startActivityForResult(i, CODE_COLORSELECTOR_BLUE);
			}
		});
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
			Toast.makeText(getApplicationContext(), R.string.s43, Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ShapeDrawable circleBlue = new ShapeDrawable(new OvalShape());
		circleBlue.getPaint().setColor(Color.parseColor(ColorHolder.getAllColor().get(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.BLUE_PLAYER))));
		circleBlue.setBounds(0, 0, Tools.convertDpToPixel(36), Tools.convertDpToPixel(36));

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			colorBlue.setBackgroundDrawable(circleBlue);
		} else {
			colorBlue.setBackground(circleBlue);
		}

		ShapeDrawable circleRed = new ShapeDrawable(new OvalShape());
		circleRed.getPaint().setColor(Color.parseColor(ColorHolder.getAllColor().get(ColorHolder.getInstance(getApplicationContext()).getColorIndex(MainActivity.RED_PLAYER))));
		circleRed.setBounds(0, 0, Tools.convertDpToPixel(36), Tools.convertDpToPixel(36));

		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			colorRed.setBackgroundDrawable(circleRed);
		} else {
			colorRed.setBackground(circleRed);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
