package com.fourtime.home;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.advinterval.AdvintervalActivity;
import com.fourtime.constants.Constants;
import com.fourtime.countdown.CountDownActivity;
import com.fourtime.interval.IntervalActivity;
import com.fourtime.playlist.MusicActivity;
import com.fourtime.service.Adv_IntervalService;
import com.fourtime.service.CountdownService;
import com.fourtime.service.IntervalService;
import com.fourtime.service.StandardService;
import com.fourtime.service.TabataService;
import com.fourtime.settings.SettingsActivity;
import com.fourtime.standard.StandardTimerActivity;
import com.fourtime.tabata.TabataActivity;
import com.fourtime.utils.SoundEffects;

public class MainActivity extends Activity implements OnClickListener {

	private ImageView standardBtn, countdownBtn, advBtn, intervalBtn, tabataBtn;
	private TextView txtMusic, txtSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPortraitOrientation();
		setContentView(R.layout.main_screen);

		init();
		setupDefaults();
		setupEvents();
	}

	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}

	private void init(){		
		standardBtn=(ImageView)findViewById(R.id.standardBtn);
		countdownBtn=(ImageView)findViewById(R.id.countdownBtn);
		advBtn=(ImageView)findViewById(R.id.advBtn);
		intervalBtn=(ImageView)findViewById(R.id.intervalBtn);
		tabataBtn=(ImageView)findViewById(R.id.tabataBtn);
		txtMusic=(TextView)findViewById(R.id.txtMusic);
		txtSettings=(TextView)findViewById(R.id.txtSettings);	
	}

	private void setupDefaults(){
		if(Constants.tf_DIGITAL == null){
			Constants.tf_DIGITAL = Typeface.createFromAsset(getAssets(), "fonts/digital_regular.ttf");
			Constants.tf_HELVETICA_BOLD = Typeface.createFromAsset(getAssets(), "fonts/helvetica_bold.ttf");
			Constants.tf_HELVETICA_MEDIUM = Typeface.createFromAsset(getAssets(), "fonts/helvetica_medium.otf");
		}
		Constants.soundFX = new SoundEffects(this);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
		txtSettings.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtMusic.setTypeface(Constants.tf_HELVETICA_MEDIUM);
	}

	private void setupEvents(){
		standardBtn.setOnClickListener(this);
		countdownBtn.setOnClickListener(this);
		advBtn.setOnClickListener(this);
		intervalBtn.setOnClickListener(this);
		tabataBtn.setOnClickListener(this);
		txtMusic.setOnClickListener(this);
		txtSettings.setOnClickListener(this);		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopAllServices();

		if(Constants.mediaPlayer.isPlaying()){
			Constants.mediaPlayer.stop();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.standardBtn:
			Intent i=new Intent(this,StandardTimerActivity.class);
			startActivity(i);
			break;
		case R.id.countdownBtn:
			Intent i1=new Intent(this,CountDownActivity.class);
			startActivity(i1);
			break;
		case R.id.advBtn:
			startActivity(new Intent(this, AdvintervalActivity.class));
			break;
		case R.id.intervalBtn:
			startActivity(new Intent(this, IntervalActivity.class));
			break;
		case R.id.tabataBtn:
			Intent intent = new Intent(this, TabataActivity.class);
			startActivity(intent);
			break;
		case R.id.txtMusic:
			startActivity(new Intent(this, MusicActivity.class));
			break;

		case R.id.txtSettings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;					
		}
	}

	private void stopAllServices(){
		stopService(new Intent(this, StandardService.class));
		stopService(new Intent(this, CountdownService.class));
		stopService(new Intent(this, Adv_IntervalService.class));
		stopService(new Intent(this, IntervalService.class));
		stopService(new Intent(this, TabataService.class));
	}

}
