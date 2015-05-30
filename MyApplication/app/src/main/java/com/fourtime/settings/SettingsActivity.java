package com.fourtime.settings;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.fourtime.preference.SharedPersistance;

public class SettingsActivity extends Activity {

	private RelativeLayout aboutLayout, preparationTimeLayout;
	private TextView emailSupport, rate4Time, preparationTimeSec, txtArmory;
	private Button doneButton;
	private SeekBar volumeSeekBar;

	private SharedPersistance sharedPreferences;

	private int preparationTimeFlag=0,index;
	private String preparationText;
	private AlertDialog alert = null; 
	private String[] preparationArray;
	private int preparationTime = -1;
	private boolean isChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int layoutRes = R.layout.settings_dialog;
		if(!Constants.isTablet){			
			super.setTheme(android.R.style.Theme_NoTitleBar);	
			layoutRes = R.layout.settings;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void init(){
		aboutLayout=(RelativeLayout)findViewById(R.id.aboutLayout);
		preparationTimeLayout=(RelativeLayout)findViewById(R.id.preparationTimeLayout);
		volumeSeekBar=(SeekBar)findViewById(R.id.volumeSeekBar);

		emailSupport=(TextView)findViewById(R.id.emailSupportText);
		rate4Time=(TextView)findViewById(R.id.rate4Time);
		preparationTimeSec=(TextView)findViewById(R.id.preparationTimeSec);
		txtArmory = (TextView)findViewById(R.id.txtArmory);
		doneButton=(Button)findViewById(R.id.doneBtn);

		sharedPreferences=new SharedPersistance(this);

		preparationArray=new String[60];
		for(int i=0;i<=59;i++){
			preparationArray[i]= i + " secs";
		}

		volumeSeekBar.setMax(100);
		volumeSeekBar.setProgress(sharedPreferences.getTimerVolume());
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
	}

	private void setupDefaults(){
		index = sharedPreferences.getPreparationTime();
		preparationTime = index;
		preparationTimeSec.setText(preparationArray[index]);
	}
	
	private void saveItems(){
		sharedPreferences.setPreparationTime(preparationTime);
		sharedPreferences.setTimerVolume(volumeSeekBar.getProgress());
		if(Constants.soundFX != null){
			Constants.soundFX.resetTimerVolume(SettingsActivity.this);
		}
	}

	private void setupEvents(){
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveItems();
				finish();				
			}
		});

		aboutLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent =new Intent(SettingsActivity.this,AboutUsActivity.class);
				startActivity(intent);
			}
		});
		
		txtArmory.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				
			}
		});

		preparationTimeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPreparationTimeAlert();
			}
		});

		emailSupport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				Intent emailIntent=new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/html");
				emailIntent.putExtra(Intent.EXTRA_EMAIL,"supportDreamWorkshop@me.com");
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Support");
				emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + " Support");
				startActivity(Intent.createChooser(emailIntent,"Share"));
			}
		});

		rate4Time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Intent intent =new Intent(SettingsActivity.this,MusicActivity.class);
				startActivity(intent);*/
			}
		});	

		volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
		
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				//audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
				isChanged = true;
			}
		});

	}

	private void openPreparationTimeAlert(){
		AlertDialog.Builder methodAlert = new AlertDialog.Builder(this);
		methodAlert.setTitle(getResources().getString(R.string.preparationtime));

		int pos = 0;
		for(int i=0;i<preparationArray.length;i++){
			if(Arrays.asList(preparationArray[i]).contains(preparationTimeSec.getText().toString())){
				pos = i;
				preparationTime = i;
			}
		}
		methodAlert.setSingleChoiceItems(preparationArray, pos, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				preparationText = preparationArray[item];
				preparationTimeFlag = 1;
				index = item;
			}
		});
		methodAlert.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if(preparationTimeFlag == 0){
					preparationText=preparationArray[0].toString();
				}
				preparationTime = index;
				preparationTimeSec.setText(preparationText);
				isChanged = true;
				//sharedPreferences.setPreparationTime(index);
			}
		});
		methodAlert	.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				alert.dismiss();
			}
		});
		alert = methodAlert.create();
		alert.show(); 
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