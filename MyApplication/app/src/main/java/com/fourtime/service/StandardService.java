package com.fourtime.service;

import com.fourtime.constants.Constants;
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

public class StandardService extends Service{

	public static final String VALUE = "value";

	private final Messenger messenger = new Messenger(new MsgHandler());
	private Messenger mServiceCaller;
	private MsgHandler mHandler;
	private static boolean isServiceRunning = false;
	private static boolean isActivityRunning = false;
	private static int repsCount = 0;
	private boolean canRunTimer = false;

	private long countDownDuration = 0, mCountDownSpent = 0;
	private long timerDuration = 0;
	private boolean isCountDownProgress = false;
	private boolean isTimerProgress = false;
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
					sendMessageToHandler(ServiceMessage.INIT_TIMER_STARTED, timerDuration);
				}else if(isTimerPaused){
					sendMessageToHandler(ServiceMessage.INIT_PUASED_TIMER, timerDuration);
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

			case ServiceMessage.RESET:
				reset();
				break;

			case ServiceMessage.RUN_TIMER:
				runTimer();
				break;

			case ServiceMessage.STOP_TIMER:
				stopTimer();
				break;	

			default:
				super.handleMessage(msg);
			}
		}
	}


	private void startCountDown(){		
		if(isTimerPaused && timerDuration > 0){
			runTimer();
			return;
		}
		isTimerPaused = false;
		isTimerProgress = false;
		isCountDownProgress = true;
		mCountDownSpent = countDownDuration;
		sendMessageToHandler(ServiceMessage.START_COUNTDOWN, 0);
		mHandler.post(runnCountDown);		
	}

	private void runTimer(){
		isCountDownProgress = false;
		isTimerProgress = true;
		canRunTimer = true;
		mHandler.removeCallbacks(runnCountDown);
		mHandler.post(runnTimer);
	}

	private void stopTimer(){
		isTimerProgress = false;
		isTimerPaused = true;
		canRunTimer = false;
		mHandler.removeCallbacks(runnTimer);
	}

	private Runnable runnCountDown = new Runnable() {		
		@Override
		public void run() {
			if(mCountDownSpent <= 0){
				Message msg = mHandler.obtainMessage();
				msg.what = ServiceMessage.RUN_TIMER;
				mHandler.sendMessage(msg);
				sendMessageToHandler(ServiceMessage.COUNTDOWN_FINISHED, 0);
				playLongBeep();
			}else{
				mHandler.postDelayed(runnCountDown, 1000);
				sendMessageToHandler(ServiceMessage.UPDATE_COUNTDOWN, (mCountDownSpent / 1000));
				mCountDownSpent -= 1000;
				playShortBeep();
			}
		}
	};

	private Runnable runnTimer = new Runnable() {		
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			sendMessageToHandler(ServiceMessage.UPDATE_TIMER_VALUE, timerDuration);
			timerDuration += 100;

			if(canRunTimer){
				long diff = System.currentTimeMillis() - startTime;
				mHandler.postDelayed(runnTimer, (100 - diff));
			}else{
				sendMessageToHandler(ServiceMessage.TIMER_PAUSED, 0);
			}
		}
	};


	private void sendMessageToHandler(int MSG_TYPE, long value){			
		if(!isActivityRunning) return;

		try {
			Message message = Message.obtain(null, MSG_TYPE);
			Bundle bundle = new Bundle();
			bundle.putLong(VALUE, value);
			message.setData(bundle);
			mServiceCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void sendResetValues(){
		if(!isActivityRunning) return;

		try {
			Message message = Message.obtain(null, ServiceMessage.RESET);
			mServiceCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	private void init(){
		mHandler = new MsgHandler();
		setPreparationTime();
	}

	private void reset(){
		timerDuration = 0;
		isTimerPaused = false;
		isTimerProgress = false;
		isCountDownProgress = false;
		repsCount = 0;
		mHandler.removeCallbacks(runnTimer);
		sendMessageToHandler(ServiceMessage.RESET, 0);
	}
		
	private void setPreparationTime(){
		countDownDuration = (new SharedPersistance(this).getPreparationTime() * 1000);
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
