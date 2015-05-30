package com.fourtime.service;

import java.util.ArrayList;

import com.fourtime.constants.Constants;
import com.fourtime.preference.SharedPersistance;
import com.fourtime.preference.TabataPreferences;
import com.fourtime.result.Result;
import com.fourtime.utils.TimerData;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class TabataService extends Service{

	//private static final String TAG = "TabataService";
	public static final String FLAG = "flag";
	public static final String VALUE = "value";
	public static final String COUNT = "count";
	public static final String TABATA_COUNT = "tabata_count";
	public static final String ROUNDS_COUNT = "rounds_count";
	public static final int FLAG_WORK = 100;
	public static final int FLAG_REST = 101;

	private ArrayList<TimerData> timerData;
	private ArrayList<Result> results = new ArrayList<Result>();
	private TabataPreferences mPrefs;	
	private final Messenger mMessenger = new Messenger(new MsgHandler());
	private Messenger mCaller;

	private MsgHandler mHandler;
	private static boolean isRunning = false;
	private static int repsCount = 0;
	private boolean isTimerProgress = false;
	private boolean isTimerEnded = false;

	private int mTabataCount, mRounds, roundsPassed = 0, mTabetaPassed = 1;
	private long mWorkDuration, mRestDuration;
	private long mTimerDuration = 0;
	private long countDownDuration = 0;
	private long mTimerSpent = 0, mCountDownSpent = 0;
	private boolean isActivityRunning = false;
	private boolean isCountDownProgress = false;
	private boolean isTimerPaused = false;
	private int currentMode = FLAG_WORK, mCurrIndex = 0;

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
					Message message = Message.obtain(null, ServiceMessage.INIT_COUNT_DOWN_PROGRESS);
					Bundle bundle = new Bundle();
					bundle.putLong(VALUE, (mCountDownSpent / 1000));
					bundle.putInt(ROUNDS_COUNT, mRounds);
					bundle.putInt(TABATA_COUNT, mTabataCount);
					message.setData(bundle);
					sendMessageToHandler(message);

				}else if(isTimerProgress){
					Message m = Message.obtain(null, ServiceMessage.INIT_TIMER_STARTED);
					Bundle bundle = new Bundle();
					bundle.putInt(FLAG, currentMode);
					bundle.putInt(COUNT, timerData.get(mCurrIndex).count);					
					m.setData(bundle);
					sendMessageToHandler(m);

					m = Message.obtain(null, ServiceMessage.SET_REPS_COUNT);
					m.arg1 = repsCount;
					sendMessageToHandler(m);
					sendTabataCount(mTabetaPassed);	
					
				}else if(isTimerPaused){
					Message m = Message.obtain(null, ServiceMessage.TIMER_PAUSED);
					Bundle bundle = new Bundle();
					bundle.putInt(FLAG, currentMode);
					bundle.putInt(COUNT, timerData.get(mCurrIndex).count);
					bundle.putInt(TABATA_COUNT, mTabetaPassed);
					bundle.putLong(VALUE, (mTimerDuration - mTimerSpent));
					m.setData(bundle);
					sendMessageToHandler(m);
				
				}else if(isTimerEnded){
					Message m = Message.obtain(null, ServiceMessage.INIT_TIMER_END);
					Bundle bundle = new Bundle();
					bundle.putInt(FLAG, currentMode);
					bundle.putInt(COUNT, timerData.get(mCurrIndex).count);
					bundle.putInt(TABATA_COUNT, mTabataCount);
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
				isTimerProgress = false;
				isTimerPaused = true;
				mHandler.removeCallbacks(runTimer);
				if(mTimerSpent > 100) mTimerSpent -= 100;
				//stopTimer(false);
				break;

			case ServiceMessage.RUN_TIMER:
				startTimer();
				break;

			case ServiceMessage.RESET:
				reset();
				sendResetValues();
				resetResults();
				break;

			case ServiceMessage.PREF_RESET:
				resetValues();
				break;

			case ServiceMessage.SEND_RESULT:
				sendResult();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	public static void setRepsCount(int count){
		repsCount = count;
	}

	public static int getRepsCount(){
		return repsCount;
	}

	private void starCountDown(){
		if(isTimerPaused){
			isTimerProgress = true;
			sendMessageToHandler(ServiceMessage.TIMER_STARTED, 0);
			mHandler.post(runTimer);
			return;
		}
		mTimerSpent = 0;
		resetResults();
		mCountDownSpent = countDownDuration;
		isTimerProgress = false;
		isCountDownProgress = true;
		isTimerEnded = false;
		resetTimerValues();
		mHandler.removeCallbacks(runCountDown);
		sendMessageToHandler(ServiceMessage.START_COUNTDOWN, 0);
		mHandler.post(runCountDown);
	}

	private void resetTimerValues(){
		isTimerEnded = false;
		mCurrIndex = 0;
		mTabetaPassed = 1;
		roundsPassed = 0;
		currentMode = FLAG_WORK;
		repsCount = 0;
	}

	private void resetResults(){
		isTimerEnded = false;
		repsCount = 0;
		results.clear();
	}

	private void startTimer(){
		isCountDownProgress = false;
		isTimerProgress = true;
		isTimerPaused = false;
		mTimerDuration = timerData.size() > 0 ? timerData.get(mCurrIndex).duration : 0;
		mHandler.removeCallbacks(runTimer);
		sendTabataCount(mTabetaPassed);
		sendExtraUpdate(currentMode, 1, mTimerDuration);
		sendMessageToHandler(ServiceMessage.COUNTDOWN_FINISHED, 0);
		mHandler.post(runTimer);
	}

	private void stopTimer(boolean disPlayFinish){
		mHandler.removeCallbacks(runTimer);
		isTimerProgress = false;
		if(disPlayFinish){
			sendMessageToHandler(ServiceMessage.DISPLAY_END, (long)0);
		}else{
			sendMessageToHandler(ServiceMessage.TIMER_FINISHED, (long)0);
		}

		if(disPlayFinish){
			results.add(new Result(mTabetaPassed, mRounds, repsCount));
			//results.get(size-1).reps = repsCount;
		}	
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

			boolean canStopTimer = false;

			if(millis <= 0 && (mCurrIndex + 1) < timerData.size()){
				mCurrIndex += 1;
				TimerData data = timerData.get(mCurrIndex);
				currentMode = data.flag;
				mTimerDuration = data.duration;
				mTimerSpent = 0;

				sendExtraUpdate(currentMode, data.count, millis);
				playShortBeep();

				if(currentMode == FLAG_REST){
					roundsPassed += 1;		

					if(roundsPassed >= mRounds){
						roundsPassed = 0;
					}	
				}

				if(currentMode == FLAG_WORK){
					if(roundsPassed != 0)
						addResult();					
				}

				if(currentMode != FLAG_REST && roundsPassed == 0){
					results.add(new Result(mTabetaPassed, mRounds, repsCount));
					mTabetaPassed ++;
					if(mTabetaPassed <= mTabataCount){
						sendTabataCount(mTabetaPassed);
					}
				}

				if(currentMode == FLAG_WORK){
					repsCount = 0;
					sendMessageToHandler(ServiceMessage.SET_REPS_COUNT, 0);
				}

			}else if( (mCurrIndex + 1) >= timerData.size()){
				canStopTimer = true;
				isTimerPaused = false;
				isTimerEnded = true;
			}

			if(millis <=0 && canStopTimer){
				stopTimer(true);
				playLongBeep();
			}else{				
				long diff = System.currentTimeMillis() - startTime;
				int delay = (int) (100 - diff); if(delay < 0) delay = 5;				
				mHandler.postDelayed(runTimer, delay);				
			}			
		}
	};	

	private void sendExtraUpdate(int FLAG_VALUE, int count, long millis){		
		Message message = Message.obtain(null, ServiceMessage.UPDATE_EXTRA_TIMER_VALUE);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, millis);
		bundle.putInt(FLAG, FLAG_VALUE);
		bundle.putInt(COUNT, count);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendMessageToHandler(int MSG_TYPE, long value){					
		Message message = Message.obtain(null, MSG_TYPE);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, value);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendTabataCount(int count){
		Message message = Message.obtain(null, ServiceMessage.UPDATE_TABATA_COUNT);
		message.arg1 = count;
		sendMessageToHandler(message);
	}

	private void sendResetValues(){
		Message message = Message.obtain(null, ServiceMessage.RESET);
		Bundle bundle = new Bundle();
		bundle.putLong(VALUE, mWorkDuration);
		bundle.putInt(ROUNDS_COUNT, mRounds);
		bundle.putInt(TABATA_COUNT, mTabataCount);
		message.setData(bundle);
		sendMessageToHandler(message);
	}

	private void sendResult(){
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

	private void addResult(){
		int reps = repsCount;
		/*if(results.size() > 0){
			reps = repsCount - results.get(results.size()-1).reps;
		}*/
		results.add(new Result(mTabetaPassed, roundsPassed, reps));
	}

	private void reset(){
		resetTimerValues();
		isTimerProgress = false;
		isCountDownProgress = false;
		isTimerPaused = false;
		isTimerEnded = false;
	}

	private void resetValues(){
		mPrefs = new TabataPreferences(this);
		mWorkDuration = mPrefs.getWork();
		mRestDuration = mPrefs.getRest();
		mRounds = mPrefs.getRounds();
		mTabataCount = mPrefs.getTabatas();
		repsCount = 0;

		timerData = new ArrayList<TimerData>();

		for(int i=1; i<=mTabataCount; i++){			
			for(int j=1; j<=mRounds; j++){
				timerData.add(new TimerData(FLAG_WORK, j, mWorkDuration));
				timerData.add(new TimerData(FLAG_REST, j, mRestDuration));
			}			
		}

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
