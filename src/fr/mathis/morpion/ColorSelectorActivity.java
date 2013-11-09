package fr.mathis.morpion;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;

import fr.mathis.morpion.tools.ColorHolder;
import fr.mathis.morpion.tools.Tools;

public class ColorSelectorActivity extends SherlockActivity {

	LinearLayout[] colors;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.AppThemeDialogDark);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(isDark ?  R.layout.activity_colorselectordark : R.layout.activity_colorselector);

		final int playerId = getIntent().getExtras().getInt("player");

		colors = new LinearLayout[12];

		colors[0] = (LinearLayout) findViewById(R.id.colorSelector1);
		colors[1] = (LinearLayout) findViewById(R.id.colorSelector2);
		colors[2] = (LinearLayout) findViewById(R.id.colorSelector3);
		colors[3] = (LinearLayout) findViewById(R.id.colorSelector4);
		colors[4] = (LinearLayout) findViewById(R.id.colorSelector5);
		colors[5] = (LinearLayout) findViewById(R.id.colorSelector6);
		colors[6] = (LinearLayout) findViewById(R.id.colorSelector7);
		colors[7] = (LinearLayout) findViewById(R.id.colorSelector8);
		colors[8] = (LinearLayout) findViewById(R.id.colorSelector9);
		colors[9] = (LinearLayout) findViewById(R.id.colorSelector10);
		colors[10] = (LinearLayout) findViewById(R.id.colorSelector11);
		colors[11] = (LinearLayout) findViewById(R.id.colorSelector12);

		final int currentColorIndex = ColorHolder.getInstance(getApplicationContext()).getColorIndex(playerId);

		for (int i = 0; i < 12; i++) {
			ShapeDrawable circleBlue = new ShapeDrawable(new OvalShape());
			circleBlue.getPaint().setColor(Color.parseColor(ColorHolder.getAllColor().get(i)));
			circleBlue.setBounds(0, 0, Tools.convertDpToPixel(36), Tools.convertDpToPixel(36));

			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				colors[i].getChildAt(0).setBackgroundDrawable(circleBlue);
			} else {
				colors[i].getChildAt(0).setBackground(circleBlue);
			}

			final int finalIndex = i;

			colors[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((LinearLayout)colors[currentColorIndex].getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
					((LinearLayout)colors[finalIndex].getChildAt(0)).getChildAt(0).setVisibility(View.VISIBLE);
					ColorHolder.getInstance(getApplicationContext()).save(playerId, finalIndex);
					finish();
				}
			});

			if (currentColorIndex == i) {

				((LinearLayout)colors[i].getChildAt(0)).getChildAt(0).setVisibility(View.VISIBLE);
			}
		}

	}

}
