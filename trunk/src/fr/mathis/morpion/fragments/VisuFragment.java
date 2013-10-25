package fr.mathis.morpion.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.mathis.morpion.MainActivity;
import fr.mathis.morpion.R;
import fr.mathis.morpion.tools.ToolsBDD;
import fr.mathis.morpion.views.GameView;

public class VisuFragment extends SherlockFragment {

	private static final int MENU_SHARE = 0;
	int id = 0;
	int w = 0;
	ImageButton[][] tabIB;
	int[][] tabVal;
	View v;
	private boolean isDark;

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

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(getActivity());
		isDark = mgr.getBoolean("isDark", false);

		id = getArguments().getInt("id");
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_SHARE, 0, R.string.s59).setIcon(isDark ? R.drawable.ic_action_sharedark : R.drawable.ic_action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
		View viewToPrint = v.findViewById(R.id.gameView1);
		Bitmap bmpToShare = getBitmapFromView(viewToPrint);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		String url = Images.Media.insertImage(getActivity().getContentResolver(), bmpToShare, "share", null);
		if (url == null) {
			Toast.makeText(getActivity(), R.string.s60, Toast.LENGTH_SHORT).show();
		} else {
			share.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
			startActivity(Intent.createChooser(share, getString(R.string.sharewith)));
		}
	}

	public static Bitmap getBitmapFromView(View view) {
		Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		Drawable bgDrawable = view.getBackground();
		if (bgDrawable != null)
			bgDrawable.draw(canvas);
		else
			canvas.drawColor(Color.WHITE);
		view.draw(canvas);
		return returnedBitmap;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		v = inflater.inflate(R.layout.visu2, null);
		String resultat = ToolsBDD.getInstance(getActivity()).getResultat(id);

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
		gv.setDark(isDark);
		gv.setAlignement(GameView.STYLE_CENTER_BOTH);
		gv.setValues(val, MainActivity.BLUE_PLAYER);
		gv.setShowWinner(true);

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
