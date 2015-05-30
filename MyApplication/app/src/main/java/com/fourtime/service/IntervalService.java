package com.fourtime.service;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.fourtime.constants.Constants;
import com.fourtime.preference.IntervalPreferences;
import com.fourtime.preference.SharedPersistance;
import com.fourtime.result.Result;

public class IntervalService extends Service{

	public static final String TAG = "IntervalService";
	public static final String VALUE = "value";
	public static final String COUNT = "count";

	private IntervalPreferences mPrefs;	
	private final Messenger mMessenger = new Messenger(new MsgHandler());
	private Messenger mCaller;
	private ArrayList<Result> results = new ArrayList<Result>();

	private MsgHandler mHandler;
	private static boolean isRunning = false;
	private static int totalRepscount = 0, repsCount = 0;;
	private boolean isTimerStarted = false;

	private int roundsCompleted = 1;
	private long mTimerDuration = 0;
	private long countDownDuration = 0;
	private long mTimerSpent = 0, mCountDownSpent = 0;
	private boolean isActivityRunning = false;
	private boolean isCountDownProgress = false;
	private boolean isTimerPaused = false;

	@Override
	public IBinder onBind(Intent intent) {		
		return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		isRunning = true;
		init();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
		repsCount = 0;
		mHandler.removeCallbacks(runTimer);
		mHandler.removeCallbacks(runCountDown);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	private void init(){
		mHandler = new MsgHandler();
		resetValues();
		setPreparationTime();
	}

	public static boolean isRunning(){
		return isRunning;
	}

	@SuppressLint("HandlerLeak")
	class MsgHandler extends Handler{		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case ServiceMessage.SERVICE_CONNECTED:
				isActivityRunning = true;
				mCaller = msg.replyTo;
				setPreparationTime();

				if(isCountDownProgress){
					sendMessageToHandler(ServiceMessage.UPDATE_COUNTDOWN, (mCountDownSpent / 1000));
				}else if(isTimerStarted){
					Message m = Message.obtain(null, ServiceMessage.INIT_TIMER_STARTED);
					Bundle bundle = new Bundle();					
					bundle.putInt(COUNT, roundsCompleted);					
					m.setData(bundle);
					sendMessageToHandler(m);

					m = Message.obtain(null, ServiceMessage.SET_REPS_COUNT);
					m.arg1 = totalRepscount;
					sendMessageToHandler(m);

				}else if(isTimerPaused){

					Message m = Message.obtain(null, ServiceMessage.INIT_PUASED_TIMER);
					Bundle bundle = new Bundle();					
					bundle.putInt(COUNT, roundsCompleted);
					long spent = (mTimerSpent > 100 ? (mTimerSpent - 100) : mTimerSpent);
					bundle.putLong(VALUE, (mTimerDuration - spent));
					m.setData(bundle);
					sendMessageToHandler(m);					

				}else{
					sendResetValues();
				}

				break;

			case ServiceMessage.SERVICE_DISCONNECTED:
				isActivityRunning = false;
				break;

			case ServiceMessage.START_COUNTDOWN:				
				starCountDown();
				break;

			case ServiceMessage.STOP_TIMER:
				stopTimer();
				break;

			case ServiceMessage.RUN_TIMER:
				startTimer();
				break;

			case ServiceMessage.RESET:
				isTimerPaused = false;
				sendResetValues();
				resetResults();
				break;

			case ServiceMessage.PREF_RESET:
				resetValues();
				break;

			case ServiceMessage.SEND_RESULT:
				sendResults();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	public static void setTotalRepsCount(int count){
		totalRepscount = count;	
	}

	public static void setRepsCount(int count){
		repsCount = count;
	}

	public static int getRepsCount(){
		return repsCount;
	}

	public static int getTotalRepsCount(){
		return totalRepscount;
	}

	private void starCountDown(){
		if(isTimerPaused){
			startTimer();
			return;
		}
		mCountDownSpent = countDownDuration;
		isTimerStarted = false;
		isCountDownProgress = true;
		isTimerPaused = false;
		resetTimerValues();
		mHandler.removeCallbacks(runCountDown);
		sendMessageToHandler(ServiceMessage.START_COUNTDOWN, 0);
		mHandler.post(runCountDown);
	}

	private void resetTimerValues(){
		mTimerSpent = 0;
		roundsCompleted = 1;
		resetResults();
	}

	private void startTimer(){
		isCountDownProgress = false;
		isTimerStarted = true;
		mHandler.removeCallbacks(runTimer);
		sendMessageToHandler(ServiceMessage.COUNTDOWN_FINISHED, 0);
		sendRoundsCompleted();
		mHandler.post(runTimer);
	}

	private void stopTimer(){
		mHandler.removeCallbacks(runTimer);
		isTimerStarted = false;	
		isTimerPaused = true;
		sendMessageToHandler(ServiceMessage.TIMER_FINISHED, (long)0);
	}

	private Runnable runCountDown = new Runnable() {		
		@Override
		public void run() {
			if(mCountDownSpent <= 0){
				Message msg = mHandler.obtainMessage();
				msg.what = ServiceMessage.RUN_TIMER;
				mHandler.sendMessage(msg);
				playLongBeep();
			}else{
				mHandler.postDelayed(runCountDown, 1000);
				sendMessageToHandler(ServiceMessage.UPDATE_COUNTDOWN, (mCountDownSpent / 1000));
				mCountDownSpent -= 1000;
				playShortBeep();
			}
		}
	};

	private Runnable runTimer = new Runnable() {		
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			final long millis  = mTimerDuration - mTimerSpent;								
			sendMessageToHandler(ServiceMessage.UPDATE_TIMER_VALUE, millis);
			mTimerSpent += 100;

			if(millis <= 0){
				results.add(new Result(roundsCompleted, repsCount));
				roundsCompleted += 1;				
				mTimerSpent = 0;
				repsCount = 0;
				sendRoundsCompleted();
				playShortBeep();
			}

			long diff = System.currentTimeMillis() - startTime;
			int delay = (int) (100 - diff); if(delay < 0) delay = 5;				
			mHandler.postDelayed(runTimer, delay);				
		}
	};


	private void sendMessageToHandler(int MSG_TYPE, long value){			
		Message message = Message.obtain(null, MSG_TYPE);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, value);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendRoundsCompleted(){
		Message message = Message.obtain(null, ServiceMessage.ROUNDS_COMPLETED);
		message.arg1 = roundsCompleted;
		message.arg2 = repsCount;
		sendMessageToHandler(message);
	}

	private void sendResetValues(){
		Message message = Message.obtain(null, ServiceMessage.RESET);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, mTimerDuration);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendResults(){
		Message message = Message.obtain(null, ServiceMessage.SEND_RESULT);
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(VALUE, results);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendMessageToHandler(Message msg){
		if(!isActivityRunning) return;
		try {
			mCaller.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void setPreparationTime(){
		countDownDuration = (new SharedPersistance(this).getPreparationTime() * 1000);
	}

	private void resetResults(){
		repsCount = 0;
		results.clear();
	}

	private void resetValues(){
		mPrefs = new IntervalPreferences(this);
		mTimerDuration = mPrefs.getInterval();
		sendResetValues();
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
