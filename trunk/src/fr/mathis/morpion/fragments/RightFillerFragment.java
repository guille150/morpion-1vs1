package fr.mathis.morpion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import fr.mathis.morpion.R;

public class RightFillerFragment extends SherlockFragment {

	public static RightFillerFragment newInstance() {
		RightFillerFragment fragment = new RightFillerFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.right_filler, null);
		return v;
	}
	
}
