package com.fourtime.standard;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.fourtime.R;
import com.fourtime.constants.Constants;
import com.fourtime.service.ServiceMessage;
import com.fourtime.service.StandardService;
import com.fourtime.utils.Utils;

public class StandardTimerActivity extends Activity {

	//private LinearLayout repsLayout;
	private TextView txtTime,txtRepsCount,txtStart,txtReset;
	private TextView txtStop;

	private Messenger mService = null;
	private Messenger mMessenger = new Messenger(new ServiceMsgHandler());
	private boolean isBound = false, isTimerRunning = false;


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestPortraitOrientation();
		setContentView(R.layout.standard_new);
		Utils.loadAd(this);
		init();
		setupDefaults();
		setupEvents();
		startBindingService();
	}
	
	private void requestPortraitOrientation(){
		if(!Constants.isTablet){
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unBindService();
	}

	private void startBindingService(){
		setRepsCount(true);
		if(StandardService.isServiceRunning()){					
			bindService();
		}else{
			startService(new Intent(this, StandardService.class));
			bindService();
		}
	}

	private void bindService(){
		bindService(new Intent(this, StandardService.class), mServiceConn, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	private void unBindService(){
		if(isBound){
			if(mService != null){						
				sendMessage(ServiceMessage.SERVICE_DISCONNECTED);
			}
			unbindService(mServiceConn);
			isBound = false;
		}
	}

	private ServiceConnection mServiceConn = new ServiceConnection(){
		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
			sendMessage(ServiceMessage.SERVICE_DISCONNECTED);
			sendMessage(ServiceMessage.RESET_PREFS);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			isBound = true;
			mService = new Messenger(service);
			sendMessage(ServiceMessage.SERVICE_CONNECTED);
		}
	};

	@SuppressLint("HandlerLeak")
	private class ServiceMsgHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {

			long value = msg.getData().getLong(StandardService.VALUE);

			switch(msg.what){

			case ServiceMessage.INIT_COUNT_DOWN_PROGRESS:
				txtReset.setEnabled(false);
				setStartVisible(false);
				txtStop.setEnabled(false);
				break;

			case ServiceMessage.INIT_TIMER_STARTED:
				isTimerRunning = true;
				setStartVisible(false);
				txtReset.setEnabled(false);
				break;

			case ServiceMessage.INIT_PUASED_TIMER:
				setStartVisible(true);
				txtStart.setEnabled(true);
				updateTimer(value);
				break;

			case ServiceMessage.UPDATE_COUNTDOWN:
				isTimerRunning = false;
				updateCountDown((int)value);
				break;
				
			case ServiceMessage.COUNTDOWN_FINISHED:
				 //setStartVisible(false);
				 txtStop.setEnabled(true);
				 txtReset.setEnabled(false);
				break;
				
			case ServiceMessage.START_COUNTDOWN:
				txtReset.setEnabled(false);
				setStartVisible(false);
				txtStop.setEnabled(false);
				break;

			case ServiceMessage.UPDATE_TIMER_VALUE:
				isTimerRunning = true;
				updateTimer(value);
				if(txtStop.getVisibility() != View.VISIBLE){
					setStartVisible(false);
					txtStop.setEnabled(true);
				}
				break;
				
			case ServiceMessage.TIMER_PAUSED:
				setStartVisible(true);
				txtStart.setEnabled(true);
				break;

			case ServiceMessage.RESET:
				isTimerRunning = false;
				setStartVisible(true);
				txtStart.setEnabled(true);
				setRepsCount(true);
				updateTimer(-1);
				break;
				
			default:
				super.handleMessage(msg);
			}			
		}
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

	private void updateTimer(long value){
		if(value < 0){
			txtTime.setText(Utils.getFormattedValue(0l));
		}else{
			txtTime.setText(Utils.getFormattedValue(value)); 
		}
	}

	private void updateCountDown(int value){
		txtTime.setText(String.valueOf(value));
	}


	private void init(){
		//repsLayout = (LinearLayout)findViewById(R.id.repsLayout);
		txtTime = (TextView)findViewById(R.id.txtTime);
		txtRepsCount= (TextView)findViewById(R.id.txtRepsCount);
		txtStart = (TextView)findViewById(R.id.txtStart);
		txtReset= (TextView)findViewById(R.id.txtReset);
		txtStop = (TextView)findViewById(R.id.txtStop);		
	}

	private void setupDefaults() {
		txtTime.setTypeface(Constants.tf_DIGITAL);
		txtRepsCount.setTypeface(Constants.tf_DIGITAL);
		((TextView)findViewById(R.id.txtTitle)).setTypeface(Constants.tf_HELVETICA_BOLD);
		txtStart.setTypeface(Constants.tf_HELVETICA_MEDIUM);;
		txtStop.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		txtReset.setTypeface(Constants.tf_HELVETICA_MEDIUM);
		((TextView)findViewById(R.id.txtReps)).setTypeface(Constants.tf_HELVETICA_MEDIUM);
	}


	private void setupEvents() {	
		txtRepsCount.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					setRepsCount(false);	
				}
				return false;
			}
		});


		txtStart.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {				
				if(txtStart.getVisibility() == View.VISIBLE && txtStart.isEnabled()){
					sendMessage(ServiceMessage.START_COUNTDOWN);
					txtReset.setEnabled(false);					
				}
			}
		});


		txtStop.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(txtStop.getVisibility() == View.VISIBLE && txtStop.isEnabled()){					
					isTimerRunning = false;
					setStartVisible(true);
					txtStart.setEnabled(true);
					txtReset.setEnabled(true);
					sendMessage(ServiceMessage.STOP_TIMER);
				}			
			}
		});


		txtReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v){
				sendMessage(ServiceMessage.RESET);
				setRepsCount(true);
			}
		});

	}

	/*private void onCountDownStarted(){
		txtStart.setEnabled(false);
		txtReset.setEnabled(false);
	}*/

	private void setStartVisible(boolean shouldVisible){
		if(shouldVisible){
			txtStart.setVisibility(View.VISIBLE);
			txtStop.setVisibility(View.INVISIBLE);
		}else{
			txtStop.setVisibility(View.VISIBLE);
			txtStart.setVisibility(View.INVISIBLE);
		}
	}

	private void setRepsCount(boolean onlyToDisplay){
		if(onlyToDisplay){
			txtRepsCount.setText(String.valueOf(StandardService.getRepsCount()));
		}

		if(isTimerRunning){
			int count = StandardService.getRepsCount();
			count += 1;
			txtRepsCount.setText(String.valueOf(count));
			StandardService.setRepsCount(count);
		}
	}


	/*private String formatClock(long elapsed){
		if (elapsed<0)
			return "00:00:00";

		elapsed/=10;

		String hundreths = Integer.toString((int)(elapsed % 100)/10);
		String seconds   = Integer.toString((int)(elapsed % 6000)/100);
		String minutes   = Integer.toString((int)(elapsed / 6000));

		//		if (hundreths.length()<2) hundreths="0"+hundreths;
		if (seconds.length()<2)   seconds="0"+seconds;
		if (minutes.length()<2)   minutes="0"+minutes;

		return minutes+":"+seconds+":"+hundreths;
	}*/

}
