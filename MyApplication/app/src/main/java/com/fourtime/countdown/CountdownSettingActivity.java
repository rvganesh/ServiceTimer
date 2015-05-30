package com.fourtime.countdown;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.adapter.SettingsAdapter;
import com.fourtime.bean.Item;
import com.fourtime.constants.Constants;
import com.fourtime.dialog.PickerDialog;
import com.fourtime.dialog.PickerDialog.OnPickerValueSelectedListener;
import com.fourtime.preference.CountdownPrefrences;
import com.fourtime.utils.Utils;

public class CountdownSettingActivity extends FragmentActivity{
	private Button btnDone;
	private ListView listView;
	private ArrayList<Item> items;
	private SettingsAdapter adapter;
	private int selectedIndex = ListView.INVALID_POSITION;
	private boolean isChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.tabata_settings_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.tabata_settings;
		}
		super.onCreate(savedInstanceState);
		
		requestPortraitOrientation();	
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layoutRes);

		init();
		setupDefaults();
		setupEvents();
	}

	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}

	@Override
	public void onBackPressed() {
		if(isChanged){
			showConfirmationAlert();
		}else{
			finish();
		}
	}

	private void init(){
		btnDone = (Button)findViewById(R.id.btnDone);
		listView = (ListView)findViewById(R.id.listTabataSettings);	
		((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.countdown));
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
	}

	private void setupDefaults(){
		items = getItems();
		adapter = new SettingsAdapter(this, 0, items);
		listView.setAdapter(adapter);
	}

	private void setupEvents(){			
		btnDone.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				saveItems();
				finish();
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				selectedIndex = pos;
				PickerDialog dialog = null;
				Item item = (Item)parent.getItemAtPosition(pos);

				if(selectedIndex == 0){
					dialog = PickerDialog.getTimeInstance(onTimeSelected, item.original);
				}

				dialog.show(getSupportFragmentManager(), "tag");
			}
		});
	}

	private ArrayList<Item> getItems(){
		ArrayList<Item> _items = new ArrayList<Item>();
		CountdownPrefrences prefs = new CountdownPrefrences(this);
		_items.add(new Item("Work", prefs.getInterval(),Utils.format(prefs.getInterval())));
		return _items;
	}


	private void saveItems(){
		CountdownPrefrences  prefs = new CountdownPrefrences (this);
		for(int i=0; i<adapter.getCount(); i++){
			final Item item = adapter.getItem(i);

			switch(i){

			case 0:
				prefs.setInterval(item.original);
				break;
			}
		}
		setResult(RESULT_OK);
	}

	private OnPickerValueSelectedListener onTimeSelected = new OnPickerValueSelectedListener() {		
		@Override
		public void onValueSet(long time) {
			items.get(selectedIndex).original = time;			
			items.get(selectedIndex).value = Utils.format(time);
			adapter.notifyDataSetChanged();
			isChanged = true;		
		}
	};


	private void showConfirmationAlert(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you want to save the changes?");
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveItems();
				finish();
			}
		});

		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		builder.show();
	}

}
