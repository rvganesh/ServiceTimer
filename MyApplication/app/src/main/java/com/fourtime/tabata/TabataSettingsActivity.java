package com.fourtime.tabata;

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
import com.fourtime.preference.TabataPreferences;
import com.fourtime.utils.Utils;

public class TabataSettingsActivity extends FragmentActivity{
	public static final String SETTING_NAME = "setting_name";
	private Button btnDone;
	private TextView txtActivityLable;
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
		txtActivityLable = (TextView)findViewById(R.id.txtTitle);

		String settingName = getIntent().getStringExtra(SETTING_NAME);
		txtActivityLable.setText(settingName);
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
				PickerDialog dialog;
				Item item = (Item)parent.getItemAtPosition(pos);

				if(selectedIndex <= 1){
					dialog = PickerDialog.getTimeInstance(onTimeSelected, item.original);
				}else{
					String title = pos == 2 ? "Rounds" : "Tabatas";
					dialog = PickerDialog.getNumberPickerInstance(onTimeSelected, title, 1, 20, (int)item.original);
				}

				dialog.show(getSupportFragmentManager(), "tag");
			}
		});
	}


	private ArrayList<Item> getItems(){
		ArrayList<Item> _items = new ArrayList<Item>();
		TabataPreferences prefs = new TabataPreferences(this);
		_items.add(new Item("Work", prefs.getWork(),Utils.format(prefs.getWork())));
		_items.add(new Item("Rest", prefs.getRest(), Utils.format(prefs.getRest())));
		_items.add(new Item("Rounds", prefs.getRounds(), String.valueOf(prefs.getRounds())));
		_items.add(new Item("Tabatas", prefs.getTabatas(), String.valueOf(prefs.getTabatas())));		
		return _items;
	}

	private void saveItems(){
		TabataPreferences prefs = new TabataPreferences(this);
		for(int i=0; i<adapter.getCount(); i++){
			final Item item = adapter.getItem(i);

			switch(i){
			case 0:
				prefs.setWork(item.original);
				break;
			case 1:
				prefs.setRest(item.original);
				break;
			case 2:
				prefs.setRounds((int)item.original);
				break;
			case 3:
				prefs.setTabatas((int)item.original);
				break;
			}
		}
		setResult(RESULT_OK);
	}

	private OnPickerValueSelectedListener onTimeSelected = new OnPickerValueSelectedListener() {		
		@Override
		public void onValueSet(long time) {
			items.get(selectedIndex).original = time;			
			if(selectedIndex > 1){
				items.get(selectedIndex).value = String.valueOf(time);
			}else{
				items.get(selectedIndex).value = Utils.format(time);
			}
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
