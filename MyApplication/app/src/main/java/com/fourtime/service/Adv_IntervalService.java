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
import com.fourtime.preference.AdvIntervalPreference;
import com.fourtime.preference.SharedPersistance;
import com.fourtime.result.Result;
import com.fourtime.utils.TimerData;

public class Adv_IntervalService extends Service{
	//private static final String TAG = "IntervalService";
	public static final String VALUE = "value";
	public static final String COUNT = "count";

	private AdvIntervalPreference mPrefs;	
	private final Messenger mMessenger = new Messenger(new MsgHandler());
	private Messenger mCaller;
	private ArrayList<Result> results = new ArrayList<Result>();
	private ArrayList<TimerData> timerData = new ArrayList<TimerData>();

	private MsgHandler mHandler;
	private static boolean isRunning = false;
	private static int totalRepscount = 0, repsCount = 0;
	private boolean isTimerStarted = false;
	private boolean isReset = false;

	private int mRounds = 0, mCurrentIndex = 0;
	private int roundsCompleted = 1;
	private long mTimerDuration = 0;
	private long countDownDuration = 0;
	private long mTimerSpent = 0, mCountDownSpent = 0;
	private boolean isActivityRunning = false;
	private boolean isCountDownProgress = false;
	private boolean isLoopInterval = false;

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
					sendMessageToHandler(ServiceMessage.INIT_COUNT_DOWN_PROGRESS, (mCountDownSpent / 1000));
					sendRoundsCount();

				}else if(isTimerStarted){
					Message m = Message.obtain(null, ServiceMessage.INIT_TIMER_STARTED);
					Bundle bundle = new Bundle();					
					bundle.putInt(COUNT, mRounds);
					bundle.putInt(VALUE, roundsCompleted);
					m.setData(bundle);

					try {
						mCaller.send(m);
						m = Message.obtain(null, ServiceMessage.SET_REPS_COUNT);
						m.arg1 = totalRepscount;
						mCaller.send(m);
					} catch (RemoteException e) {				
						e.printStackTrace();
					}
				}else if(!isReset){
					sendFinishOnResume();
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
				sendMessageToHandler(ServiceMessage.COUNTDOWN_FINISHED, 0);
				startTimer();
				break;

			case ServiceMessage.RESET:
				isReset = true;
				resetTimerValues();
				sendResetValues();
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
		if(isTimerStarted){
			startTimer();
			return;
		}
		isReset = false;
		mCountDownSpent = countDownDuration;
		isTimerStarted = false;
		isCountDownProgress = true;
		resetTimerValues();
		mHandler.removeCallbacks(runCountDown);
		sendMessageToHandler(ServiceMessage.START_COUNTDOWN, 0);
		mHandler.post(runCountDown);
	}

	private void resetTimerValues(){
		isTimerStarted = false;
		mTimerSpent = 0;
		roundsCompleted = 1;
		mTimerDuration = timerData.get(0).duration;
		mCurrentIndex = 0;
		resetResults();
	}

	private void startTimer(){
		isCountDownProgress = false;
		isTimerStarted = true;
		mHandler.removeCallbacks(runTimer);		
		mHandler.post(runTimer);
	}

	private void stopTimer(){
		mHandler.removeCallbacks(runTimer);	
		sendMessageToHandler(ServiceMessage.TIMER_FINISHED, (long)0);
	}

	private void onTimerEnd(){
		isTimerStarted = false;
		stopTimer();
		sendTimerEndNotification();
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
			boolean canFinish = false;

			if(millis <=0 && (mCurrentIndex + 1) < timerData.size()){
				results.add(new Result(roundsCompleted, repsCount));
				roundsCompleted += 1;
				mCurrentIndex ++;
				mTimerDuration = timerData.get(mCurrentIndex).duration;				
				mTimerSpent = 0;
				repsCount = 0;
				sendRoundsCompleted();
				playShortBeep();
				
			}else if(millis <=0 && ((mCurrentIndex + 1) >= timerData.size())){
				results.add(new Result(roundsCompleted, repsCount));
				canFinish = true;
				
				if(isLoopInterval){
					canFinish = false;
					mTimerSpent = 0;
					repsCount = 0;
					roundsCompleted = 1;
				    mCurrentIndex = 0;
				    mTimerDuration = timerData.get(mCurrentIndex).duration;	
				    sendRoundsCompleted();
				    playShortBeep();
				}
			}		

			if(canFinish){
				onTimerEnd();
				playLongBeep();
			}else{
				long diff = System.currentTimeMillis() - startTime;
				int delay = (int) (100 - diff); if(delay < 0) delay = 5;				
				mHandler.postDelayed(runTimer, delay);	
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
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendRoundsCompleted(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.ROUNDS_COMPLETED);
			message.arg1 = roundsCompleted;
			message.arg2 = repsCount;
			Bundle bundle = new Bundle();
			bundle.putInt(COUNT, mRounds);
			message.setData(bundle);
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendRoundsCount(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.SEND_ROUNDS_COUNT);
			message.arg1 = mRounds;
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendTimerEndNotification(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.TIMER_END);
			message.arg1 = mRounds;
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	private void sendResetValues(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.RESET);
			Bundle bundle = new Bundle();
			bundle.putLong(VALUE, mTimerDuration);
			bundle.putInt(COUNT, mRounds);
			message.setData(bundle);
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void sendFinishOnResume(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.RESUME_FINISH);
			Bundle bundle = new Bundle();
			bundle.putInt(COUNT, mRounds);
			message.setData(bundle);
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendResults(){
		if(!isActivityRunning) return;
		try {
			Message message = Message.obtain(null, ServiceMessage.SEND_RESULT);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(VALUE, results);
			message.setData(bundle);
			mCaller.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void resetResults(){
		repsCount = 0;
		results.clear();
	}
	
	private void setPreparationTime(){
		countDownDuration = (new SharedPersistance(this).getPreparationTime() * 1000);
	}

	private void resetValues(){
		isReset = true;
		mPrefs = new AdvIntervalPreference(this);
		mRounds = mPrefs.getRounds();
		isLoopInterval = mPrefs.isLoopInterval();
		String work = mPrefs.getRoundIntervals();
		String round[] = null;
		if(!work.equalsIgnoreCase("")){
			round=work.split(",");
		}
		timerData = new ArrayList<TimerData>();		

		if(round!=null){
			if(round.length>0){
				for(int j=0; j<mRounds; j++){						
					timerData.add(new TimerData(j, Long.parseLong(round[j].toString())));
				}	
			}
		}

		mTimerDuration = timerData.get(0).duration;
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
