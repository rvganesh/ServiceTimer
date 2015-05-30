package com.fourtime.advinterval;

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
import android.widget.ToggleButton;

import com.fourtime.R;
import com.fourtime.adapter.SettingsAdapter;
import com.fourtime.bean.Item;
import com.fourtime.constants.Constants;
import com.fourtime.dialog.PickerDialog;
import com.fourtime.dialog.PickerDialog.OnPickerValueSelectedListener;
import com.fourtime.preference.AdvIntervalPreference;
import com.fourtime.utils.Utils;

public class AdvIntervalSettingsActivity extends FragmentActivity{
	private Button  btnDone;
	private ToggleButton toggLoopIntervals;
	private ListView listView;
	private ListView dynamicList;
	private ArrayList<Item> items;
	private ArrayList<Item> dynamicListItems;
	private SettingsAdapter adapter, dynamicAdapter;

	private int selectedIndex = ListView.INVALID_POSITION;
	private int dynamicListSelectedIndex=ListView.INVALID_POSITION;
	private AdvIntervalPreference prefs;
	private boolean isChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.adinterval_settings_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.adinterval_settings;
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
		if(isChanged || (prefs.isLoopInterval() != toggLoopIntervals.isChecked())){
			showConfirmationAlert();
		}else{
			finish();
		}
	}

	private void init(){
		btnDone = (Button)findViewById(R.id.btnDone);
		listView = (ListView)findViewById(R.id.listTabataSettings);	
		dynamicList=(ListView)findViewById(R.id.listTabataDynamic);
		toggLoopIntervals = (ToggleButton)findViewById(R.id.toggLoop);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);

		prefs = new AdvIntervalPreference(this);
	}

	private void setupDefaults(){
		toggLoopIntervals.setChecked(prefs.isLoopInterval());
		items = getItems();
		adapter = new SettingsAdapter(this, 0, items);
		listView.setAdapter(adapter);

		dynamicListItems=getDynamicListItems();
		dynamicAdapter = new SettingsAdapter(this, 0, dynamicListItems);
		dynamicList.setAdapter(dynamicAdapter);
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
			public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {
				selectedIndex = pos;
				PickerDialog dialog;
				Item item = (Item)parent.getItemAtPosition(pos);

				dialog = PickerDialog.getNumberPickerInstance(onTimeSelected, "Rounds", 1, 24, (int)item.original);
				dialog.show(getSupportFragmentManager(), "tag");
			}
		});

		dynamicList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
				dynamicListSelectedIndex = position;
				PickerDialog dialog;
				Item item = (Item)adapterView.getItemAtPosition(position);

				dialog = PickerDialog.getTimeInstance(onDynamicTimeSelected, item.original);
				dialog.show(getSupportFragmentManager(), "tag");
			}
		});
	}

	private ArrayList<Item> getItems(){
		ArrayList<Item> _items = new ArrayList<Item>();
		_items.add(new Item("Rounds", prefs.getRounds(), String.valueOf(prefs.getRounds())));
		return _items;
	}


	private ArrayList<Item> getDynamicListItems(){
		ArrayList<Item> _items =new ArrayList<Item>();

		if(!prefs.getRoundIntervals().equalsIgnoreCase("")){
			dynamicList.setVisibility(View.VISIBLE);
			String[] val=prefs.getRoundIntervals().split(",");

			if(val.length > 0){
				for(int i=0;i<val.length;i++){
					_items.add(new Item("Time for interval " + (i + 1) + ":", Long.parseLong(val[i].toString()), Utils.format(Long.parseLong(val[i].toString()))));	
				}
			}
		}	
		return _items;
	}

	private void saveItems(){
		for(int i=0; i<adapter.getCount(); i++){
			final Item item = adapter.getItem(i);			
			prefs.setRounds((int)item.original);
		}

		String dynamicListValue="";

		for(int i=0;i<dynamicAdapter.getCount();i++){
			Item item = dynamicAdapter.getItem(i);
			dynamicListValue=dynamicListValue+ Long.toString(item.original)+",";
		}

		if(!dynamicListValue.equalsIgnoreCase("")){
			dynamicListValue = dynamicListValue.substring(0,dynamicListValue.length()-1);
		}
		else {
			dynamicListValue="";
		}
		prefs.setRoundIntervals(dynamicListValue);
		prefs.setLoopInterval(toggLoopIntervals.isChecked());

		setResult(RESULT_OK);
	}

	private OnPickerValueSelectedListener onTimeSelected = new OnPickerValueSelectedListener() {		
		@Override
		public void onValueSet(long time) {
			items.get(selectedIndex).original = time;			
			items.get(selectedIndex).value = String.valueOf(time);
			adapter.notifyDataSetChanged();		
			generateListView((int)items.get(selectedIndex).original);
		}
	};

	private OnPickerValueSelectedListener onDynamicTimeSelected = new OnPickerValueSelectedListener() {		
		@Override
		public void onValueSet(long time) {
			dynamicListItems.get(dynamicListSelectedIndex).original = time;			
			dynamicListItems.get(dynamicListSelectedIndex).value = Utils.format(time);
			dynamicAdapter.notifyDataSetChanged();
			isChanged = true;
		}
	};


	private void generateListView(int count){
		long longVal = 60000;
		if(dynamicAdapter.getCount() == 0){
			dynamicList.setVisibility(View.VISIBLE);
			for(int i=0;i<count;i++){
				dynamicListItems.add(new Item("Time for interval " + (i + 1) + ":", longVal,Utils.format(longVal)));			
			}
		}
		else if(count > dynamicAdapter.getCount()){
			int value = (dynamicAdapter.getCount() + 1);
			for(int i = dynamicAdapter.getCount();i<count;i++){
				dynamicListItems.add(new Item("Time for interval " + value + ":", longVal,Utils.format(longVal)));	
				value=value+1;
			}

		}
		else if(count < dynamicAdapter.getCount()){		
			for(int i = dynamicAdapter.getCount() - 1; i >= count; i--){
				dynamicListItems.remove(i);
			}
		}

		dynamicAdapter.notifyDataSetChanged();
		isChanged = true;
	}

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
