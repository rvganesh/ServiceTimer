package com.fourtime.interval;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.fourtime.result.ResultsActivity;
import com.fourtime.service.IntervalService;
import com.fourtime.service.ServiceMessage;
import com.fourtime.utils.Utils;

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

public class IntervalActivity extends Activity{
	private static final String TAG = "IntervalActivity";
	private static final int SETTING_RESULT = 95;	

	private Messenger mService = null;
	private Messenger mMessenger = new Messenger(new MyMsgHandler());
	private boolean isServiceBound = false;

	private TextView txtStart, txtStop, txtReset;
	private FrameLayout btnResults;
	private TextView txtTimer, txtRoundsCount, txtTotalRepsCount, txtRepsCount;
	private ImageView imgSettings;
	//private LinearLayout repsLayout;
	private boolean isTimerRunning = false;
	private boolean isCountDownRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPortraitOrientation();
		setContentView(R.layout.interval_new);
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
		if(IntervalService.isRunning()){					
			bindService();
		}else{
			startService(new Intent(this, IntervalService.class));
			bindService();
		}
	}

	private void bindService(){
		bindService(new Intent(this, IntervalService.class), mServiceConn, Context.BIND_AUTO_CREATE);
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
				updateUIAtInit(msg);
				setStartVisible(false);
				txtStop.setEnabled(false);
				break;

			case ServiceMessage.INIT_PUASED_TIMER:
				setTimerValue(msg.getData().getLong(IntervalService.VALUE));
				setRoundsCount(msg.getData());
				enableSettings(false);
				break;

			case ServiceMessage.UPDATE_COUNTDOWN:
				isCountDownRunning = true;
				int value = (int)msg.getData().getLong(IntervalService.VALUE);
				setCountDown(value);
				break;

			case ServiceMessage.UPDATE_TIMER_VALUE:
				isTimerRunning = true;
				long millisVal = msg.getData().getLong(IntervalService.VALUE);
				setTimerValue(millisVal);
				if(txtStop.getVisibility() != View.VISIBLE){
					setStartVisible(false);
					txtStop.setEnabled(true);
				}
				break;


			case ServiceMessage.TIMER_FINISHED:
				isTimerRunning = false;
				txtReset.setEnabled(true);
				setStartVisible(true);				
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
				setStartVisible(false);
				txtStop.setEnabled(false);
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
				break;

			case ServiceMessage.ROUNDS_COMPLETED:
				int roundsCompleted = msg.arg1;
				setRoundsCount(roundsCompleted);
				txtRepsCount.setText(getValue(msg.arg2));
				break;

			case ServiceMessage.SEND_RESULT:
				openResult(msg.getData());
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	private void init(){
		txtStart = (TextView)findViewById(R.id.txtStart);
		imgSettings = (ImageView)findViewById(R.id.imgSettings);
		txtTimer = (TextView)findViewById(R.id.txtTime);
		txtRoundsCount = (TextView)findViewById(R.id.txtRoundsCount);
		txtTotalRepsCount = (TextView)findViewById(R.id.txtTotalRepsCount);
		txtRepsCount = (TextView)findViewById(R.id.txtRepsCount);
		txtStop = (TextView)findViewById(R.id.txtStop);
		txtReset = (TextView)findViewById(R.id.txtReset);
		btnResults = (FrameLayout)findViewById(R.id.btnResults);		
		
		txtTimer.setTypeface(Constants.tf_DIGITAL);
		txtRepsCount.setTypeface(Constants.tf_DIGITAL);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
		txtStart.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtStop.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtReset.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtRoundsCount.setTypeface(Constants.tf_HELVETICA_BOLD);
		((TextView)findViewById(R.id.txtReps)).setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtTotalRepsCount.setTypeface(Constants.tf_HELVETICA_MEDIUM);
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
				}
			}
		});

		txtStart.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(txtStart.isEnabled()){					
					startCountDown();
					txtReset.setEnabled(false);
					//IntervalService.setRepsCount(0);
					//IntervalService.setTotalRepsCount(0);
					//txtReset.setEnabled(false);
					//setRoundsCount(0);
					//setRepsCount(true);
				}
			}
		});

		txtStop.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendMessage(ServiceMessage.STOP_TIMER);
				setStartVisible(true);
				txtStart.setEnabled(true);
				txtReset.setEnabled(false);
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


	private void reset(Bundle bundle){
		long millis = bundle.getLong(IntervalService.VALUE);
		int roundsCount = bundle.getInt(IntervalService.COUNT);

		setTimerValue(millis);
		setRoundsCount(roundsCount);
		IntervalService.setTotalRepsCount(0);
		setRepsCount(true);
		setStartVisible(true);
		txtStart.setEnabled(true);
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

	private void setRoundsCount(Bundle bundle){
		int count = bundle.getInt(IntervalService.COUNT);	
		setRoundsCount(count);
	}

	private void setRoundsCount(int count){
		txtRoundsCount.setText("Round " + getValue(count));
	}

	private void setTotalRepsCount(int count){
		txtTotalRepsCount.setText("Total: " + getValue(count));
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
			setRoundsCount(msg.getData());			
		}

		if(isCountDownRunning || isTimerRunning){
			txtReset.setEnabled(false);
		}

		if(imgSettings.isEnabled()){
			enableSettings(false);
		}
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

	private void setRepsCount(boolean displayValue){		
		if(displayValue){
			txtRepsCount.setText(getValue(IntervalService.getRepsCount()));
			setTotalRepsCount(IntervalService.getTotalRepsCount());
			return;
		}

		if(isTimerRunning){
			int count = IntervalService.getTotalRepsCount();
			count += 1;
			IntervalService.setTotalRepsCount(count);
			IntervalService.setRepsCount(IntervalService.getRepsCount() + 1);

			txtRepsCount.setText(getValue(IntervalService.getRepsCount()));
			setTotalRepsCount(IntervalService.getTotalRepsCount());
		}/*else{
			IntervalService.setTotalRepsCount(0);
		}*/
	}

	private void onSettingsClicked(){
		Intent intent = new Intent(this, IntervalSettingActivity.class);
		startActivityForResult(intent, SETTING_RESULT);
	}

	private void openResult(Bundle bundle){
		Intent intent = new Intent(this, ResultsActivity.class);
		intent.putParcelableArrayListExtra(IntervalService.VALUE, bundle.getParcelableArrayList(IntervalService.VALUE));
		intent.putExtra(ResultsActivity.RESULT_TYPE, ResultsActivity.INTERVAL_RESULT);
		startActivity(intent);
	}

	private String getValue(int value){
		return String.valueOf(value);
	}


	/*private String getFormattedValue(long timeInMillis){
		long mSecs = timeInMillis / 100;
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;
		int hours = minutes / 60;

		minutes = minutes % 60;
		seconds = seconds % 60;
		mSecs = mSecs % 10;

		String time = "";

		if(hours > 0){
			time += hours + ":";		
			time += minutes + ":";
		}else if(minutes > 0){
			time += minutes + ":";
		}

		time += seconds + ":" + mSecs;		
		return time;
	}*/

}
