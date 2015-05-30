package com.fourtime.tabata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.fourtime.result.ResultsActivity;
import com.fourtime.service.ServiceMessage;
import com.fourtime.service.TabataService;
import com.fourtime.utils.Utils;

public class TabataActivity extends Activity{
	private static final String TAG = "TabataActivity";
	private static final int SETTING_RESULT = 95;	

	private Messenger mService = null;
	private Messenger mMessenger = new Messenger(new MyMsgHandler());
	private boolean isServiceBound = false;

	private TextView txtStart, txtStop, txtReset;
	private FrameLayout btnResults;	
	private TextView txtTimer, txtRounds, txtTabata, txtRepsCount;
	private ImageView imgSettings;
	private boolean isTimerRunning = false;
	private boolean isCountDownRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPortraitOrientation();
		setContentView(R.layout.tabata_activity_new);
		Utils.loadAd(this);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && requestCode == SETTING_RESULT){
			if(mService != null){
				sendMessage(ServiceMessage.PREF_RESET);
				Log.e(TAG, "Pref reset");
			}
		}	
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindService();
	}



	private void startBindingService(){
		if(TabataService.isRunning()){					
			bindService();
		}else{
			startService(new Intent(this, TabataService.class));
			bindService();
		}
	}

	private void bindService(){
		bindService(new Intent(this, TabataService.class), mServiceConn, Context.BIND_AUTO_CREATE);
		isServiceBound = true;
	}

	private void unBindService(){
		if(isServiceBound){
			if(mService != null){						
				sendMessage(ServiceMessage.SERVICE_DISCONNECTED);
			}
			unbindService(mServiceConn);
			isServiceBound = false;
		}
	}

	private ServiceConnection mServiceConn = new ServiceConnection() {		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			sendMessage(ServiceMessage.SERVICE_CONNECTED);
		}
	};

	@SuppressLint("HandlerLeak")
	private class MyMsgHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){

			case ServiceMessage.INIT_COUNT_DOWN_PROGRESS:
				isCountDownRunning = true;
				setCountDown((int) msg.getData().getLong(TabataService.VALUE));
				txtRounds.setText("Rounds: " + msg.getData().getInt(TabataService.ROUNDS_COUNT));				
				setTabatasCount(msg.getData().getInt(TabataService.TABATA_COUNT));
				txtStop.setEnabled(false);
				setStartVisible(false);
				break;
				
			case ServiceMessage.INIT_TIMER_END:
				setTimerLable(msg.getData());
				int tabataCount = msg.getData().getInt(TabataService.TABATA_COUNT);
				setTabataCount(tabataCount);
				setStartVisible(true);
				txtStart.setEnabled(false);
				txtReset.setEnabled(true);
				txtTimer.setText("Stop!");
				break;
				
			case ServiceMessage.TIMER_PAUSED:
				setTimerValueExtra(msg.getData());
				setTabataCount(msg.getData().getInt(TabataService.TABATA_COUNT));
				enableSettings(false);
				break;

			case ServiceMessage.UPDATE_COUNTDOWN:
				isCountDownRunning = true;
				int value = (int)msg.getData().getLong(TabataService.VALUE);
				setCountDown(value);
				break;

			case ServiceMessage.UPDATE_TIMER_VALUE:
				isTimerRunning = true;
				long millisVal = msg.getData().getLong(TabataService.VALUE);
				setTimerValue(millisVal);
				break;

			case ServiceMessage.UPDATE_EXTRA_TIMER_VALUE:
				isTimerRunning = true;
				setTimerValueExtra(msg.getData());
				break;

			case ServiceMessage.TIMER_FINISHED:
				isTimerRunning = false;
				txtReset.setEnabled(true);
				setStartVisible(true);
				break;

			case ServiceMessage.DISPLAY_END:
				isTimerRunning = false;
				txtReset.setEnabled(true);
				txtTimer.setText("Stop!");
				setStartVisible(true);
				txtStart.setEnabled(false);
				break;

			case ServiceMessage.TIMER_STARTED:
				 setStartVisible(false);
				 txtStop.setEnabled(true);
				 txtReset.setEnabled(false);
				break;

			case ServiceMessage.SET_REPS_COUNT:
				setRepsCount(true);
				break;

			case ServiceMessage.INIT_TIMER_STARTED:
				isTimerRunning = true;
				updateUIAtInit(msg);
				break;

			case ServiceMessage.START_COUNTDOWN:
				isCountDownRunning = true;
				lockCountDown();
				txtStop.setEnabled(false);
				setStartVisible(false);
				break;

			case ServiceMessage.COUNTDOWN_FINISHED:
				isCountDownRunning = false;
				isTimerRunning = true;
				txtStop.setEnabled(true);
				break;

			case ServiceMessage.RESET:
				isCountDownRunning = false;
				isTimerRunning = false;
				reset(msg.getData());
				Log.e(TAG, "Reset Called");
				break;

			case ServiceMessage.UPDATE_TABATA_COUNT:
				setTabataCount(msg.arg1);
				break;
				
			case ServiceMessage.SEND_RESULT:
				addResult(msg.getData());
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	private void init(){
		txtStart = (TextView)findViewById(R.id.txtStart);
		imgSettings = (ImageView)findViewById(R.id.ivSettings);
		txtTimer = (TextView)findViewById(R.id.txtTime);
		txtRounds = (TextView)findViewById(R.id.txtRounds);
		txtTabata = (TextView)findViewById(R.id.txtTabatas);
		txtRepsCount = (TextView)findViewById(R.id.txtRepsCount);
		//repsLayout = (LinearLayout)findViewById(R.id.layoutCenter);
		txtStop = (TextView)findViewById(R.id.txtStop);
		txtReset = (TextView)findViewById(R.id.txtReset);
		btnResults = (FrameLayout)findViewById(R.id.btnResults);
		
		txtTimer.setTypeface(Constants.tf_DIGITAL);
		txtRepsCount.setTypeface(Constants.tf_DIGITAL);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
		
		txtStart.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtStop.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtReset.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		((TextView)findViewById(R.id.txtReps)).setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtRounds.setTypeface(Constants.tf_HELVETICA_BOLD);
		txtTabata.setTypeface(Constants.tf_HELVETICA_BOLD);
	}

	private void setupDefaults(){
		startBindingService();
		setRepsCount(true);
	}

	private void setupEvents() {
		txtRepsCount.setOnTouchListener(onRepsTap);

		txtReset.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(txtReset.isEnabled()){
					sendMessage(ServiceMessage.RESET);
					enableSettings(true);
					setRepsCount(true);					
				}
			}
		});

		txtStart.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(txtStart.isEnabled()){					
					startCountDown();
				}
			}
		});

		txtStop.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendMessage(ServiceMessage.STOP_TIMER);
				setStartVisible(true);
				txtStart.setEnabled(true);
				txtReset.setEnabled(true);
				//imgSettings.setEnabled(true);
			}
		});

		btnResults.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendMessage(ServiceMessage.SEND_RESULT);
			}
		});

		imgSettings.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(imgSettings.isEnabled()){
					onSettingsClicked();
				}
			}
		});
	}


	private OnTouchListener onRepsTap = new OnTouchListener() {		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setRepsCount(false);
				break;

			case MotionEvent.ACTION_UP:

				break;
			}
			return false;
		}
	};


	private void addResult(Bundle bundle){
		Intent intent = new Intent(this, ResultsActivity.class);
		intent.putParcelableArrayListExtra(TabataService.VALUE, bundle.getParcelableArrayList(TabataService.VALUE));
		intent.putExtra(ResultsActivity.RESULT_TYPE, ResultsActivity.TABATA_RESULT);
		startActivity(intent);
	}
	
	private void reset(Bundle bundle){
		long millis = bundle.getLong(TabataService.VALUE);
		int roundsCount = bundle.getInt(TabataService.ROUNDS_COUNT);
		int tabataCount = bundle.getInt(TabataService.TABATA_COUNT);

		setTimerValue(millis);
		txtRounds.setText("Rounds: " + roundsCount);
		setTabatasCount(tabataCount);
		setStartVisible(true);
		txtStart.setEnabled(true);
		setWorkColor();
		setRepsCount(true);
	}
	
	private void setStartVisible(boolean shouldVisible){
		if(shouldVisible){
			txtStart.setVisibility(View.VISIBLE);
			txtStop.setVisibility(View.INVISIBLE);
		}else{
			txtStart.setVisibility(View.INVISIBLE);
			txtStop.setVisibility(View.VISIBLE);
		}
	}

	private void setTabataCount(int count){
		txtTabata.setText("Tabata " + count);
	}
	
	private void setTabatasCount(int count){
		txtTabata.setText("Tabatas: " + count);
	}

	private void setTimerLable(Bundle bundle){
		int flag = bundle.getInt(TabataService.FLAG);
		int count = bundle.getInt(TabataService.COUNT);

		if(flag == TabataService.FLAG_WORK){
			txtRounds.setText("Work " + count);
			setWorkColor();
		}else if(flag == TabataService.FLAG_REST){
			txtRounds.setText("Rest " + count);		
			setRestColor();
		}
	}
	
	private void setWorkColor(){
		txtRounds.setTextColor(getResources().getColor(R.color.tc_green));
		txtTimer.setTextColor(getResources().getColor(R.color.tc_green));
	}
	
	private void setRestColor(){
		txtRounds.setTextColor(getResources().getColor(R.color.rest_color));
		txtTimer.setTextColor(getResources().getColor(R.color.rest_color));
	}
	
	private void enableSettings(boolean shouldEnable){
		imgSettings.setEnabled(shouldEnable);
	}

	private void setCountDown(int value){
		txtTimer.setText(getValue(value));
	}

	private void setTimerValue(long millis){
		txtTimer.setText(Utils.getFormattedValue(millis));
	}

	private void lockCountDown(){
		if(txtStart.isEnabled()){
			txtStart.setEnabled(false);
		}
		
		if(imgSettings.isEnabled()){
			enableSettings(false);
		}
		
		if(txtReset.isEnabled()){
			txtReset.setEnabled(false);
		}
	}

	private void updateUIAtInit(final Message msg){
		if(isCountDownRunning){
			lockCountDown();
		}else if(isTimerRunning){
			if(!txtStop.isEnabled()){
				txtStop.setEnabled(true);
			}
			setStartVisible(false);
			setTimerLable(msg.getData());			
		}
		
		if(isCountDownRunning || isTimerRunning){
			txtReset.setEnabled(false);
		}

		if(imgSettings.isEnabled()){
			enableSettings(false);
		}
	}


	private void setTimerValueExtra(Bundle bundle){
		long millis = bundle.getLong(TabataService.VALUE);
		int flag = bundle.getInt(TabataService.FLAG);
		int count = bundle.getInt(TabataService.COUNT);

		if(flag == TabataService.FLAG_WORK){
			//isWorkRunning = true;
			txtRounds.setText("Work " + count);
			txtRounds.setTextColor(getResources().getColor(R.color.tc_green));
			txtTimer.setTextColor(getResources().getColor(R.color.tc_green));
			
		}else if(flag == TabataService.FLAG_REST){
			//isWorkRunning = false;
			txtRounds.setText("Rest " + count);
			txtRounds.setTextColor(getResources().getColor(R.color.rest_color));
			txtTimer.setTextColor(getResources().getColor(R.color.rest_color));
		}

		txtTimer.setText(Utils.getFormattedValue(millis));
	}

	private void startCountDown(){
		sendMessage(ServiceMessage.START_COUNTDOWN);
	}	


	private void sendMessage(int MSG_TYPE){
		try {
			Message msg = Message.obtain(null, MSG_TYPE);
			msg.replyTo = mMessenger;
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void setRepsCount(boolean onlyUpdate){		
		if(onlyUpdate){
			txtRepsCount.setText(getValue(TabataService.getRepsCount()));
			return;
		}

		if(isTimerRunning){
			int count = TabataService.getRepsCount();
			count ++;
			txtRepsCount.setText(getValue(count));
			TabataService.setRepsCount(count);
		}
	}

	private void onSettingsClicked(){
		Intent intent = new Intent(this, TabataSettingsActivity.class);
		intent.putExtra(TabataSettingsActivity.SETTING_NAME, getString(R.string.tabata));
		startActivityForResult(intent, SETTING_RESULT);
	}

	private String getValue(int value){
		return String.valueOf(value);
	}

	/*private String getFormattedValue(long timeInMillis){
		long mSecs = timeInMillis / 100;
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;

		seconds = seconds % 60;
		mSecs = mSecs % 10;

		String time = "";

		time += minutes + ":";	
		time += seconds + ":" + mSecs;		
		return time;
	}*/

}
