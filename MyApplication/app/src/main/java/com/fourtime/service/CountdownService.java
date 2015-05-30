package com.fourtime.service;

import com.fourtime.constants.Constants;
import com.fourtime.preference.CountdownPrefrences;
import com.fourtime.preference.SharedPersistance;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class CountdownService extends Service{

	public static final String VALUE = "value";

	private final Messenger messenger = new Messenger(new MsgHandler());
	private Messenger mServiceCaller;
	private MsgHandler mHandler;
	private CountdownPrefrences mPrefs;
	private static boolean isServiceRunning = false;
	private static boolean isActivityRunning = false;
	private static int repsCount = 0;

	private long countDownDuration = 0, countDownSpent = 0;
	private long timerDuration = 0, mTimerSpent = 0;
	private boolean isCountDownProgress = false;
	private boolean isTimerProgress = false;
	private boolean isTimerCompleted = false;
	private boolean isTimerPaused = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return messenger.getBinder();
	}

	@SuppressLint("HandlerLeak")
	private class MsgHandler extends Handler{	
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){

			case ServiceMessage.SERVICE_CONNECTED:
				mServiceCaller = msg.replyTo;
				isActivityRunning = true;
				setPreparationTime();

				if(isCountDownProgress){
					sendMessageToHandler(ServiceMessage.INIT_COUNT_DOWN_PROGRESS, 0);					
				}else if(isTimerProgress){
					sendMessageToHandler(ServiceMessage.INIT_TIMER_STARTED, (timerDuration - mTimerSpent));					
				}else if(isTimerCompleted){
					sendMessageToHandler(ServiceMessage.TIMER_END, repsCount);					
				}else if(isTimerPaused){
					sendMessageToHandler(ServiceMessage.INIT_PUASED_TIMER, (timerDuration - mTimerSpent));
				}else{
					sendResetValues();
				}
				break;

			case ServiceMessage.SERVICE_DISCONNECTED:
				isActivityRunning = false;
				break;

			case ServiceMessage.START_COUNTDOWN:
				startCountDown();
				break;

			case ServiceMessage.RESET_PREFS:
				resetPrefs();
				break;

			case ServiceMessage.RUN_TIMER:
				runTimer();
				break;

			case ServiceMessage.STOP_TIMER:
				isTimerPaused = true;
				stopTimer();
				break;	

			case ServiceMessage.RESET:
				reset();
				sendResetValues();
				break;

			case ServiceMessage.PREF_RESET:
				resetPrefs();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	private void startCountDown(){		
		if(isTimerPaused && mTimerSpent > 0){
			runTimer();
			return;
		}
		isTimerPaused = false;
		mTimerSpent = 0;
		isCountDownProgress = true;
		isTimerCompleted = false;
		countDownSpent = countDownDuration;
		sendMessageToHandler(ServiceMessage.START_COUNTDOWN, 0);
		mHandler.post(runnCountDown);	
	}

	private void runTimer(){
		isCountDownProgress = false;
		isTimerProgress = true;
		mHandler.removeCallbacks(runnCountDown);
		mHandler.post(runnTimer);
	}

	private void stopTimer(){
		if(mTimerSpent >= 100) mTimerSpent -= 100;
		isTimerProgress = false;
		sendMessageToHandler(ServiceMessage.TIMER_STOPPED, timerDuration);
		mHandler.removeCallbacks(runnTimer);
	}

	private void reset(){
		timerDuration = mPrefs.getInterval();
		isTimerProgress = false;
		isTimerCompleted = false;
		isTimerPaused = false;
		repsCount = 0;
		mHandler.removeCallbacks(runnTimer);
	}

	private Runnable runnCountDown = new Runnable() {		
		@Override
		public void run() {
			if(countDownSpent <= 0){
				Message msg = mHandler.obtainMessage();
				msg.what = ServiceMessage.RUN_TIMER;
				mHandler.sendMessage(msg);
				sendMessageToHandler(ServiceMessage.COUNTDOWN_FINISHED, 0);
				playLongBeep();
			}else{
				mHandler.postDelayed(runnCountDown, 1000);
				sendMessageToHandler(ServiceMessage.UPDATE_COUNTDOWN, (countDownSpent / 1000));
				countDownSpent -= 1000;
				playShortBeep();
			}
		}
	};

	private Runnable runnTimer = new Runnable() {		
		@Override
		public void run() {			
			final long millis  = timerDuration - mTimerSpent;								
			sendMessageToHandler(ServiceMessage.UPDATE_TIMER_VALUE, millis);
			
			if(millis <= 0){
				stopTimer();
				isTimerCompleted = true;
				isTimerProgress = false;
				isTimerPaused = false;
				sendMessageToHandler(ServiceMessage.TIMER_END, 0);
				playLongBeep();
			}else{
				mTimerSpent += 100;
				mHandler.postDelayed(runnTimer, 100);
			}
		}
	};


	private void sendMessageToHandler(int MSG_TYPE, long value){			
		Message message = Message.obtain(null, MSG_TYPE);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, value);
		message.setData(bundle);
		sendMessage(message);
	}


	private void init(){
		mHandler = new MsgHandler();
		resetPrefs();
		setPreparationTime();
	}

	private void sendResetValues(){
		Message message = Message.obtain(null, ServiceMessage.RESET);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, timerDuration);
		message.setData(bundle);
		sendMessage(message);
	}

	private void sendMessage(Message msg){
		if(!isActivityRunning) return;
		try {			
			mServiceCaller.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void setPreparationTime(){
		countDownDuration = (new SharedPersistance(this).getPreparationTime() * 1000);
	}

	private void resetPrefs(){
		// re initialize prefrences
		mPrefs = new CountdownPrefrences(this);
		timerDuration = mPrefs.getInterval();		
		repsCount = 0;
		sendResetValues();
	}

	public static boolean isServiceRunning(){
		return isServiceRunning;
	}

	public static int getRepsCount(){
		return repsCount;
	}

	public static void setRepsCount(int count){
		repsCount = count;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		isServiceRunning = true;
		init();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isServiceRunning = false;
		repsCount = 0;
		mHandler.removeCallbacks(runnTimer);
		mHandler.removeCallbacks(runnCountDown);
	}
	
	private void playShortBeep(){
		if(isActivityRunning)
		Constants.soundFX.playShortBeep();
	}
	
	private void playLongBeep(){
		if(isActivityRunning)
		Constants.soundFX.playLongBeep();
	}
}
