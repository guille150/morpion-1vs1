package fr.mathis.morpion;

import group.pals.android.lib.ui.lockpattern.widget.LockPatternView;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.Cell;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.DisplayMode;
import group.pals.android.lib.ui.lockpattern.widget.LockPatternView.OnPatternListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

import com.actionbarsherlock.app.SherlockActivity;

public class PaternActivityDialog extends SherlockActivity implements OnPatternListener {

	boolean isDark;
	List<Cell> cells;
	LockPatternView patern;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(this);
		isDark = mgr.getBoolean("isDark", false);

		if (isDark)
			super.setTheme(R.style.Alp_BaseTheme_Dialog_Dark);

		setContentView(isDark ? R.layout.paterndark : R.layout.patern);

		patern = (LockPatternView) findViewById(R.id.lockPatternView1);
		patern.setOnPatternListener(this);
		cells = new ArrayList<LockPatternView.Cell>();

		Random r = new Random();
		;
		
		switch (r.nextInt(5)) {
		case 0:
			cells.add(Cell.of(0,0 ));
			cells.add(Cell.of(1,0 ));
			cells.add(Cell.of(2,0 ));
			cells.add(Cell.of(2,1 ));
			cells.add(Cell.of(1,1 ));
			break;
		case 1:
			cells.add(Cell.of(1,1 ));
			cells.add(Cell.of(2,2 ));
			cells.add(Cell.of(1,2 ));
			cells.add(Cell.of(0,2 ));
			cells.add(Cell.of(0,1 ));
			break;
		case 2:
			cells.add(Cell.of(0,0 ));
			cells.add(Cell.of(0,1 ));
			cells.add(Cell.of(1,1 ));
			cells.add(Cell.of(1,0 ));
			break;
		case 3:
			cells.add(Cell.of(0,0 ));
			cells.add(Cell.of(1,1));
			cells.add(Cell.of(2,2 ));
			cells.add(Cell.of(2,1 ));
			cells.add(Cell.of(2,0 ));
			break;
		case 4:
			cells.add(Cell.of(2,0 ));
			cells.add(Cell.of(1,0 ));
			cells.add(Cell.of(0,1 ));
			cells.add(Cell.of(0,2 ));
			break;
		default:
			break;
		}
		
		patern.setPattern(DisplayMode.Animate, cells);
		patern.setTactileFeedbackEnabled(true);
	}

	@Override
	public void onPatternStart() {

	}

	@Override
	public void onPatternCleared() {

	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {

	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		boolean succes = true;
		if (pattern.size() == cells.size()) {
			for (int i = 0; i < pattern.size() && i < cells.size(); i++) {
				if (pattern.get(i).getColumn() == cells.get(i).getColumn() && pattern.get(i).getRow() == cells.get(i).getRow()) {

				} else {
					succes = false;
					break;
				}
			}
		} else {
			succes = false;
		}

		patern.clearPattern();
		if (succes) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

}
