package com.fourtime.dialog;

import net.simonvt.numberpicker.NumberPicker;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.utils.Utils;

public class PickerDialog extends android.support.v4.app.DialogFragment {

	public static final int TIME_PICKER = 0;
	public static final int NUMBER_PICKER = 1;

	private static final String TYPE = "type", PICKER_TITLE = "pick_title";
	private static final String INIT_TIME = "time";
	private static final String MIN_VAL = "min_val", MAX_VAL = "max_val",
			SELECTED = "selected";

	public interface OnPickerValueSelectedListener {
		public void onValueSet(long time);
	}

	private static OnPickerValueSelectedListener listener;
	private NumberPicker hrPick, minPick, secPick, numPick;
	private TextView btnDone, btnCancel;

	public static PickerDialog getTimeInstance(OnPickerValueSelectedListener l, 
			long initTime) {
		listener = l;
		PickerDialog dialog = new PickerDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(TYPE, TIME_PICKER);
		bundle.putLong(INIT_TIME, initTime);
		dialog.setArguments(bundle);
		return dialog;
	}

	public static PickerDialog getNumberPickerInstance(OnPickerValueSelectedListener l, String title,
			int minVal, int maxVal,
			int selectedVal) {
		listener = l;
		PickerDialog dialog = new PickerDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(TYPE, NUMBER_PICKER);
		bundle.putString(PICKER_TITLE, title);
		bundle.putInt(MIN_VAL, minVal);
		bundle.putInt(MAX_VAL, maxVal);
		bundle.putInt(SELECTED, selectedVal);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		dialog.setCancelable(false);
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int type = getArguments().getInt(TYPE);

		View view;
		
		if (type == TIME_PICKER) {
			view = inflater.inflate(R.layout.time_picker_new, null);
			hrPick = (NumberPicker) view.findViewById(R.id.picker1);
			minPick = (NumberPicker) view.findViewById(R.id.picker2);
			secPick = (NumberPicker) view.findViewById(R.id.picker3);
		} else {
			view = inflater.inflate(R.layout.num_picker_new, null);
			numPick = (NumberPicker) view.findViewById(R.id.picker1);
			((TextView) view.findViewById(R.id.txtPickTitle)).setText(getArguments().getString(PICKER_TITLE));
		}

		setupDefaults();
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		dialog.setContentView(view, lp);

		btnDone = (TextView) view.findViewById(R.id.btnDone);
		btnCancel = (TextView) view.findViewById(R.id.btnCancel);
		
		btnDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long value = 0;
				if (getArguments().getInt(TYPE) == TIME_PICKER) {
					int value1 = hrPick.getValue();
					int value2 = minPick.getValue();
					int value3 = secPick.getValue();
					value = (value1 * (60 * 60 * 1000))
							+ (value2 * (60 * 1000)) + (value3 * 1000);
					
					if(value <= 0){
						Utils.showAlert(getActivity(), "Alert", "Please select a valid time.");
						return;
					}
					
				} else {
					value = numPick.getValue();
				}
				listener.onValueSet(value);
				dismiss();
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return dialog;
	}

	private void setupDefaults() {
		Bundle bundle = getArguments();
		int type = bundle.getInt(TYPE);

		if (type == TIME_PICKER) {
			hrPick.setMinValue(0);
			hrPick.setFocusable(true);
			hrPick.setFocusableInTouchMode(true);
			hrPick.setMaxValue(23);
			minPick.setMinValue(0);
			minPick.setFocusable(true);
			minPick.setFocusableInTouchMode(true);
			minPick.setMaxValue(59);
			secPick.setMinValue(0);
			secPick.setFocusable(true);
			secPick.setFocusableInTouchMode(true);
			secPick.setMaxValue(59);

			long initTime = bundle.getLong(INIT_TIME);
			if (initTime > 0) {
				int seconds = (int) (initTime / 1000);
				int minutes = seconds / 60;
				int hours = minutes / 60;
				minutes = minutes % 60;
				seconds = seconds % 60;
				hrPick.setValue(hours);
				minPick.setValue(minutes);
				secPick.setValue(seconds);
			}

		} else if (type == NUMBER_PICKER) {
			numPick.setFocusable(true);
			numPick.setFocusableInTouchMode(true);
			numPick.setMinValue(bundle.getInt(MIN_VAL));
			numPick.setMaxValue(bundle.getInt(MAX_VAL));
			numPick.setValue(bundle.getInt(SELECTED));
		}
	}
}
