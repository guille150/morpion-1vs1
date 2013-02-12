package fr.mathis.morpion;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fr.mathis.morpion.tools.ToolsBDD;

public class VisuFragment extends SherlockFragment {

	int id = 0;
	int w = 0;
	ImageButton[][] tabIB;
	View v;

	public static VisuFragment newInstance(int id) {
		VisuFragment fragment = new VisuFragment();
		Bundle args = new Bundle();
		args.putInt("id", id);
		fragment.setArguments(args);
		return fragment;
	}

	public VisuFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		id = getArguments().getInt("id");

		super.onCreate(savedInstanceState);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		v = inflater.inflate(R.layout.visu, null, false);

		String resultat = ToolsBDD.getInstance(getActivity()).getResultat(id);
		Display display = getActivity().getWindowManager().getDefaultDisplay();

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		w = metrics.widthPixels;
		if ((metrics.heightPixels) < w)
			w = (metrics.heightPixels);
		int ratio = 5;

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
			if (display.getRotation() == Surface.ROTATION_0)
				ratio = 3;
		} else {
			if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				ratio = 3;
		}

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

		tabIB = new ImageButton[3][3];
		tabIB[0][0] = (ImageButton) v.findViewById(R.id.imageButton1);
		tabIB[0][1] = (ImageButton) v.findViewById(R.id.imageButton2);
		tabIB[0][2] = (ImageButton) v.findViewById(R.id.imageButton3);
		tabIB[1][0] = (ImageButton) v.findViewById(R.id.imageButton4);
		tabIB[1][1] = (ImageButton) v.findViewById(R.id.imageButton5);
		tabIB[1][2] = (ImageButton) v.findViewById(R.id.imageButton6);
		tabIB[2][0] = (ImageButton) v.findViewById(R.id.imageButton7);
		tabIB[2][1] = (ImageButton) v.findViewById(R.id.imageButton8);
		tabIB[2][2] = (ImageButton) v.findViewById(R.id.imageButton9);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tabIB[i][j].setMinimumWidth((w) / ratio);
				tabIB[i][j].setMinimumHeight((w) / ratio);
				tabIB[i][j].setMaxWidth((w) / ratio);
				tabIB[i][j].setMaxHeight((w) / ratio);
				tabIB[i][j].setEnabled(false);

				if (val[i][j] == MainActivity.BLUE_PLAYER) {
					Drawable d = getResources().getDrawable(R.drawable.croix);
					tabIB[i][j].setImageDrawable(d);
				}
				if (val[i][j] == MainActivity.RED_PLAYER) {
					Drawable d = getResources().getDrawable(R.drawable.cercle);
					tabIB[i][j].setImageDrawable(d);
				}
			}
		}
		recalculateSize();

		return v;
	}

	public void recalculateSize() {
		final ScrollView sc = (ScrollView) v.findViewById(R.id.layoutswipe);
		ViewTreeObserver vto = sc.getViewTreeObserver();
		final Display display = getActivity().getWindowManager().getDefaultDisplay();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				ViewTreeObserver obs = sc.getViewTreeObserver();

				w = sc.getWidth();

				int h = sc.getHeight();

				if (h < w)
					w = h;
				int ratio = 3;

				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
					if (display.getRotation() == Surface.ROTATION_0)
						ratio = 3;
				} else {
					if (display.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
						ratio = 3;
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						tabIB[i][j].setMinimumWidth((w) / ratio);
						tabIB[i][j].setMinimumHeight((w) / ratio);
						tabIB[i][j].setMaxWidth((w) / ratio);
						tabIB[i][j].setMaxHeight((w) / ratio);
						tabIB[i][j].invalidate();
					}
				}

				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

}
