package fr.mathis.morpion;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.tools.ToolsBDD;

public class VisuFragment extends SherlockFragment {

	private static final int MENU_SHARE = 0;
	int id = 0;
	int w = 0;
	ImageButton[][] tabIB;
	int[][] tabVal;
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
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_SHARE, 0, R.string.share).setIcon(R.drawable.social_share2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case MENU_SHARE:
			generateImageThenShare();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void generateImageThenShare() {
		View viewToPrint = v.findViewById(R.id.screenshot);
		Bitmap bmpToShare = getBitmapFromView(viewToPrint);
				
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		String url = Images.Media.insertImage(getActivity().getContentResolver(), bmpToShare, "share", null);
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
		startActivity(Intent.createChooser(share, getString(R.string.sharewith)));
	}
	
	public static Bitmap getBitmapFromView(View view) {
	    Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(returnedBitmap);
	    Drawable bgDrawable =view.getBackground();
	    if (bgDrawable!=null) 
	        bgDrawable.draw(canvas);
	    else 
	        canvas.drawColor(Color.WHITE);
	    view.draw(canvas);
	    return returnedBitmap;
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
		tabVal = new int[3][3];
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
					tabVal[i][j] = MainActivity.BLUE_PLAYER;
				} else if (val[i][j] == MainActivity.RED_PLAYER) {
					Drawable d = getResources().getDrawable(R.drawable.cercle);
					tabIB[i][j].setImageDrawable(d);
					tabVal[i][j] = MainActivity.RED_PLAYER;
				} else {
					tabVal[i][j] = MainActivity.NONE_PLAYER;
				}
			}
		}
		recalculateSize();
		checkWinner();
		return v;
	}

	private void checkWinner() {
		if (tabVal[0][0] == tabVal[0][1] && tabVal[0][1] == tabVal[0][2] && tabVal[0][2] != MainActivity.NONE_PLAYER) {
			tabIB[0][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[0][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[0][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[1][0] == tabVal[1][1] && tabVal[1][1] == tabVal[1][2] && tabVal[1][2] != MainActivity.NONE_PLAYER) {
			tabIB[1][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[2][0] == tabVal[2][1] && tabVal[2][1] == tabVal[2][2] && tabVal[2][2] != MainActivity.NONE_PLAYER) {
			tabIB[2][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[0][0] == tabVal[1][0] && tabVal[1][0] == tabVal[2][0] && tabVal[2][0] != MainActivity.NONE_PLAYER) {
			tabIB[0][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[0][1] == tabVal[1][1] && tabVal[1][1] == tabVal[2][1] && tabVal[2][1] != MainActivity.NONE_PLAYER) {
			tabIB[0][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[0][2] == tabVal[1][2] && tabVal[1][2] == tabVal[2][2] && tabVal[2][2] != MainActivity.NONE_PLAYER) {
			tabIB[0][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[0][0] == tabVal[1][1] && tabVal[1][1] == tabVal[2][2] && tabVal[2][2] != MainActivity.NONE_PLAYER) {
			tabIB[0][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[2][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		} else if (tabVal[2][0] == tabVal[1][1] && tabVal[1][1] == tabVal[0][2] && tabVal[0][2] != MainActivity.NONE_PLAYER) {
			tabIB[2][0].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[1][1].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
			tabIB[0][2].setBackgroundResource(R.drawable.btn_default_normal_holo_light);
		}
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
